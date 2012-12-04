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

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.configuration.PropertySimple;
import org.rhq.core.domain.resource.ResourceType;
import org.rhq.core.pluginapi.inventory.DiscoveredResourceDetails;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryComponent;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;
import org.rhq.plugins.jbossas5.helper.MoreKnownComponentTypes;
import org.rhq.plugins.jbossas5.util.RegularExpressionNameMatcher;
import org.rhq.plugins.jbossas5.util.ManagedComponentUtils;

import org.jboss.deployers.spi.management.ManagementView;
import org.jboss.managed.api.ComponentType;
import org.jboss.managed.api.ManagedComponent;

/**
 * A component for discovering JBoss Web connectors.
 *
 * @author Ian Springer
 */
public class ConnectorDiscoveryComponent
        implements ResourceDiscoveryComponent<JBossWebComponent>
{
    // A regex for the names of all MBean:WebRequestProcessor components,
    // e.g. "jboss.web:name=http-127.0.0.1-8080,type=GlobalRequestProcessor"
    private static final String WEB_REQUEST_PROCESSOR_COMPONENT_NAMES_REGEX =
            "jboss.web:name=([^\\-]+)-([^\\-]+)-([0-9]+),type=GlobalRequestProcessor";

    private final Log log = LogFactory.getLog(this.getClass());

    public Set<DiscoveredResourceDetails> discoverResources(
            ResourceDiscoveryContext<JBossWebComponent> discoveryContext) throws Exception
    {
        ResourceType resourceType = discoveryContext.getResourceType();
        log.trace("Discovering " + resourceType.getName() + " Resources...");

        JBossWebComponent jbossWebComponent = discoveryContext.getParentResourceComponent();
        ManagementView managementView = jbossWebComponent.getConnection().getManagementView();

        // TODO (ips): Only refresh the ManagementView *once* per runtime discovery scan, rather than every time this
        //             method is called. Do this by providing a runtime scan id in the ResourceDiscoveryContext.
        managementView.load();

        Set<ManagedComponent> webRequestProcessorComponents = getWebRequestProcessorComponents(managementView);
        Set<DiscoveredResourceDetails> discoveredResources = new LinkedHashSet(webRequestProcessorComponents.size());
        for (ManagedComponent webRequestProcessorComponent : webRequestProcessorComponents)
        {
            // Parse the component name, e.g. "jboss.web:name=http-127.0.0.1-8080,type=GlobalRequestProcessor", to
            // figure out the protocol, address, and port.
            Pattern pattern = Pattern.compile(WEB_REQUEST_PROCESSOR_COMPONENT_NAMES_REGEX);
            Matcher matcher = pattern.matcher(webRequestProcessorComponent.getName());
            if (!matcher.matches())
            {
                log.error("Component name '" + webRequestProcessorComponent.getName() + "' does not match regex '"
                        + pattern + "'.");
                continue;
            }
            String protocol = matcher.group(1);
            String address = matcher.group(2);
            int port = Integer.valueOf(matcher.group(3));

            String resourceKey = protocol + "://" + address + ":" + port;
            String resourceName = protocol + "://" + address + ":" + port;
            String resourceDescription = resourceType.getDescription();
            String resourceVersion = null;

            Configuration pluginConfig = discoveryContext.getDefaultPluginConfiguration();
            pluginConfig.put(new PropertySimple(ManagedComponentComponent.Config.COMPONENT_NAME,
                    webRequestProcessorComponent.getName()));
            pluginConfig.put(new PropertySimple(ConnectorComponent.PROTOCOL_PROPERTY, protocol));
            pluginConfig.put(new PropertySimple(ConnectorComponent.ADDRESS_PROPERTY, address));
            pluginConfig.put(new PropertySimple(ConnectorComponent.PORT_PROPERTY, port));

            DiscoveredResourceDetails resource =
                    new DiscoveredResourceDetails(resourceType,
                            resourceKey,
                            resourceName,
                            resourceVersion,
                            resourceDescription,
                            pluginConfig,
                            null);

            discoveredResources.add(resource);
        }

        log.trace("Discovered " + discoveredResources.size() + " " + resourceType.getName() + " Resources.");
        return discoveredResources;
    }

    private static Set<ManagedComponent> getWebRequestProcessorComponents(ManagementView managementView)
            throws Exception
    {
        ComponentType webRequestProcessorComponentType = MoreKnownComponentTypes.MBean.WebRequestProcessor.getType();
        //return managementView.getMatchingComponents(WEB_REQUEST_PROCESSOR_COMPONENT_NAMES_REGEX,
        //        webRequestProcessorComponentType, new RegularExpressionNameMatcher());
        return ManagedComponentUtils.getManagedComponents(managementView, webRequestProcessorComponentType,
                WEB_REQUEST_PROCESSOR_COMPONENT_NAMES_REGEX, new RegularExpressionNameMatcher());
    }
}