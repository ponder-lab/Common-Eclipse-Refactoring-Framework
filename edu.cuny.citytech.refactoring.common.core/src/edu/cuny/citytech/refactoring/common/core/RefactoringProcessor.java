package edu.cuny.citytech.refactoring.common.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;

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
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		pm.beginTask(Messages.ClearingCaches, 2);

		this.clearCaches();
		pm.worked(1);

		this.getExcludedTimeCollector().clear();
		pm.worked(2);

		pm.done();
		return new RefactoringStatus();
	}

	protected abstract void clearCaches();

	public TimeCollector getExcludedTimeCollector() {
		return this.excludedTimeCollector;
	}

	@Override
	public RefactoringParticipant[] loadParticipants(RefactoringStatus status, SharableParticipants sharedParticipants)
			throws CoreException {
		return new RefactoringParticipant[0];
	}
}
