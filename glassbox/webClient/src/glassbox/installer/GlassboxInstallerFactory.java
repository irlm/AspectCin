/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved. This program along with all
 * accompanying source code and applicable materials are made available under the terms of the Lesser Gnu Public License
 * v2.1, which accompanies this distribution and is available at http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.installer;

import glassbox.installer.perContainer.JBossGlassboxInstaller;
import glassbox.installer.perContainer.JettyGlassboxInstaller;
import glassbox.installer.perContainer.OC4JEvermindGlassboxInstaller;
import glassbox.installer.perContainer.OC4JGlassboxInstaller;
import glassbox.installer.perContainer.TomcatGlassboxInstaller;
import glassbox.installer.perContainer.UnknownGlassboxInstaller;
import glassbox.installer.perContainer.WebSphereGlassboxInstaller;
import glassbox.installer.perContainer.WeblogicGlassboxInstaller;

import java.util.*;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class GlassboxInstallerFactory {

    private static final Log log = LogFactory.getLog(GlassboxInstallerFactory.class);

    protected static GlassboxInstallerFactory instance = new GlassboxInstallerFactory();

    public static GlassboxInstallerFactory getInstance() {
    	if (instance == null) { // this would only happen during unit testing 
    		instance = new GlassboxInstallerFactory();
    	}
    	
        return instance;
    }

    protected GlassboxInstaller installer = null;

	List availableInstallers = new ArrayList();

    // This constructor should only be invoked internally or by test classes.
    // Maybe add an enforcement aspect later?
    protected GlassboxInstallerFactory() {

    	// NOTE! These must be checked first! They embeds other web containers...
    	availableInstallers.add(new JBossGlassboxInstaller());
    	availableInstallers.add(new WebSphereGlassboxInstaller());
    	
    	availableInstallers.add(new WeblogicGlassboxInstaller());
    	availableInstallers.add(new OC4JGlassboxInstaller());
    	availableInstallers.add(new OC4JEvermindGlassboxInstaller());
    	availableInstallers.add(new JettyGlassboxInstaller());

    	// likewise Tomcat should be checked last because it is embedded by many containers
        availableInstallers.add(new TomcatGlassboxInstaller());
    }

    public GlassboxInstaller getInstaller(ServletContext context) {
    	
    	if (context == null) 
    		throw new NullPointerException("null argument context");
    	
    	if (this.installer == null) {
    		for (Iterator iterator = availableInstallers.iterator(); iterator.hasNext();) {
				GlassboxInstaller inst = (GlassboxInstaller) iterator.next();
				
				if (inst.matchesContext(context)) {
					this.installer = inst;
					log.info("Chose "+inst+" for Glassbox Installer.");
					break;
				}
			}
    		
    		if (installer == null)
    			installer = new UnknownGlassboxInstaller();
    	}

    	this.installer.setContext(context);
    	
    	return installer;
    }

    public GlassboxInstaller getInstaller() {
    	if (installer == null)
    		throw new RuntimeException("Installer not yet configured. Call getInstaller(ServletContext) first.");

    	return installer;
    }
}
