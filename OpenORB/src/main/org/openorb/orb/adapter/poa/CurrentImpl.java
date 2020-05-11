/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.adapter.poa;

import java.util.NoSuchElementException;

import org.apache.avalon.framework.logger.Logger;

import org.openorb.util.CurrentStack;
import org.openorb.util.ExceptionTool;

import org.omg.PortableServer.CurrentPackage.NoContext;

/**
 * This class implements the static thread table for the POA (Current).
 * DispatchState objects are stored in a stack. The class provides
 * additional methods to access information from the DispatchState
 * objects stored on the stack.
 */
class CurrentImpl
    extends org.omg.CORBA.LocalObject
    implements org.omg.PortableServer.Current
{
    /** The stack in which the DispatchState elements are stored. */
    private CurrentStack m_st = new CurrentStack();

    /** The logger instance of this class. */
    private Logger m_logger;

    /**
     * Default constructor.
     */
    CurrentImpl()
    {
    }

    /**
     * Return the object id as an array of bytes.
     */
    public byte[] get_object_id()
        throws NoContext
    {
        return peek().getObjectID();
    }

    /**
     * Return the POA.
     */
    public org.omg.PortableServer.POA get_POA()
        throws NoContext
    {
        return peek().getPoa();
    }

    /**
     * Create an object reference from the information stored in the
     * DispatchState.
     */
    public org.omg.CORBA.Object get_reference()
        throws NoContext
    {
        DispatchState state = peek();
        return state.getPoa().create_reference_with_id( state.getObjectID(),
            state.getRepositoryID() );
    }

    /**
     * Return the servant.
     */
    public org.omg.PortableServer.Servant get_servant()
        throws NoContext
    {
        return peek().getServant();
    }

    /**
     * Peek the next DispatchState from the stack.
     */
    DispatchState peek()
        throws NoContext
    {
        try
        {
            return ( DispatchState ) m_st.peek();
        }
        catch ( final NoSuchElementException ex )
        {
            throw ( NoContext ) ExceptionTool.initCause( new NoContext(), ex );
        }
    }

    /**
     * Push a new DispatchState to the stack.
     */
    void push( DispatchState state )
    {
        m_st.push( state );
    }

    /**
     * Pop a DispatchState from the stack.
     */
    DispatchState pop() throws NoContext
    {
        try
        {
            return ( DispatchState ) m_st.pop();
        }
        catch ( final NoSuchElementException ex )
        {
            getLogger().error( "No element to pop.", ex );
            throw ( NoContext ) ExceptionTool.initCause( new NoContext(), ex );
        }
    }

    /**
     * Provide the class with a logger.
     *
     * @param logger the logger
     */
    protected void enableLogging( Logger logger )
    {
        m_logger = logger;
    }

    /**
     * Return the logger instance for this class.
     */
    private Logger getLogger()
    {
        return m_logger;
    }
}

