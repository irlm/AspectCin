/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.installer;

import java.io.File;
import java.util.Map;

import javax.servlet.ServletContext;

public interface GlassboxInstaller {
    public boolean isInstalled();

    public boolean isPartiallyInstalled();
    
    public File getInstallLog();

    public void install() throws InstallException;

    public String getFormattedEnvVars();
    
    public String getConfigureInstallerPageName();
    
    public String getInstallerResultsPageName();
    
    public String getTargetSystemLabel();
    
    public String getFullJavaVersion();
    
    public String getJavaVendor();
    
    public String getVersion();
    
    public File getDefaultScript();
    public File getDefaultLibDirectory();

    /**
     * this is only exposed so InstallValidationHelper can do some things....
     * 
     * @return
     */
    public File getContainerHome();

    /**
     * If null then no script provided
     * 
     * @return null or the complete launch command including args
     */
    public String getGlassboxLaunchCommand();
    
    public void setCustomScriptToWrap(File customScript);
    public File getCustomScriptToWrap();
    
    public void setCustomLibDirectory(File customLibDirectory);
    public File getCustomLibDirectory();

	public boolean matchesContext(ServletContext context);

	public void setContext(ServletContext context);

	public void reset();

	public void customParameters(Map parameterMap);

}
