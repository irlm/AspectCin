/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.installer.web.controller;

import glassbox.installer.GlassboxInstaller;
import glassbox.installer.GlassboxInstallerFactory;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.springframework.web.servlet.ModelAndView;

public class InstallerControllerTest extends MockObjectTestCase {

	static class InjectException extends RuntimeException {}
	
	// HTTP mocks
	private Mock mockRequest = mock(HttpServletRequest.class);

	// installer mocks
	private Mock mockInstaller = mock(GlassboxInstaller.class);
    private GlassboxInstallerFactory mockInstallerFactory; // configured in setup methods

    
	public void setUp() throws Exception {
		super.setUp();
	}
	
	public void tearDown() throws Exception {
		super.tearDown();
		MockGlassboxInstallerFactory.reset();
	}

	public void testGET() throws Exception {
		mockRequest.expects(once()).method("getMethod").withNoArguments().will( returnValue("GET") );
		HttpServletRequest request = (HttpServletRequest) mockRequest.proxy();
		
		// build one that fails fast to indicate success
		InstallerController installerController = new InstallerController() {

			public ModelAndView handleGET(HttpServletRequest request,
					HttpServletResponse response) throws ServletException, IOException {
				throw new InjectException();
			}
			
		};
		
		try {
			installerController.handleRequest(request, null);
			fail();
		} catch (InjectException ex) {
			//pass
		}
	}

	public void testPOST() throws Exception {
		mockRequest.expects(once()).method("getMethod").withNoArguments().will( returnValue("Post") );
		mockRequest.expects(once()).method("getMethod").withNoArguments().will( returnValue("Post") );
		HttpServletRequest request = (HttpServletRequest) mockRequest.proxy();
		
		// build one that fails fast to indicate success
		InstallerController installerController = new InstallerController() {

			public ModelAndView handlePOST(HttpServletRequest request,
					HttpServletResponse response) throws ServletException, IOException {
				throw new InjectException();
			}
			
		};
		
		try {
			installerController.handleRequest(request, null);
			fail();
		} catch (InjectException ex) {
			//pass
		}
	}

	public void testSimpleInstallConfig() throws Exception {
		
		String customScript = null;
		String customLibDir = null;
		
		mockRequest.expects(once()).method("getParameter").with( eq("customScript") ).will( returnValue(customScript) );
		mockRequest.expects(once()).method("getParameter").with( eq("customLibDir") ).will( returnValue(customLibDir) );
		mockRequest.expects(once()).method("getParameterMap").will( returnValue(new HashMap()) );

		mockInstaller.expects(once()).method("install").withNoArguments().isVoid();
		mockInstaller.expects(once()).method("setCustomScriptToWrap").with( eq(null) ).isVoid();
		mockInstaller.expects(once()).method("setCustomLibDirectory").with( eq(null) ).isVoid();
		mockInstaller.expects(once()).method("customParameters").with( ANYTHING ).isVoid();

		
		HttpServletRequest request = (HttpServletRequest) mockRequest.proxy();
		this.mockInstallerFactory = new MockGlassboxInstallerFactory((GlassboxInstaller)mockInstaller.proxy());
		
		InstallerController installerController = new InstallerController();

		ModelAndView mv = installerController.handlePOST(request, null);
		
		assertEquals("installerResults", mv.getViewName());
	}

	public void testInstallConfigException() throws Exception {
		
		String customScript = null;
		String customLibDir = null;
		
		mockRequest.expects(once()).method("getParameter").with( eq("customScript") ).will( returnValue(customScript) );
		mockRequest.expects(once()).method("getParameter").with( eq("customLibDir") ).will( returnValue(customLibDir) );
		mockRequest.expects(once()).method("getParameterMap").will( returnValue(new HashMap()) );

		mockInstaller.expects(once()).method("setCustomScriptToWrap").with( eq(null) ).isVoid();
		mockInstaller.expects(once()).method("setCustomLibDirectory").with( eq(null) ).isVoid();
		mockInstaller.expects(once()).method("customParameters").with( ANYTHING ).isVoid();
		mockInstaller.expects(once()).method("install").withNoArguments().will(throwException(new RuntimeException("Some Error")));
		mockInstaller.expects(once()).method("reset").withNoArguments();

		mockRequest.expects(once()).method("setAttribute").with( eq("error"), eq("Some Error") ).isVoid();
		mockRequest.expects(once()).method("setAttribute").with( eq("customScript"), eq("") ).isVoid();
		mockRequest.expects(once()).method("setAttribute").with( eq("customLibDir"), eq("") ).isVoid();

		
		HttpServletRequest request = (HttpServletRequest) mockRequest.proxy();
		this.mockInstallerFactory = new MockGlassboxInstallerFactory((GlassboxInstaller)mockInstaller.proxy());
		
		InstallerController installerController = new InstallerController();

		ModelAndView mv = installerController.handlePOST(request, null);
		
		assertEquals("configureInstaller", mv.getViewName());
	}
	
}
