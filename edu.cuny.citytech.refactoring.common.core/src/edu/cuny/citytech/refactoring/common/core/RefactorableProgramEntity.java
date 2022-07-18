package edu.cuny.citytech.refactoring.common.core;

import org.osgi.framework.FrameworkUtil;

public class RefactorableProgramEntity {

	protected static final String PLUGIN_ID = FrameworkUtil.getBundle(RefactorableProgramEntity.class)
			.getSymbolicName();

}