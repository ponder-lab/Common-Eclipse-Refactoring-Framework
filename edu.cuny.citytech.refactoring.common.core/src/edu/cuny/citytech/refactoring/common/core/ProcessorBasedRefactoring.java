package edu.cuny.citytech.refactoring.common.core;

import edu.cuny.citytech.refactoring.common.core.refactorings.CommonRefactoringProcessor;

public class ProcessorBasedRefactoring extends org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring {

	public ProcessorBasedRefactoring(CommonRefactoringProcessor processor) {
		super.setProcessor(processor);
	}

}
