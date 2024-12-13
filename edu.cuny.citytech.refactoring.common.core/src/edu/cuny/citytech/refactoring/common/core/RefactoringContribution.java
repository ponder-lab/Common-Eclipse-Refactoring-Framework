/**
 *
 */
package edu.cuny.citytech.refactoring.common.core;

import java.util.Map;

/**
 * @author <a href="mailto:khatchad@hunter.cuny.edu">Raffi Khatchadourian</a>
 */
public abstract class RefactoringContribution extends org.eclipse.ltk.core.refactoring.RefactoringContribution {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Map retrieveArgumentMap(org.eclipse.ltk.core.refactoring.RefactoringDescriptor descriptor) {
		if (descriptor instanceof RefactoringDescriptor)
			return ((RefactoringDescriptor) descriptor).getArguments();
		else
			return this.retrieveArgumentMap(descriptor);
	}

}