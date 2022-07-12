package edu.cuny.citytech.refactoring.common.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * Language agnostic {@link RefactoringProcessor}.
 * 
 * @author <a href="mailto:rk1424@hunter.cuny.edu">Raffi Khatchadourian</a>
 */
public abstract class RefactoringProcessor extends org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor {

	/**
	 * For excluding AST parse time.
	 */
	private TimeCollector excludedTimeCollector = new TimeCollector();

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		this.clearCaches();
		this.getExcludedTimeCollector().clear();
		return new RefactoringStatus();
	}

	protected abstract void clearCaches();

	public TimeCollector getExcludedTimeCollector() {
		return this.excludedTimeCollector;
	}
}
