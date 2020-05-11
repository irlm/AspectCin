/*
 * Copyright (c) 2005 Glassbox Corporation. All Rights Reserved. See license terms for limitations and restrictions on
 * use.
 * 
 * Created on Mar 24, 2005
 */
package glassbox.config;

/**
 * 
 * @author Ron Bodkin
 */
public class AspectConfigurationException extends RuntimeException {

    /**
     * 
     */
    public AspectConfigurationException() {
        super();
    }

    /**
     * @param message
     * @param cause
     */
    public AspectConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     */
    public AspectConfigurationException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public AspectConfigurationException(Throwable cause) {
        super(cause);
    }

    private static final long serialVersionUID = 1L;
}
