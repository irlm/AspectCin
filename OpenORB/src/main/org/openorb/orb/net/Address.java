/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.net;

import java.lang.reflect.Constructor;

/**
 * General address for a binding target. An address is initialized from an IOR
 * profile and has associated policies and compoenents.
 *
 * Addresses must override equals and hashCode to allow comparisons between
 * different address types to work.
 *
 * @author Chris Wood
 * @version $Revision: 1.4 $ $Date: 2004/07/22 12:25:45 $
 */
public interface Address
{
    /**
     * Get target address with specified addressing disposition.
     */
    org.omg.GIOP.TargetAddress getTargetAddress( short adressingDisposition );

    /**
     * Returns a PolicyList containing the requested PolicyTypes set for the
     * address. If the specified sequence is empty, all
     * Policys set for the address will be returned. If none of the
     * requested PolicyTypes are overridden at the target, an empty sequence
     * is returned.
     */
    org.omg.CORBA.Policy[] get_target_policies( int[] ts );

    /**
     * Returns any components associated with this address. Should return an empty
     * array if there are no components available.
     */
    org.omg.IOP.TaggedComponent [] get_components();

    /**
     * Returns any components with the specified tag. Should return an empty
     * array if there are no components of that type.
     */
    org.omg.IOP.TaggedComponent [] get_components( int tag );

    /**
     * Returns the number of components associated only with the profile
     * which created this address. Components past this index in the array
     * returned from get_components are sourced from a multi-component profile.
     */
    int get_profile_components();

    /**
     * Get the component at the specified index in the get_components array.
     * @throws IndexOutOfBoundsException if the index is out of bounds.
     */
    org.omg.IOP.TaggedComponent get_component( int idx );

    /**
     * Returns the data associated with a particular component, or null if
     * the component at that index has no associated data.
     * @throws IndexOutOfBoundsException if the index is out of bounds.
     */
    Object get_component_data( int idx );

    /**
     * Set the data associated with a particular component, or null if
     * the component at that index has no associated data.
     * @throws IndexOutOfBoundsException if the index is out of bounds.
     */
    void set_component_data( int idx, Object obj );

    /**
     * Set the transport constructor for the address.
     */
    void setTransportConstructor( Constructor ctor, Object [] args );

    /**
     * Use the transport constructor to construct a new transport.
     */
    Transport createTransport();

    /**
     * Only the lowest 12 bits are available for the addresses priority.
     */
    short MASK_ADDRESS_PRIORITY = ClientBinding.MASK_ADDRESS_PRIORITY;

    /**
     * Get the addresses priority. This will be the low order word of the
     * binding priority.
     */
    short getPriority();

    /**
     * Get the addresses priority. This will be the low order word of the
     * binding priority.
     * @param mask mask of bits to get
     */
    short getPriority( short mask );

    /**
     * Set the address priority. This will be the low order word of the
     * binding priority.
     */
    void setPriority( short priority );

    /**
     * Set the address priority. This will be the low order word of the
     * binding priority.
     */
    short setPriority( short priority, short mask );

    /**
     * The protocol string as would appear in the corbaloc address.
     */
    String getProtocol();

    /**
     * A string which could be used to contact the endpoint in a corbaloc
     * style address.
     */
    String getEndpointString();

    /**
     * Human readable string describing the endpoint.
     */
    String getEndpointDescription();

    /**
     * A string which contains an RFC2396 encoding of the object key, as could
     * be included in a corbaloc style address.
     */
    String getObjectKeyString();

    /**
     * Human readable string describing the object key.
     */
    String getObjectKeyDescription();
}

