package edu.cuny.citytech.refactoring.common.core.refactorings;

import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;

public class CommonProcessorBasedRefactoring extends ProcessorBasedRefactoring{

	public CommonProcessorBasedRefactoring(CommonRefactoringProcessor processor) {
		super.setProcessor(processor);
	}

}
