package edu.cuny.hunter.refactoring.common.java.core;

import static edu.cuny.citytech.refactoring.common.core.Messages.CheckingPreconditions;
import static edu.cuny.citytech.refactoring.common.core.Messages.NoElementsToRefactor;
import static java.util.stream.Collectors.toSet;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.refactoring.CompilationUnitChange;
import org.eclipse.jdt.core.refactoring.descriptors.JavaRefactoringDescriptor;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationSettings;
import org.eclipse.jdt.internal.corext.refactoring.changes.DynamicValidationRefactoringChange;
import org.eclipse.jdt.internal.corext.refactoring.structure.CompilationUnitRewrite;
import org.eclipse.jdt.internal.corext.refactoring.util.RefactoringASTParser;
import org.eclipse.jdt.internal.corext.refactoring.util.TextEditBasedChangeManager;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;

import edu.cuny.citytech.refactoring.common.core.Messages;

/**
 * A {@link RefactoringProcessor} specific to Java refactorings.
 *
 * @author <a href="mailto:khatchad@hunter.cuny.edu">Raffi Khatchadourian</a>
 */
public abstract class RefactoringProcessor extends edu.cuny.citytech.refactoring.common.core.RefactoringProcessor {

	protected Set<RefactorableEntity> entities = new HashSet<>();

	protected Map<ICompilationUnit, CompilationUnitRewrite> compilationUnitToCompilationUnitRewriteMap = new HashMap<>();

	protected CodeGenerationSettings settings;

	protected Map<ITypeRoot, CompilationUnit> typeRootToCompilationUnitMap = new HashMap<>();

	public RefactoringProcessor(CodeGenerationSettings settings) {
		this.settings = settings;
	}

	@Override
	public void clearCaches() {
		this.getTypeRootToCompilationUnitMap().clear();
		this.getCompilationUnitToCompilationUnitRewriteMap().clear();
	}

	protected CompilationUnit getCompilationUnit(ITypeRoot root, IProgressMonitor pm) {
		CompilationUnit compilationUnit = this.getTypeRootToCompilationUnitMap().get(root);

		if (compilationUnit == null) {
			compilationUnit = RefactoringASTParser.parseWithASTProvider(root, true, pm);
			this.getTypeRootToCompilationUnitMap().put(root, compilationUnit);
		}

		return compilationUnit;
	}

	protected CompilationUnitRewrite getCompilationUnitRewrite(ICompilationUnit unit, CompilationUnit root) {
		CompilationUnitRewrite rewrite = this.getCompilationUnitToCompilationUnitRewriteMap().get(unit);

		if (rewrite == null) {
			rewrite = new CompilationUnitRewrite(unit, root);
			this.getCompilationUnitToCompilationUnitRewriteMap().put(unit, rewrite);
		}

		return rewrite;
	}

	protected Map<ICompilationUnit, CompilationUnitRewrite> getCompilationUnitToCompilationUnitRewriteMap() {
		return this.compilationUnitToCompilationUnitRewriteMap;
	}

	protected Map<ITypeRoot, CompilationUnit> getTypeRootToCompilationUnitMap() {
		return this.typeRootToCompilationUnitMap;
	}

	protected static void manageCompilationUnit(final TextEditBasedChangeManager manager, CompilationUnitRewrite rewrite,
			IProgressMonitor monitor) throws CoreException {
		CompilationUnitChange change = rewrite.createChange(false, monitor);

		if (change != null)
			change.setTextType("java");

		manager.manage(rewrite.getCu(), change);
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor monitor, CheckConditionsContext context)
			throws CoreException, OperationCanceledException {
		RefactoringStatus status = new RefactoringStatus();
		SubMonitor progress = SubMonitor.convert(monitor, CheckingPreconditions, 1);

		// get the status of each stream.
		RefactoringStatus collectedStatus = this.getEntities().parallelStream().map(RefactorableEntity::getStatus)
				.collect(() -> new RefactoringStatus(), (a, b) -> a.merge(b), (a, b) -> a.merge(b));
		status.merge(collectedStatus);
		progress.worked(1);

		// if there are no fatal errors.
		if (!status.hasFatalError()) {
			// these are the entities passing preconditions.
			Set<RefactorableEntity> passingEntities = this.getPassingEntities();

			// add a fatal error if there are no passing entities.
			if (passingEntities.isEmpty())
				status.addFatalError(Messages.NoElementsToRefactor);
			else {
				// TODO:
				// Checks.addModifiedFilesToChecker(ResourceUtil.getFiles(fChangeManager.getAllCompilationUnits()),
				// context);
			}
		}

		return status;
	}

	@Override
	public Change createChange(IProgressMonitor monitor) throws CoreException, OperationCanceledException {
		try {
			SubMonitor progress = SubMonitor.convert(monitor, Messages.CreatingChange, 100);

			Set<RefactorableEntity> entities = this.getPassingEntities();

			if (entities.isEmpty())
				return new NullChange(NoElementsToRefactor);

			SubMonitor transformProgress = progress.split(70).setWorkRemaining(entities.size());

			for (RefactorableEntity entity : entities) {
				CompilationUnitRewrite rewrite = this.getCompilationUnitRewrite(entity.getCompilationUnit(),
						entity.getEnclosingCompilationUnit());
				entity.transform(rewrite);
				transformProgress.worked(1);
			}

			// save the source changes.
			TextEditBasedChangeManager manager = new TextEditBasedChangeManager();

			ICompilationUnit[] units = this.getCompilationUnitToCompilationUnitRewriteMap().keySet().stream()
					.filter(cu -> !manager.containsChangesIn(cu)).toArray(ICompilationUnit[]::new);

			SubMonitor saveProgress = progress.split(30).setWorkRemaining(units.length * 2);
			for (ICompilationUnit cu : units) {
				CompilationUnit compilationUnit = this.getCompilationUnit(cu, saveProgress.split(1));
				manageCompilationUnit(manager, this.getCompilationUnitRewrite(cu, compilationUnit), saveProgress.split(1));
			}

			return new DynamicValidationRefactoringChange(this.getDescriptor(), this.getProcessorName(), manager.getAllChanges());
		} finally {
			this.clearCaches();
		}
	}

	protected abstract JavaRefactoringDescriptor getDescriptor();

	@Override
	public Object[] getElements() {
		return this.getEntities().toArray();
	}

	protected Set<RefactorableEntity> getEntities() {
		return entities;
	}

	/**
	 * @return True iff the {@link RefactorableEntity} {@link Set} changed as a result of this call.
	 * @see java.util.Set#addAll
	 */
	protected boolean addEntities(Set<? extends RefactorableEntity> entities) {
		return this.getEntities().addAll(entities);
	}

	public Set<RefactorableEntity> getPassingEntities() {
		Set<RefactorableEntity> entities = this.getEntities();

		return entities == null ? Collections.emptySet()
				: entities.parallelStream().filter(s -> !s.getStatus().hasError()).collect(toSet());
	}
}
