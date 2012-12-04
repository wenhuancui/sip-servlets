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

import org.jboss.managed.api.ManagedComponent;
import org.rhq.plugins.jbossas5.ManagedComponentDiscoveryComponent;

/**
 * Manager component for the Service Binding Manager.
 * 
 * @author Filip Drabek
 * @author Lukas Krejci
 */
public class ManagerDiscoveryComponent extends ManagedComponentDiscoveryComponent<ManagerComponent> {

    /**
     * Return a pretty human readable resource name. 
     */
    @Override
    protected String getResourceName(ManagedComponent component) {
        return "Service Binding Manager";
    }

    
}
