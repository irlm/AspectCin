/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.monitor;

import glassbox.response.ResponseFactory;
import glassbox.track.api.OperationDescription;
import glassbox.track.api.OperationDescriptionImpl;

public class OperationFactory {
    public static final String UNDEFINED_APPLICATION = "undefined";
    private ResponseFactory defaultResponseFactory;
    
    // this would be the logical place to cache mappings from keys to operations    
    // although the way we set parents complicates this
    // it would also avoid garbage if we cached the string extractions below too... 
    
    /**
     * 
     * @param operationClass must be non-null
     * @param operationName must be non-null
     */
    public OperationDescription makeOperation(Class operationClass, String operationName) {
        return makeOperation(operationClass.getName(), operationName);
    }
    
    /**
     * @param operationClass must be non-null
     * @param operationName must be non-null
     */
    public OperationDescription makeOperation(String operationType, String operationName) {
        if (operationType == null || operationName == null) {
            throw new IllegalArgumentException("null arg(s) "+operationType+", "+operationName);
        }
        // JSP operations and possibly others will use / paths not . class separators...
        int lastPos = operationName.lastIndexOf('/');
        if (lastPos == -1) {
            lastPos = operationName.lastIndexOf('.');
        }
        String shortName = operationName.substring(lastPos+1);
        return new OperationDescriptionImpl(operationType, operationName, shortName, getContextName(), false);
    }
    
    public OperationDescription makeOperation(String operationType, String operationName, String methodName) {
        return makeOperation(operationType, operationName, methodName, false);
    }
    
    public OperationDescription makeRemoteOperation(String operationType, String operationName, String methodName) {
        return makeOperation(operationType, operationName, methodName, true);
    }
    
    protected OperationDescription makeOperation(String operationType, String operationName, String methodName, boolean isRemote) {
        String shortName = operationName.substring(operationName.lastIndexOf('.')+1) + '.' + methodName;
        return new OperationDescriptionImpl(operationType, operationName+'.'+methodName, shortName, getContextName(), isRemote);
    }

    protected String getContextName() {
        String name = (String)defaultResponseFactory.getApplication();
        if (name == null) {
            return UNDEFINED_APPLICATION;
        }
        return name;
    }

    public ResponseFactory getResponseFactory() {
        return defaultResponseFactory;
    }

    public void setResponseFactory(ResponseFactory defaultResponseFactory) {
        this.defaultResponseFactory = defaultResponseFactory;
    }
}
