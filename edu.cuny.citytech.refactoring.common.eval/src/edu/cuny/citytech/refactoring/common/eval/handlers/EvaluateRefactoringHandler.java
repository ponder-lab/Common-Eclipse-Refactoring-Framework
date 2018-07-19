package edu.cuny.citytech.refactoring.common.eval.handlers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Our abstract handler extends AbstractHandler, an IHandler base class.
 *
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public abstract class EvaluateRefactoringHandler extends AbstractHandler {

	public static final boolean BUILD_WORKSPACE = false;

	private static final String EVALUATION_PROPERTIES_FILE_NAME = "eval.properties";

	public static CSVPrinter createCSVPrinter(String fileName, String[] header) throws IOException {
		return new CSVPrinter(new FileWriter(fileName, true), CSVFormat.EXCEL.withHeader(header));
	}

	private static File findEvaluationPropertiesFile(File directory) {
		if (directory == null)
			return null;

		if (!directory.isDirectory())
			throw new IllegalArgumentException("Expecting directory: " + directory + ".");

		File evaluationFile = directory.toPath().resolve(EVALUATION_PROPERTIES_FILE_NAME).toFile();

		if (evaluationFile != null && evaluationFile.exists())
			return evaluationFile;
		else
			return findEvaluationPropertiesFile(directory.getParentFile());
	}

	protected static File findEvaluationPropertiesFile(IJavaProject project) throws JavaModelException {
		IPath location = project.getCorrespondingResource().getLocation();
		return findEvaluationPropertiesFile(location.toFile());
	}

	protected static IType[] getAllDeclaringTypeSubtypes(IMethod method) throws JavaModelException {
		IType declaringType = method.getDeclaringType();
		ITypeHierarchy typeHierarchy = declaringType.newTypeHierarchy(new NullProgressMonitor());
		IType[] allSubtypes = typeHierarchy.getAllSubtypes(declaringType);
		return allSubtypes;
	}

	protected static Set<IMethod> getAllMethods(IJavaProject javaProject) throws JavaModelException {
		Set<IMethod> methods = new HashSet<>();

		// collect all methods from this project.
		IPackageFragment[] packageFragments = javaProject.getPackageFragments();
		for (IPackageFragment iPackageFragment : packageFragments) {
			ICompilationUnit[] compilationUnits = iPackageFragment.getCompilationUnits();
			for (ICompilationUnit iCompilationUnit : compilationUnits) {
				IType[] allTypes = iCompilationUnit.getAllTypes();
				for (IType type : allTypes)
					Collections.addAll(methods, type.getMethods());
			}
		}
		return methods;
	}
}