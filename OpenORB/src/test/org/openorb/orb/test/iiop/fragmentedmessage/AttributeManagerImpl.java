/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.test.iiop.fragmentedmessage;

import java.util.List;
import java.util.LinkedList;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;

import org.omg.PortableServer.POA;

/**
 * @author Michael Macaluso
 */
public class AttributeManagerImpl
    extends AttributeManagerPOA
{
    private AttributeDefinition[] m_attributeDefinitions;
    private int m_numberOfAttributeDefintions = 0;
    private POA m_poa;

    AttributeManagerImpl( POA aPOA )
    {
        m_poa = aPOA;
    }

    public POA _default_POA()
    {
        return m_poa;
    }

    public AttributeDefinition[] getAttributeDefinitions()
    {
        if ( null == m_attributeDefinitions )
        {
            List anAttributeDefinitionsList = new LinkedList();

            // Use an instance of ourselves as the SAX event handler
            LocalContentHandler aLocalContentHandler = new LocalContentHandler(
                  anAttributeDefinitionsList, _orb() );

            // Use the default (non-validating) parser
            SAXParserFactory aSAXParserFactory = SAXParserFactory.newInstance();
            try
            {
                // Parse the input
                SAXParser aSAXParser = aSAXParserFactory.newSAXParser();
                aSAXParser.parse( FragmentedMessageTest.class.getResourceAsStream(
                      "AttributeData.xml" ), aLocalContentHandler );
            }
            catch ( Throwable t )
            {
                t.printStackTrace();
            }

            m_attributeDefinitions = ( AttributeDefinition[] ) anAttributeDefinitionsList.toArray(
                  new AttributeDefinition[ anAttributeDefinitionsList.size() ] );
            m_numberOfAttributeDefintions =
               aLocalContentHandler.getNumberOfAttributeDefinitions();
        }
        return m_attributeDefinitions;
    }

    public int getNumberOfAttributeDefintions()
    {
        return m_numberOfAttributeDefintions;
    }
}

