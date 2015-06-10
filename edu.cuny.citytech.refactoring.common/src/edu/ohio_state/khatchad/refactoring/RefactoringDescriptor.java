/**
 * 
 */
package edu.ohio_state.khatchad.refactoring;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * @author <a href="mailto:rkhatchadourian@citytech.cuny.edu">Raffi
 *         Khatchadourian</a>
 *
 */
public abstract class RefactoringDescriptor extends
		org.eclipse.ltk.core.refactoring.RefactoringDescriptor {

	protected final Map fArguments;

	protected RefactoringDescriptor(String refactoringID, String project,
			String description, String comment, Map arguments) {
		super(refactoringID, project, description, comment,
				RefactoringDescriptor.STRUCTURAL_CHANGE
						| RefactoringDescriptor.MULTI_CHANGE);

		this.fArguments = arguments;
	}

	public org.eclipse.ltk.core.refactoring.Refactoring createRefactoring(RefactoringStatus status)
			throws CoreException {
		final Refactoring refactoring = this.createRefactoring();
		status.merge(refactoring.initialize(this.fArguments));
		return refactoring;
	}

	protected abstract Refactoring createRefactoring();

	public Map getArguments() {
		return this.fArguments;
	}
}