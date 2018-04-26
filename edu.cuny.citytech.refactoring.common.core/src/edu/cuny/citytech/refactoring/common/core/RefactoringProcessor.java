package edu.cuny.citytech.refactoring.common.core;

import static org.eclipse.jdt.ui.JavaElementLabels.ALL_FULLY_QUALIFIED;
import static org.eclipse.jdt.ui.JavaElementLabels.getElementLabel;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.refactoring.CompilationUnitChange;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationSettings;
import org.eclipse.jdt.internal.corext.refactoring.base.JavaStatusContext;
import org.eclipse.jdt.internal.corext.refactoring.structure.CompilationUnitRewrite;
import org.eclipse.jdt.internal.corext.refactoring.util.RefactoringASTParser;
import org.eclipse.jdt.internal.corext.refactoring.util.TextEditBasedChangeManager;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusContext;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;

@SuppressWarnings("restriction")
public abstract class RefactoringProcessor extends org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor {

	/**
	 * The minimum logging level, one of the constants in {@link IStatus}.
	 */
	protected static int loggingLevel = IStatus.WARNING;

	protected static RefactoringStatus createRefactoringStatus(String message, IMember member,
			BiFunction<String, RefactoringStatusContext, RefactoringStatus> function) {
		String elementName = getElementLabel(member, ALL_FULLY_QUALIFIED);
		return function.apply(MessageFormat.format(message, elementName), JavaStatusContext.create(member));
	}

	protected static int getLoggingLevel() {
		return loggingLevel;
	}

	/**
	 * Minimum logging level. One of the constants in @{link IStatus}.
	 *
	 * @param level
	 *            The minimum logging level to set.
	 * @see org.eclipse.core.runtime.IStatus.
	 */
	public static void setLoggingLevel(int level) {
		loggingLevel = level;
	}

	protected Map<ICompilationUnit, CompilationUnitRewrite> compilationUnitToCompilationUnitRewriteMap = new HashMap<>();

	/**
	 * For excluding AST parse time.
	 */
	protected TimeCollector excludedTimeCollector = new TimeCollector();

	/**
	 * Does the refactoring use a working copy layer?
	 */
	protected boolean layer;

	/** The code generation settings, or <code>null</code> */
	protected CodeGenerationSettings settings;

	protected Map<ITypeRoot, CompilationUnit> typeRootToCompilationUnitMap = new HashMap<>();

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		this.clearCaches();
		this.getExcludedTimeCollector().clear();
		return new RefactoringStatus();
	}

	protected abstract RefactoringStatus checkStructure(IMember member) throws JavaModelException;

	protected RefactoringStatus checkStructure(IMember member, String message) throws JavaModelException {
		if (!member.isStructureKnown())
			return RefactoringStatus.createErrorStatus(
					MessageFormat.format(message, getElementLabel(member, ALL_FULLY_QUALIFIED),
							getElementLabel(member.getCompilationUnit(), ALL_FULLY_QUALIFIED)),
					JavaStatusContext.create(member.getCompilationUnit()));
		return new RefactoringStatus();
	}

	public void clearCaches() {
		this.getCompilationUnitToCompilationUnitRewriteMap().clear();
		this.getTypeRootToCompilationUnitMap().clear();
	}

	public CompilationUnit getCompilationUnit(ITypeRoot root, IProgressMonitor pm) {
		CompilationUnit compilationUnit = this.getTypeRootToCompilationUnitMap().get(root);
		if (compilationUnit == null) {
			this.getExcludedTimeCollector().start();
			compilationUnit = RefactoringASTParser.parseWithASTProvider(root, true, pm);
			this.getExcludedTimeCollector().stop();
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

	public TimeCollector getExcludedTimeCollector() {
		return this.excludedTimeCollector;
	}

	protected Map<ITypeRoot, CompilationUnit> getTypeRootToCompilationUnitMap() {
		return this.typeRootToCompilationUnitMap;
	}

	@Override
	public RefactoringParticipant[] loadParticipants(RefactoringStatus status, SharableParticipants sharedParticipants)
			throws CoreException {
		return new RefactoringParticipant[0];
	}

	protected void manageCompilationUnit(final TextEditBasedChangeManager manager, CompilationUnitRewrite rewrite,
			Optional<IProgressMonitor> monitor) throws CoreException {
		monitor.ifPresent(m -> m.beginTask("Creating change ...", IProgressMonitor.UNKNOWN));
		CompilationUnitChange change = rewrite.createChange(false, monitor.orElseGet(NullProgressMonitor::new));

		if (change != null)
			change.setTextType("java");

		manager.manage(rewrite.getCu(), change);
	}
}
