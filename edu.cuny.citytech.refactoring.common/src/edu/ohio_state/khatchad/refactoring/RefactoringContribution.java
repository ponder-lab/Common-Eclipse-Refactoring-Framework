/**
 * 
 */
package edu.ohio_state.khatchad.refactoring;

import java.util.Map;

/**
 * @author <a href="mailto:rkhatchadourian@citytech.cuny.edu">Raffi Khatchadourian</a>
 *
 */
public abstract class RefactoringContribution extends org.eclipse.ltk.core.refactoring.RefactoringContribution {

	public Map retrieveArgumentMap(org.eclipse.ltk.core.refactoring.RefactoringDescriptor descriptor) {
		if (descriptor instanceof RefactoringDescriptor)
			return ((RefactoringDescriptor) descriptor).getArguments();
		else
			return this.retrieveArgumentMap(descriptor);
	}

}