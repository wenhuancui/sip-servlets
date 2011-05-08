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

import org.jboss.managed.api.ManagedComponent;
import org.jboss.metatype.api.values.SimpleValue;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;
import org.rhq.plugins.jbossas5.util.Ejb2BeanUtils;

/**
 * A discovery component for EJB 1/2 beans.
 * @author Lukas Krejci
 */
public class Ejb2BeanDiscoveryComponent extends ManagedComponentDiscoveryComponent<AbstractManagedDeploymentComponent> {

    @Override
    protected String getResourceKey(ManagedComponent component) {
        return Ejb2BeanUtils.getUniqueBeanIdentificator(component);
    }

    @Override
    protected String getResourceName(ManagedComponent component) {
        return Ejb2BeanUtils.parseResourceName(component);
    }

    @Override
    protected boolean accept(ResourceDiscoveryContext<AbstractManagedDeploymentComponent> discoveryContext,
        ManagedComponent component) {
        String deploymentName = ((SimpleValue) component.getProperty("DeploymentName").getValue()).getValue()
            .toString();

        AbstractManagedDeploymentComponent parentDeployment = discoveryContext.getParentResourceComponent();

        String parentDeploymentName = parentDeployment.getDeploymentName();

        //compare ignoring the trailing slash
        if (deploymentName.endsWith("/"))
            deploymentName = deploymentName.substring(0, deploymentName.length() - 1);
        if (parentDeploymentName.endsWith("/"))
            parentDeploymentName = parentDeploymentName.substring(0, parentDeploymentName.length() - 1);

        return parentDeploymentName.equals(deploymentName);
    }
}
