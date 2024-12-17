/**
 *
 */
package edu.cuny.hunter.refactoring.common.java.ui;

import static org.eclipse.jdt.internal.ui.util.SelectionUtil.toList;
import static org.eclipse.ui.handlers.HandlerUtil.getActiveShellChecked;
import static org.eclipse.ui.handlers.HandlerUtil.getCurrentSelectionChecked;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;

/**
 * @author <a href="mailto:khatchad@hunter.cuny.edu">Raffi Khatchadourian</a>
 */
public abstract class Handler extends AbstractHandler {

	/**
	 * Gather all the streams from the user's selection.
	 */
	@SuppressWarnings("restriction")
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection currentSelection = getCurrentSelectionChecked(event);
		List<?> list = toList(currentSelection);

		Set<IJavaProject> javaProjectSet = new HashSet<>();

		if (list != null)
			try {
				for (Object obj : list)
					if (obj instanceof IJavaElement) {
						IJavaElement jElem = (IJavaElement) obj;
						switch (jElem.getElementType()) {
						case IJavaElement.JAVA_PROJECT:
							javaProjectSet.add((IJavaProject) jElem);
							break;
						}
					}

				Shell shell = getActiveShellChecked(event);

				if (javaProjectSet.isEmpty())
					MessageDialog.openError(shell, this.getHandlerLabel(), Messages.NoProjects);
				else
					this.startRefactoring(javaProjectSet.toArray(new IJavaProject[javaProjectSet.size()]), shell);
			} catch (JavaModelException e) {
				JavaPlugin.log(e);
				throw new ExecutionException("Failed to start refactoring", e);
			}
		return null;
	}

	protected abstract void startRefactoring(IJavaProject[] javaProjects, Shell shell) throws JavaModelException;
}
