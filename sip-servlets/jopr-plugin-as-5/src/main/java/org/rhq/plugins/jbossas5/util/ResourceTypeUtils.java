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

package org.rhq.plugins.jbossas5.util;

import java.util.Set;

import org.jetbrains.annotations.Nullable;
import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.configuration.definition.ConfigurationDefinition;
import org.rhq.core.domain.configuration.definition.ConfigurationTemplate;
import org.rhq.core.domain.measurement.MeasurementDefinition;
import org.rhq.core.domain.operation.OperationDefinition;
import org.rhq.core.domain.resource.ResourceType;

/**
 * @author Ian Springer
 */
public class ResourceTypeUtils
{
    public static Configuration getDefaultPluginConfiguration(ResourceType resourceType)
    {
        ConfigurationDefinition pluginConfigurationDefinition = resourceType.getPluginConfigurationDefinition();
        if (pluginConfigurationDefinition != null)
        {
            ConfigurationTemplate template = pluginConfigurationDefinition.getDefaultTemplate();
            if (template != null)
                return template.getConfiguration().deepCopy();
        }
        return new Configuration(); // there is no default plugin config defined - return an empty one
    }

    /**
     * TODO
     *
     * @param resourceType
     * @param metricName
     * @return
     */
    @Nullable
    public static MeasurementDefinition getMeasurementDefinition(ResourceType resourceType, String metricName)
    {
        Set<MeasurementDefinition> metricDefinitions = resourceType.getMetricDefinitions();
        for (MeasurementDefinition metricDefinition : metricDefinitions)
        {
            if (metricDefinition.getName().equals(metricName))
                return metricDefinition;
        }
        return null;
    }

    /**
     * TODO
     *
     * @param resourceType
     * @param operationName
     * @return
     */
    @Nullable
    public static OperationDefinition getOperationDefinition(ResourceType resourceType, String operationName)
    {
        Set<OperationDefinition> operationDefinitions = resourceType.getOperationDefinitions();
        for (OperationDefinition operationDefinition : operationDefinitions)
        {
            if (operationDefinition.getName().equals(operationName))
                return operationDefinition;
        }
        return null;
    }

    private ResourceTypeUtils()
    {
    }
}
