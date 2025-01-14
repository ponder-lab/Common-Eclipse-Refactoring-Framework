/**
 *
 */
package edu.cuny.hunter.refactoring.common.java.ui;

import org.eclipse.jdt.internal.ui.refactoring.RefactoringMessages;
import org.eclipse.jdt.internal.ui.refactoring.actions.RefactoringStarter;
import org.eclipse.jdt.ui.refactoring.RefactoringSaveHelper;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.swt.widgets.Shell;

/**
 * @author <a href="mailto:khatchad@hunter.cuny.edu">Raffi Khatchadourian</a>
 */
public abstract class RefactoringWizard extends org.eclipse.ltk.ui.refactoring.RefactoringWizard {

	@SuppressWarnings("restriction")
	protected static void startRefactoring(Shell shell, RefactoringWizard wizard) {
		new RefactoringStarter().activate(wizard, shell, RefactoringMessages.OpenRefactoringWizardAction_refactoring,
				RefactoringSaveHelper.SAVE_REFACTORING);
	}

	public RefactoringWizard(Refactoring refactoring) {
		super(refactoring, DIALOG_BASED_USER_INTERFACE & CHECK_INITIAL_CONDITIONS_ON_OPEN);
		this.setWindowTitle(refactoring.getName());
	}
}
