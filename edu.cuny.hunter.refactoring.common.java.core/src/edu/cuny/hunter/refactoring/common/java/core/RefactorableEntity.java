package edu.cuny.hunter.refactoring.common.java.core;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.corext.refactoring.structure.CompilationUnitRewrite;

public abstract class RefactorableEntity extends edu.cuny.citytech.refactoring.common.core.RefactorableEntity {

	public abstract ICompilationUnit getCompilationUnit();

	public abstract CompilationUnit getEnclosingCompilationUnit();

	@SuppressWarnings("restriction")
	public abstract void transform(CompilationUnitRewrite rewrite);
}
