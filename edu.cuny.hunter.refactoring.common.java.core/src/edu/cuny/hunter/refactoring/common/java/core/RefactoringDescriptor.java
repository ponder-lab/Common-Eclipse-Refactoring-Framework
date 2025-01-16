package edu.cuny.hunter.refactoring.common.java.core;

import java.util.Map;

import org.eclipse.jdt.core.refactoring.descriptors.JavaRefactoringDescriptor;

/**
 * A {@link RefactoringDescriptor} specific to Java refactorings for our research prototypes.
 *
 * @author <a href="mailto:khatchad@hunter.cuny.edu">Raffi Khatchadourian</a>
 */
public class RefactoringDescriptor extends JavaRefactoringDescriptor {

	private static final int FLAGS = STRUCTURAL_CHANGE | MULTI_CHANGE;

	public RefactoringDescriptor(String id) {
		super(id);
	}

	public RefactoringDescriptor(String id, String description, String comment, Map<String, String> arguments) {
		super(id, null, description, comment, arguments, FLAGS);
	}

	public RefactoringDescriptor(String id, String project, String description, String comment, Map<String, String> arguments) {
		super(id, project, description, comment, arguments, FLAGS);
	}
}
