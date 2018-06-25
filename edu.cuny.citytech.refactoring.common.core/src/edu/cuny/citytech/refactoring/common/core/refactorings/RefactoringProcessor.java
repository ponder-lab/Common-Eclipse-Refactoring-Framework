package edu.cuny.citytech.refactoring.common.core.refactorings;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.refactoring.CompilationUnitChange;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationSettings;
import org.eclipse.jdt.internal.corext.refactoring.structure.CompilationUnitRewrite;
import org.eclipse.jdt.internal.corext.refactoring.util.RefactoringASTParser;
import org.eclipse.jdt.internal.corext.refactoring.util.TextEditBasedChangeManager;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;

@SuppressWarnings("restriction")
public abstract class RefactoringProcessor extends org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor {

	private IJavaProject[] javaProjects;
	private CodeGenerationSettings settings;
	private Map<ITypeRoot, CompilationUnit> typeRootToCompilationUnitMap = new HashMap<>();
	private Map<ICompilationUnit, CompilationUnitRewrite> compilationUnitToCompilationUnitRewriteMap = new HashMap<>();

	public RefactoringProcessor(IJavaProject[] javaProjects, final CodeGenerationSettings settings,
			Optional<IProgressMonitor> monitor) {
		try {
			this.javaProjects = javaProjects;
			this.settings = settings;
		} finally {
			monitor.ifPresent(IProgressMonitor::done);
		}
	}

	@Override
	public Object[] getElements() {
		return null;
	}

	@Override
	public boolean isApplicable() throws CoreException {
		return true;
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		this.clearCaches();
		RefactoringStatus status = new RefactoringStatus();
		return status;
	}

	public IJavaProject[] getJavaProjects() {
		return this.javaProjects;
	}

	protected CompilationUnit getCompilationUnit(ITypeRoot root, IProgressMonitor pm) {
		CompilationUnit compilationUnit = this.getTypeRootToCompilationUnitMap().get(root);
		if (compilationUnit == null) {
			compilationUnit = RefactoringASTParser.parseWithASTProvider(root, true, pm);
			this.getTypeRootToCompilationUnitMap().put(root, compilationUnit);
		}
		return compilationUnit;
	}

	protected Map<ITypeRoot, CompilationUnit> getTypeRootToCompilationUnitMap() {
		return this.typeRootToCompilationUnitMap;
	}

	protected void manageCompilationUnit(final TextEditBasedChangeManager manager, CompilationUnitRewrite rewrite,
			Optional<IProgressMonitor> monitor) throws CoreException {
		monitor.ifPresent(m -> m.beginTask("Creating change ...", IProgressMonitor.UNKNOWN));
		CompilationUnitChange change = rewrite.createChange(false, monitor.orElseGet(NullProgressMonitor::new));

		if (change != null)
			change.setTextType("java");

		manager.manage(rewrite.getCu(), change);
	}

	protected CompilationUnitRewrite getCompilationUnitRewrite(ICompilationUnit unit, CompilationUnit root) {
		CompilationUnitRewrite rewrite = this.getCompilationUnitToCompilationUnitRewriteMap().get(unit);
		if (rewrite == null) {
			rewrite = new CompilationUnitRewrite(unit, root);
			this.getCompilationUnitToCompilationUnitRewriteMap().put(unit, rewrite);
		}
		return rewrite;
	}

	public void clearCaches() {
		this.getTypeRootToCompilationUnitMap().clear();
	}

	protected Map<ICompilationUnit, CompilationUnitRewrite> getCompilationUnitToCompilationUnitRewriteMap() {
		return this.compilationUnitToCompilationUnitRewriteMap;
	}

	@Override
	public RefactoringParticipant[] loadParticipants(RefactoringStatus status, SharableParticipants sharedParticipants)
			throws CoreException {
		return new RefactoringParticipant[0];
	}

}
