package edu.cuny.citytech.refactoring.common.core;

import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public abstract class RefactorableEntity {

	protected RefactoringStatus status = new RefactoringStatus();

	public RefactoringStatus getStatus() {
		return this.status;
	}
}
