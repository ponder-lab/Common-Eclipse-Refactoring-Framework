/**
 *
 */
package edu.cuny.hunter.refactoring.common.java.ui;

import org.eclipse.ltk.core.refactoring.Refactoring;

/**
 * @author <a href="mailto:khatchad@hunter.cuny.edu">Raffi Khatchadourian</a>
 */
public abstract class RefactoringWizard extends org.eclipse.ltk.ui.refactoring.RefactoringWizard {

	public RefactoringWizard(Refactoring refactoring) {
		super(refactoring, RefactoringWizard.DIALOG_BASED_USER_INTERFACE & RefactoringWizard.CHECK_INITIAL_CONDITIONS_ON_OPEN);
		this.setWindowTitle(refactoring.getName());
	}
}
