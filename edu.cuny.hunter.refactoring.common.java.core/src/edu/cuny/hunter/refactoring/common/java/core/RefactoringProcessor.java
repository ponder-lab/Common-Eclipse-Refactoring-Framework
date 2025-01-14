package edu.cuny.hunter.refactoring.common.java.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.refactoring.CompilationUnitChange;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationSettings;
import org.eclipse.jdt.internal.corext.refactoring.structure.CompilationUnitRewrite;
import org.eclipse.jdt.internal.corext.refactoring.util.RefactoringASTParser;
import org.eclipse.jdt.internal.corext.refactoring.util.TextEditBasedChangeManager;

/**
 * A {@link RefactoringProcessor} specific to Java refactorings.
 *
 * @author <a href="mailto:khatchad@hunter.cuny.edu">Raffi Khatchadourian</a>
 */
@SuppressWarnings("restriction")
public abstract class RefactoringProcessor extends edu.cuny.citytech.refactoring.common.core.RefactoringProcessor {

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
			Optional<IProgressMonitor> monitor) throws CoreException {
		monitor.ifPresent(m -> m.beginTask("Creating change ...", IProgressMonitor.UNKNOWN));
		CompilationUnitChange change = rewrite.createChange(false, monitor.orElseGet(NullProgressMonitor::new));

		if (change != null)
			change.setTextType("java");

		manager.manage(rewrite.getCu(), change);
	}
}
