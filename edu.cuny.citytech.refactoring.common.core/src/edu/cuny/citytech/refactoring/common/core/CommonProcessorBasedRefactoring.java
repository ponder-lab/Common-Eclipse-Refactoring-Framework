package edu.cuny.citytech.refactoring.common.core;

import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;

import edu.cuny.citytech.refactoring.common.core.refactorings.CommonRefactoringProcessor;

public class CommonProcessorBasedRefactoring extends ProcessorBasedRefactoring{

	public CommonProcessorBasedRefactoring(CommonRefactoringProcessor processor) {
		super.setProcessor(processor);
	}

}
