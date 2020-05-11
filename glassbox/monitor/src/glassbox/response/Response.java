/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.response;

import java.io.Serializable;

/**
 * 
 * Collects data about the system's response while processing a request. These are typically nested, i.e., we track
 * times, parameters, etc. for Servlet requests that result in Struts action requests that result in a database query.
 * 
 * @author Ron Bodkin
 * 
 */

public interface Response extends Serializable {

    public static final int NOT_STARTED = 0;

    public static final int IN_PROGRESS = 1;

    public static final int COMPLETED = 2;

    public static final int FAILED = 3;

    public static final String FAILURE_DATA = "failure.data";
    public static final String EXCEPTION_WARNING = "exception.warning"; // exceptions that aren't deemed to be failures...
    public static final String PARAMETERS = "parameters";
    public static final String RESOURCE_KEY = "resource.key";
    public static final String OPERATION_PRIORITY = "operation.priority";
    public static final String REQUEST = "request";

    // layers
    public static final String UI_CONTROLLER = "ui.controller";

    public static final String UI_RENDERER = "ui.rendering";

    public static final String UI_MODEL = "ui.model";

    public static final String SERVICE_PROCESSOR = "service.procesing";

    public static final String PROCESSING = "processing";

    public static final String RESOURCE_DATABASE = "resource.database";

    public static final String RESOURCE_DATABASE_STATEMENT = "resource.database.statement";

    public static final String RESOURCE_DATABASE_CONNECTION = "resource.database.connection";
    
    public static final String RESOURCE_SERVICE = "resource.service";

    public static final String RESOURCE_FILE = "resource.file";

    public static final String RESOURCE_NAMING = "resource.directory";
    
    public static final String OTHER = "other";

    // priority levels - any integer will do, default is 50
    public static final Integer DEFAULT_PRIORITY = new Integer(50);
    
    Serializable getKey();
    
    void setLayer(Serializable layer);
    Serializable getLayer();

    /** The response within which this one is nested, if any. */ 
    Response getParent();
    
    Serializable getApplication();
    
    Response start();

    Response start(long time);
    
    /** record start without firing event: can be useful when preceeding events haven't been processed yet */ 
    void setStart(long time);

    /** record end without firing event: can be useful when preceeding events haven't been processed yet */ 
    void setEnd(long time);
    
    /** Must be called on same thread as start. If this isn't the case, call complete(long) */
    Response complete();

    Response complete(long duration);

    Response update();

    Response fail();

    long getStart();

    long getEnd();

    long getDuration();

    int getStatus();

    Object get(Serializable key);

    void set(String key, Object value);

    ResponseFactory getFactory();

}