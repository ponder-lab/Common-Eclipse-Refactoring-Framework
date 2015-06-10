/**
 * 
 */
package edu.ohio_state.khatchad.refactoring;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * @author <a href="mailto:rkhatchadourian@citytech.cuny.edu">Raffi
 *         Khatchadourian</a>
 *
 */
public abstract class RefactoringPlugin extends AbstractUIPlugin {

	protected static RefactoringPlugin plugin;

	public static RefactoringPlugin getDefault() {
		return plugin;
	}

	protected abstract String getRefactoringId();

	public void log(Throwable throwable) {
		getDefault().getLog().log(
				new Status(IStatus.ERROR, this.getRefactoringId(), 0, throwable
						.getMessage(), throwable));
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

}