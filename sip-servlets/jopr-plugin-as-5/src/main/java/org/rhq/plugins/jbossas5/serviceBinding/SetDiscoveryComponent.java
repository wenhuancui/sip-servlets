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

package org.rhq.plugins.jbossas5.serviceBinding;

import java.util.HashSet;
import java.util.Set;

import org.jboss.managed.api.ManagedComponent;
import org.jboss.metatype.api.values.CollectionValue;
import org.jboss.metatype.api.values.CompositeValue;
import org.jboss.metatype.api.values.MetaValue;
import org.rhq.core.domain.resource.ResourceType;
import org.rhq.core.pluginapi.inventory.DiscoveredResourceDetails;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryComponent;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;

/**
 * Discovery component for binding sets.
 * 
 * @author Filip Drabek
 * @author Lukas Krejci
 */
public class SetDiscoveryComponent implements ResourceDiscoveryComponent<ManagerComponent> {

    public Set<DiscoveredResourceDetails> discoverResources(ResourceDiscoveryContext<ManagerComponent> context)
        throws InvalidPluginConfigurationException, Exception {

        ResourceType resourceType = context.getResourceType();

        //refresh the management view
        ManagerComponent managerResourceComponent = context.getParentResourceComponent();
        managerResourceComponent.getConnection().getManagementView().load();

        ManagedComponent bindingManagerComponent = managerResourceComponent.getBindingManager();

        CollectionValue bindingSets = (CollectionValue) bindingManagerComponent.getProperty(Util.BINDING_SETS_PROPERTY)
            .getValue();

        Set<DiscoveredResourceDetails> discoveredResources = new HashSet<DiscoveredResourceDetails>(bindingSets
            .getSize());

        for (MetaValue m : bindingSets.getElements()) {
            CompositeValue bindingSet = (CompositeValue) m;

            String bindingSetName = Util.getValue(bindingSet, Util.NAME_PROPERTY, String.class);
            String resourceKey = context.getParentResourceComponent().getBindingSetResourceKey(bindingSetName);

            DiscoveredResourceDetails resource = new DiscoveredResourceDetails(resourceType, resourceKey,
                bindingSetName, null, resourceType.getDescription(), context.getDefaultPluginConfiguration(), null);

            discoveredResources.add(resource);
        }
        return discoveredResources;
    }
}
