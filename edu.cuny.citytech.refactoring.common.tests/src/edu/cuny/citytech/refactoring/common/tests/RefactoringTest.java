package edu.cuny.citytech.refactoring.common.tests;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.jdt.ui.tests.refactoring.GenericRefactoringTest;
import org.eclipse.jdt.ui.tests.refactoring.rules.RefactoringTestSetup;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

@SuppressWarnings("restriction")
public abstract class RefactoringTest extends GenericRefactoringTest {

	private static final String REPLACE_EXPECTED_WITH_ACTUAL_KEY = "edu.cuny.citytech.refactoring.common.tests.replaceExpectedWithActual";

	/**
	 * The name of the directory containing resources under the project directory.
	 */
	private static final String RESOURCE_PATH = "resources";

	protected static void assertFailedPrecondition(RefactoringStatus initialStatus, RefactoringStatus finalStatus) {
		assertTrue("Precondition was supposed to fail.", !initialStatus.isOK() || !finalStatus.isOK());
	}

	protected static Path getAbsolutionPath(String fileName) {
		Path path = Paths.get(RESOURCE_PATH, fileName);
		Path absolutePath = path.toAbsolutePath();
		return absolutePath;
	}

	public static void setFileContents(String fileName, String contents) throws IOException {
		Path absolutePath = getAbsolutionPath(fileName);
		Files.write(absolutePath, contents.getBytes());
	}

	/**
	 * True if the expected output should be replaced with the actual output. Useful
	 * to creating new or updated test data and false otherwise.
	 */
	protected boolean replaceExpectedWithActual;

	/**
	 * Creates a new {@link RefactoringTest}.
	 */
	public RefactoringTest() {
		setReplaceExpectedWithActualFromProperty();
	}

	/**
	 * Creates a new {@link RefactoringTest}.
	 *
	 * @param replaceExpectedWithActual True if the expected output should be
	 *        replaced with the actual output.
	 */
	public RefactoringTest(boolean replaceExpectedWithActual) {
		this.replaceExpectedWithActual = replaceExpectedWithActual;
	}

	public RefactoringTest(RefactoringTestSetup rts) {
		super(rts);
		this.setReplaceExpectedWithActualFromProperty();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.jdt.ui.tests.refactoring.RefactoringTest#getFileContents(java
	 * .lang.String) Had to override this method because, since this plug-in is a
	 * fragment (at least I think that this is the reason), it doesn't have an
	 * activator and the bundle is resolving to the eclipse refactoring test bundle.
	 */
	@Override
	public String getFileContents(String fileName) throws IOException {
		Path absolutePath = getAbsolutionPath(fileName);
		byte[] encoded = Files.readAllBytes(absolutePath);
		return new String(encoded, Charset.defaultCharset());
	}


	private void setReplaceExpectedWithActualFromProperty() {
		String replaceProperty = System.getenv(REPLACE_EXPECTED_WITH_ACTUAL_KEY);

		if (replaceProperty != null)
			this.replaceExpectedWithActual = Boolean.valueOf(replaceProperty);
	}
	
	/**
	 * Returns the extension of the test file(s), e.g., .py, .java.
	 * @return The test filename extension.
	 */
	protected abstract String getTestFileExtension();
	
	@Override
	protected String createTestFileName(String fileName, String infix) {
		return getTestPath() + getName() + infix + fileName + '.' + getTestFileExtension();
	}
}
