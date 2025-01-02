package edu.cuny.citytech.refactoring.common.core;

import org.eclipse.osgi.util.NLS;

/**
 * @author <a href="mailto:khatchad@hunter.cuny.edu">Raffi Khatchadourian</a>
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "edu.cuny.citytech.refactoring.common.core.Messages"; //$NON-NLS-1$
	public static String ClearingCaches;
	public static String CheckingPreconditions;
	public static String CompilingSource;
	public static String CreatingChange;
	public static String CUContainsCompileErrors;
	public static String RefactoringNotPossible;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
