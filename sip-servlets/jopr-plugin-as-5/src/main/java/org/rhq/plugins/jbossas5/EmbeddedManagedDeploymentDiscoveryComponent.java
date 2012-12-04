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

import org.jboss.managed.api.ManagedDeployment;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;

/**
 * @author Ian Springer
 */
public class EmbeddedManagedDeploymentDiscoveryComponent extends AbstractManagedDeploymentDiscoveryComponent {
    protected boolean accept(ManagedDeployment managedDeployment) {
        return (managedDeployment.getParent() != null);
    }
}
