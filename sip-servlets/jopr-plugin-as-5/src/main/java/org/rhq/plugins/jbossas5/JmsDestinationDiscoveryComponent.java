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
import org.rhq.plugins.jbossas5.util.ManagedComponentUtils;

/**
 * A discovery component for JMS topics and queues.
 *
 * @author Ian Springer
 */
public class JmsDestinationDiscoveryComponent extends ManagedComponentDiscoveryComponent<ApplicationServerComponent> {
    private static final String NAME_MANAGED_PROPERTY = "name";

    @Override
    protected String getResourceName(ManagedComponent component) {
        // For topics and queues, use their name, rather than their JNDI name, as the default Resource name, since the
        // name is usually more terse than the JNDI name (e.g. "DLQ" versus "/queue/DLQ").
        String name = (String)ManagedComponentUtils.getSimplePropertyValue(component, NAME_MANAGED_PROPERTY);
        return (name != null) ? name : component.getName();
    }
}