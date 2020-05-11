/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.installer.web.controller;

import javax.servlet.ServletContext;

import glassbox.installer.GlassboxInstaller;
import glassbox.installer.GlassboxInstallerFactory;

/**
 * If you use this class, make sure to call the static reset() method to remove a mock
 * instance from the static singleton Factory.
 * 
 */
public class MockGlassboxInstallerFactory extends GlassboxInstallerFactory {

	public static void reset() {
		GlassboxInstallerFactory.instance = null;
	}
	
	protected MockGlassboxInstallerFactory(GlassboxInstaller installer) {
		this.installer = installer;
		
		GlassboxInstallerFactory.instance = this;
	}

	public GlassboxInstaller getInstaller(ServletContext context) {
		return installer;
	}
}
