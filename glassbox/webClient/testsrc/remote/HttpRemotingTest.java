/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package remote;

import glassbox.common.BaseTestCase;
import glassbox.remoting.GlassboxRemoteService;

public class HttpRemotingTest extends BaseTestCase {

	public HttpRemotingTest(String arg0) {
		super(arg0);
	}
	
	public void testReflect() {
		System.out.println("---> Testing Remoting Reflect: ");
		GlassboxRemoteService service = (GlassboxRemoteService)getContext().getBean("glassboxService");
		assertNotNull(service);
		
        //not ready
		//service.reflect();
		
	}

}
