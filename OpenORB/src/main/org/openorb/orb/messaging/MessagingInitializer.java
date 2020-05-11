/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.messaging;

import org.apache.avalon.framework.logger.AbstractLogEnabled;

import org.omg.PortableInterceptor.ORBInitInfo;

import org.openorb.orb.Initializer;

import org.openorb.orb.pi.FeatureInitializer;
import org.openorb.orb.pi.FeatureInitInfo;

/**
 * @author Michael Rumpf
 */
public class MessagingInitializer
    extends AbstractLogEnabled
    implements FeatureInitializer, Initializer
{
    public String getName()
    {
        return "msg";
    }

    public void init( ORBInitInfo orbinfo, FeatureInitInfo featureinfo )
    {
        PolicyFactoryImpl pf = PolicyFactoryImpl.getInstance();

        orbinfo.register_policy_factory(
                org.omg.Messaging.SYNC_SCOPE_POLICY_TYPE.value, pf );
        orbinfo.register_policy_factory(
                org.omg.Messaging.RELATIVE_REQ_TIMEOUT_POLICY_TYPE.value, pf );
        orbinfo.register_policy_factory(
                org.omg.Messaging.RELATIVE_RT_TIMEOUT_POLICY_TYPE.value, pf );
        orbinfo.register_policy_factory(
                org.omg.Messaging.REPLY_END_TIME_POLICY_TYPE.value, pf );
        orbinfo.register_policy_factory(
                org.omg.Messaging.REPLY_START_TIME_POLICY_TYPE.value, pf );
        orbinfo.register_policy_factory(
                org.omg.Messaging.REQUEST_END_TIME_POLICY_TYPE.value, pf );
        orbinfo.register_policy_factory(
                org.omg.Messaging.REQUEST_START_TIME_POLICY_TYPE.value, pf );
    }
}

