/**
 *
 */
package edu.cuny.citytech.refactoring.common.core;

import java.util.Map;

import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * @author <a href="mailto:rkhatchadourian@citytech.cuny.edu">Raffi Khatchadourian</a>
 */
public abstract class Refactoring extends org.eclipse.ltk.core.refactoring.Refactoring {

	@SuppressWarnings("static-method")
	public RefactoringStatus initialize(@SuppressWarnings({ "rawtypes", "unused" }) Map arguments) {
		return new RefactoringStatus();
	}
}
