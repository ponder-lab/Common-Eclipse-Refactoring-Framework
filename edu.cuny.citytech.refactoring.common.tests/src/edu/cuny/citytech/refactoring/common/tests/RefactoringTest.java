package edu.cuny.citytech.refactoring.common.tests;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
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

	protected abstract Logger getLogger(); // TODO: Should use built-in Eclipse
											// logger.

	/**
	 * Returns the refactoring to be tested.
	 * 
	 * @param methods
	 *            The methods to refactor.
	 * @param cu
	 *            The compilation unit being tested. Can be null.
	 * @return The refactoring to be tested.
	 * @throws JavaModelException
	 */
	protected abstract Refactoring getRefactoring(IMethod... methods) throws JavaModelException; // TODO:
																									// Should
																									// use
																									// createRefactoring().

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.ui.tests.refactoring.RefactoringTest#createCUfromTestFile
	 * (org.eclipse.jdt.core.IPackageFragment, java.lang.String)
	 */
	@Override
	protected ICompilationUnit createCUfromTestFile(IPackageFragment pack, String cuName) throws Exception {
		ICompilationUnit unit = super.createCUfromTestFile(pack, cuName);

		if (!unit.isStructureKnown())
			throw new IllegalArgumentException(cuName + " has structural errors.");
		else
			return unit;
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

	protected void helperFail(String outerMethodName, String[] outerSignature, String innerTypeName,
			String[] methodNames, String[][] signatures) throws Exception {
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
	
	protected void helperPass(String innerTypeName, String[] methodNames, String[][] signatures) throws Exception {
		helperPass("A", null, null, innerTypeName, methodNames, signatures);
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

	/**
	 * Check for failed preconditions for the case where there is no input.
	 * 
	 * @throws Exception
	 */
	protected void helperFail() throws Exception {
		helperFail("A", null, null);
	}

	protected void helperPass(String[] methodNames, String[][] signatures) throws Exception {
		ICompilationUnit cu = createCUfromTestFile(getPackageP(), "A");
		IType type = getType(cu, "A");
		IMethod[] methods = getMethods(type, methodNames, signatures);
		helperPass(cu, methods);
	}

	private void helperPass(ICompilationUnit cu, IMethod[] methods)
			throws JavaModelException, CoreException, Exception, IOException {
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
	
	private void helperPass(String typeName, String outerMethodName, String[] outerSignature, String innerTypeName,
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
		helperPass(cu, methods);
	}

	/**
	 * Test methods in two classes, namely, A and B.
	 */
	protected void helperPass(String[] methodNames1, String[][] signatures1, String[] methodNames2,
			String[][] signatures2) throws Exception {
		ICompilationUnit cu = createCUfromTestFile(getPackageP(), "A");
		IType type = getType(cu, "A");
		Set<IMethod> methodSet = new LinkedHashSet<>();
		Collections.addAll(methodSet, getMethods(type, methodNames1, signatures1));

		type = getType(cu, "B");
		Collections.addAll(methodSet, getMethods(type, methodNames2, signatures2));

		Refactoring refactoring = getRefactoring(methodSet.toArray(new IMethod[methodSet.size()]));

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

	/**
	 * Test methods in two classes, namely, A and B, with no fatal errors.
	 */
	protected void helperPassNoFatal(String[] methodNames1, String[][] signatures1, String[] methodNames2,
			String[][] signatures2) throws Exception {
		ICompilationUnit cu = createCUfromTestFile(getPackageP(), "A");
		IType type = getType(cu, "A");
		Set<IMethod> methodSet = new LinkedHashSet<>();
		Collections.addAll(methodSet, getMethods(type, methodNames1, signatures1));

		type = getType(cu, "B");
		Collections.addAll(methodSet, getMethods(type, methodNames2, signatures2));

		Refactoring refactoring = getRefactoring(methodSet.toArray(new IMethod[methodSet.size()]));

		RefactoringStatus initialStatus = refactoring.checkInitialConditions(new NullProgressMonitor());
		getLogger().info("Initial status: " + initialStatus);

		RefactoringStatus finalStatus = refactoring.checkFinalConditions(new NullProgressMonitor());
		getLogger().info("Final status: " + finalStatus);

		assertTrue("Precondition was supposed to pass.",
				!initialStatus.hasFatalError() && !finalStatus.hasFatalError());
		performChange(refactoring, false);

		String expected = getFileContents(getOutputTestFileName("A"));
		String actual = cu.getSource();
		assertEqualLines(expected, actual);
	}

	/**
	 * Test methods in three classes, namely, A, B, and C, with no fatal errors.
	 */
	protected void helperPassNoFatal(String[] methodNames1, String[][] signatures1, String[] methodNames2,
			String[][] signatures2, String[] methodNames3, String[][] signatures3) throws Exception {
		ICompilationUnit cu = createCUfromTestFile(getPackageP(), "A");
		IType type = getType(cu, "A");
		Set<IMethod> methodSet = new LinkedHashSet<>();
		Collections.addAll(methodSet, getMethods(type, methodNames1, signatures1));

		type = getType(cu, "B");
		Collections.addAll(methodSet, getMethods(type, methodNames2, signatures2));

		type = getType(cu, "C");
		Collections.addAll(methodSet, getMethods(type, methodNames3, signatures3));

		Refactoring refactoring = getRefactoring(methodSet.toArray(new IMethod[methodSet.size()]));

		RefactoringStatus initialStatus = refactoring.checkInitialConditions(new NullProgressMonitor());
		getLogger().info("Initial status: " + initialStatus);

		RefactoringStatus finalStatus = refactoring.checkFinalConditions(new NullProgressMonitor());
		getLogger().info("Final status: " + finalStatus);

		assertTrue("Precondition was supposed to pass.",
				!initialStatus.hasFatalError() && !finalStatus.hasFatalError());
		performChange(refactoring, false);

		String expected = getFileContents(getOutputTestFileName("A"));
		String actual = cu.getSource();
		assertEqualLines(expected, actual);
	}

	public void testPlainMethod() throws Exception {
		helperPass(new String[] { "m" }, new String[][] { new String[0] });
	}
}
