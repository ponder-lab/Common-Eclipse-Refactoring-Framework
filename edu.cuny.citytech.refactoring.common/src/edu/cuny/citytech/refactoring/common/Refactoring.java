/**
 * 
 */
package edu.cuny.citytech.refactoring.common;

import java.util.Map;

import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * @author <a href="mailto:rkhatchadourian@citytech.cuny.edu">Raffi
 *         Khatchadourian</a>
 *
 */
public abstract class Refactoring extends
		org.eclipse.ltk.core.refactoring.Refactoring {

	public abstract RefactoringStatus initialize(Map arguments);

}