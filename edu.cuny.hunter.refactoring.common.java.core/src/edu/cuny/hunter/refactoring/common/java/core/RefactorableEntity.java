package edu.cuny.hunter.refactoring.common.java.core;

import static org.eclipse.jdt.internal.corext.dom.ASTNodes.getParent;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.internal.corext.refactoring.structure.CompilationUnitRewrite;

public abstract class RefactorableEntity extends edu.cuny.citytech.refactoring.common.core.RefactorableEntity {

	public abstract ICompilationUnit getCompilationUnit();

	public CompilationUnit getEnclosingCompilationUnit() {
		return (CompilationUnit) getParent(this.getEnclosingTypeDeclaration(), ASTNode.COMPILATION_UNIT);
	}

	public abstract TypeDeclaration getEnclosingTypeDeclaration();

	public abstract void transform(CompilationUnitRewrite rewrite);
}
