package edu.cuny.citytech.refactoring.common.core;

import edu.cuny.citytech.refactoring.common.core.refactorings.RefactoringProcessor;

public class ProcessorBasedRefactoring extends org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring {

	public ProcessorBasedRefactoring(RefactoringProcessor processor) {
		super.setProcessor(processor);
	}

}
