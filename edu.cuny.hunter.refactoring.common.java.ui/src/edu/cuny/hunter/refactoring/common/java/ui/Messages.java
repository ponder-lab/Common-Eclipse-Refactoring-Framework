package edu.cuny.hunter.refactoring.common.java.ui;

import org.eclipse.osgi.util.NLS;

/**
 * @author <a href="mailto:khatchad@hunter.cuny.edu">Raffi Khatchadourian</a>
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "edu.cuny.hunter.refactoring.common.java.ui.Messages"; //$NON-NLS-1$
	public static String NoProjects;

	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
