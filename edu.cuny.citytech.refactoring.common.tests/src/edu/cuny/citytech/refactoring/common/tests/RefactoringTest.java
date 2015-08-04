package edu.cuny.citytech.refactoring.common.tests;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;

import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

@SuppressWarnings("restriction")
public abstract class RefactoringTest extends org.eclipse.jdt.ui.tests.refactoring.RefactoringTest {

	/**
	 * The name of the directory containing resources under the project
	 * directory.
	 */
	private static final String RESOURCE_PATH = "resources";

	public RefactoringTest(String name) {
		super(name);
	}

	private static void assertFailedPrecondition(RefactoringStatus initialStatus, RefactoringStatus finalStatus) {
		assertTrue("Precondition was supposed to fail.", !initialStatus.isOK() || !finalStatus.isOK());
	}

	protected void assertFailedPrecondition(IMethod... methods) throws CoreException {
		Refactoring refactoring = getRefactoring(methods);
	
		RefactoringStatus initialStatus = refactoring.checkInitialConditions(new NullProgressMonitor());
		getLogger().info("Initial status: " + initialStatus);
	
		RefactoringStatus finalStatus = refactoring.checkFinalConditions(new NullProgressMonitor());
		getLogger().info("Final status: " + finalStatus);
	
		assertFailedPrecondition(initialStatus, finalStatus);
	}

	protected abstract Logger getLogger(); // TODO: Should use built-in Eclipse logger.

	/**
	 * Returns the refactoring to be tested.
	 * @param methods The methods to refactor.
	 * @return The refactoring to be tested.
	 */
	protected abstract Refactoring getRefactoring(IMethod... methods); 	// TODO: Should use createRefactoring().

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.ui.tests.refactoring.RefactoringTest#getFileContents(java
	 * .lang.String) Had to override this method because, since this plug-in is
	 * a fragment (at least I think that this is the reason), it doesn't have an
	 * activator and the bundle is resolving to the eclipse refactoring test
	 * bundle.
	 */
	@Override
	public String getFileContents(String fileName) throws IOException {
		Path path = Paths.get(RESOURCE_PATH, fileName);
		Path absolutePath = path.toAbsolutePath();
		byte[] encoded = Files.readAllBytes(absolutePath);
		return new String(encoded, Charset.defaultCharset());
	}

	private void helperFail(String typeName, String outerMethodName, String[] outerSignature, String innerTypeName,
			String[] methodNames, String[][] signatures) throws Exception {
		ICompilationUnit cu = createCUfromTestFile(getPackageP(), typeName);
		IType type = getType(cu, typeName);
	
		if (outerMethodName != null) {
			IMethod method = type.getMethod(outerMethodName, outerSignature);
			if (innerTypeName != null) {
				type = method.getType(innerTypeName, 1); // get the local type
			} else {
				type = method.getType("", 1); // get the anonymous type.
			}
		} else if (innerTypeName != null) {
			type = type.getType(innerTypeName); // get the member type.
		}
	
		IMethod[] methods = getMethods(type, methodNames, signatures);
		assertFailedPrecondition(methods);
	}

	protected void helperFail(String outerMethodName, String[] outerSignature, String innerTypeName, String[] methodNames,
			String[][] signatures) throws Exception {
		helperFail("A", outerMethodName, outerSignature, innerTypeName, methodNames, signatures);
	}

	/**
	 * Check for a failed precondition for a case with an inner type.
	 * 
	 * @param outerMethodName
	 *            The method declaring the anonymous type.
	 * @param outerSignature
	 *            The signature of the method declaring the anonymous type.
	 * @param methodNames
	 *            The methods in the anonymous type.
	 * @param signatures
	 *            The signatures of the methods in the anonymous type.
	 * @throws Exception
	 */
	protected void helperFail(String outerMethodName, String[] outerSignature, String[] methodNames,
			String[][] signatures) throws Exception {
		helperFail("A", outerMethodName, outerSignature, null, methodNames, signatures);
	}

	protected void helperFail(String innerTypeName, String[] methodNames, String[][] signatures) throws Exception {
		helperFail("A", null, null, innerTypeName, methodNames, signatures);
	}

	/**
	 * Check for failed precondition for a simple case.
	 * 
	 * @param methodNames
	 *            The methods to test.
	 * @param signatures
	 *            Their signatures.
	 * @throws Exception
	 */
	protected void helperFail(String[] methodNames, String[][] signatures) throws Exception {
		helperFail("A", null, null, null, methodNames, signatures);
	}

	protected void helperPass(String[] methodNames, String[][] signatures) throws Exception {
		ICompilationUnit cu = createCUfromTestFile(getPackageP(), "A");
		IType type = getType(cu, "A");
		IMethod[] methods = getMethods(type, methodNames, signatures);
	
		Refactoring refactoring = getRefactoring(methods);
	
		RefactoringStatus initialStatus = refactoring.checkInitialConditions(new NullProgressMonitor());
		getLogger().info("Initial status: " + initialStatus);
	
		RefactoringStatus finalStatus = refactoring.checkFinalConditions(new NullProgressMonitor());
		getLogger().info("Final status: " + finalStatus);
	
		assertTrue("Precondition was supposed to pass.", initialStatus.isOK() && finalStatus.isOK());
		performChange(refactoring, false);
	
		String expected = getFileContents(getOutputTestFileName("A"));
		String actual = cu.getSource();
		assertEqualLines(expected, actual);
	}

	public void testPlainMethod() throws Exception {
		helperPass(new String[] { "m" }, new String[][] { new String[0] });
	}
}
