/**
 *
 */
package edu.cuny.citytech.refactoring.common.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Base class for all refactoring plugins.
 *
 * @author <a href="mailto:rkhatchadourian@citytech.cuny.edu">Raffi Khatchadourian</a>
 */
public abstract class RefactoringPlugin extends AbstractUIPlugin {

	/**
	 * Returns this refactoring's identifier.
	 *
	 * @return This Refactori's identifier.
	 */
	protected abstract String getRefactoringId();

	/**
	 * Log a Throwable to the log for this plugin as an error.
	 *
	 * @param throwable The Throwable to log.
	 */
	public void log(Throwable throwable) {
		this.getLog().log(new Status(IStatus.ERROR, this.getRefactoringId(), 0, throwable.getMessage(), throwable));
	}
}
