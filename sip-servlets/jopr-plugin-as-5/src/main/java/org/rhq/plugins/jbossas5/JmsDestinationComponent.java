/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.rhq.plugins.jbossas5;

import java.util.ArrayList;
import java.util.List;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.configuration.PropertySimple;
import org.rhq.core.pluginapi.configuration.ConfigurationUpdateReport;

/**
 * A Resource component for JBoss AS 5 JBoss Messaging JMS topic and queues.
 *
 * @author Ian Springer
 */
public class JmsDestinationComponent extends ManagedComponentComponent
{
    private static final String[] OBJECT_NAME_PROPERTY_NAMES = new String[]{"DLQ", "expiryQueue", "serverPeer"};

    @Override
    public void updateResourceConfiguration(ConfigurationUpdateReport configurationUpdateReport)
    {
        Configuration resourceConfig = configurationUpdateReport.getConfiguration();
        List<String> invalidObjectNamePropertyNames = new ArrayList();
        for (String objectNamePropertyName : OBJECT_NAME_PROPERTY_NAMES)
        {
            PropertySimple propertySimple = resourceConfig.getSimple(objectNamePropertyName);
            try
            {
                validateObjectNameProperty(propertySimple);
            }
            catch (MalformedObjectNameException e)
            {
                propertySimple.setErrorMessage("Invalid ObjectName: " + e.getLocalizedMessage());
                invalidObjectNamePropertyNames.add(propertySimple.getName());
            }
        }
        if (!invalidObjectNamePropertyNames.isEmpty())
            configurationUpdateReport.setErrorMessage("The following ObjectName properties have invalid values: "
                    + invalidObjectNamePropertyNames);
        else
            super.updateResourceConfiguration(configurationUpdateReport);
    }

    private static void validateObjectNameProperty(PropertySimple propertySimple) throws MalformedObjectNameException
    {
        if (propertySimple != null)
        {
            String value = propertySimple.getStringValue();
            if (value != null)
            {
                ObjectName objectName = new ObjectName(value);
                if (objectName.isPattern())
                    throw new MalformedObjectNameException("Patterns are not allowed.");
            }
        }
    }
}