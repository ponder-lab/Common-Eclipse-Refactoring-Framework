package edu.cuny.citytech.refactoring.common.eval.handlers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.eclipse.core.commands.AbstractHandler;

/**
 * Our abstract handler extends AbstractHandler, an IHandler base class.
 *
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public abstract class EvaluateRefactoringHandler extends AbstractHandler {

	private static final String EVALUATION_PROPERTIES_FILE_NAME = "eval.properties";

	protected static CSVPrinter createCSVPrinter(String fileName, String[] header) throws IOException {
		return new CSVPrinter(new FileWriter(fileName, true), CSVFormat.EXCEL.builder().setHeader(header).build());
	}

	protected File findEvaluationPropertiesFile(File directory) {
		if (directory == null)
			return null;

		if (!directory.isDirectory())
			throw new IllegalArgumentException("Expecting directory: " + directory + ".");

		File evaluationFile = directory.toPath().resolve(getEvaluationPropertiesFileName()).toFile();

		if (evaluationFile != null && evaluationFile.exists())
			return evaluationFile;
		else
			return findEvaluationPropertiesFile(directory.getParentFile());
	}

	protected String getEvaluationPropertiesFileName() {
		return EVALUATION_PROPERTIES_FILE_NAME;
	}
}