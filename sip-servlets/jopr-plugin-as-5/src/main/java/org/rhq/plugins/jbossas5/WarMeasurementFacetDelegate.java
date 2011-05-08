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

import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.Nullable;

import org.jboss.deployers.spi.management.ManagementView;
import org.jboss.managed.api.ManagedComponent;
import org.jboss.managed.api.ManagedDeployment;
import org.jboss.profileservice.spi.NoSuchDeploymentException;

import org.rhq.core.domain.measurement.MeasurementDataTrait;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.core.pluginapi.measurement.MeasurementFacet;
import org.rhq.plugins.jbossas5.util.ManagedComponentUtils;

/**
 * @author Ian Springer
 */
public class WarMeasurementFacetDelegate implements MeasurementFacet
{
    private static final String CONTEXT_ROOT_TRAIT = "contextRoot";
    private static final String VIRTUAL_HOSTS_TRAIT = "virtualHosts";

    private static final String CONTEXT_COMPONENT_NAME = "ContextMO";

    private final Log log = LogFactory.getLog(this.getClass());

    private AbstractManagedDeploymentComponent managedDeploymentComponent;

    public WarMeasurementFacetDelegate(AbstractManagedDeploymentComponent managedDeploymentComponent)
    {
        this.managedDeploymentComponent = managedDeploymentComponent;
    }

    public void getValues(MeasurementReport report, Set<MeasurementScheduleRequest> requests)
            throws Exception
    {
        ManagementView managementView = this.managedDeploymentComponent.getConnection().getManagementView();
        String contextPath = getContextPath();
        for (MeasurementScheduleRequest request : requests)
        {
            String metricName = request.getName();
            try
            {
                if (metricName.equals(CONTEXT_ROOT_TRAIT))
                {
                    if (contextPath == null)
                    {
                        // We can't figure out the context root for a stopped WAR.
                        continue;
                    }
                    String contextRoot = (contextPath.equals("/")) ? "/" : contextPath.substring(1);
                    MeasurementDataTrait trait = new MeasurementDataTrait(request, contextRoot);
                    report.addData(trait);
                }
                else if (metricName.equals(VIRTUAL_HOSTS_TRAIT))
                {
                    if (contextPath == null)
                    {
                        // We can't figure out the virtual hosts for a stopped WAR.
                        continue;
                    }
                    Set<String> virtualHosts = WebApplicationContextDiscoveryComponent.getVirtualHosts(contextPath,
                            managementView);
                    String value = "";
                    for (Iterator<String> iterator = virtualHosts.iterator(); iterator.hasNext();)
                    {
                        String virtualHost = iterator.next();
                        value += virtualHost;
                        if (iterator.hasNext())
                            value += ", ";
                    }
                    MeasurementDataTrait trait = new MeasurementDataTrait(request, value);
                    report.addData(trait);
                }
            }
            catch (Exception e)
            {
                // Don't let one bad apple spoil the barrel.
                log.error("Failed to collect metric '" + metricName + "' for "
                        + this.managedDeploymentComponent.getResourceDescription() + ".", e);
            }
        }
    }

    /**
     * Returns this WAR's context path (e.g. "/jmx-console"), or <code>null</code> if the WAR is currently stopped,
     * since stopped WARs are not associated with any contexts.
     *
     * @return this WAR's context path (e.g. "/jmx-console"), or <code>null</code> if the WAR is currently stopped,
     *         since stopped WARs are not associated with any contexts
     * @throws NoSuchDeploymentException if the WAR is no longer deployed
     */
    @Nullable
    private String getContextPath()
            throws NoSuchDeploymentException
    {
        ManagedDeployment deployment = this.managedDeploymentComponent.getManagedDeployment();
        ManagedComponent contextComponent = deployment.getComponent(CONTEXT_COMPONENT_NAME);
        if (contextComponent != null)
        {
            return (String)ManagedComponentUtils.getSimplePropertyValue(contextComponent, "contextRoot");
        }
        else
        {
            return null;
        }
    }
}
