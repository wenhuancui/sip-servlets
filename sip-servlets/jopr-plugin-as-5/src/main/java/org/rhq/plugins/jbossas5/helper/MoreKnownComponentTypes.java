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

package org.rhq.plugins.jbossas5.helper;

import org.jboss.deployers.spi.management.KnownComponentTypes;
import org.jboss.managed.api.ComponentType;

/**
 * @author Ian Springer
 */
public interface MoreKnownComponentTypes
{
    /**
     * An enum of additional MBean:* ManagedComponent types not defined in {@link KnownComponentTypes}.
     */
    public enum MBean
    {
        Platform,
        Servlet,
        SipServlet,
        Web,
        WebApplication,
        WebApplicationManager,
        ConvergedSipWebApplicationManager,
        WebHost,
        WebRequestProcessor,
        WebThreadPool;

        public String type()
        {
            return this.getClass().getSimpleName();
        }

        public String subtype()
        {
            return this.name();
        }

        public ComponentType getType()
        {
            return new ComponentType(type(), subtype());
        }
    }

    /**
     * An enum of additional MCBean:* ManagedComponent types not defined in {@link KnownComponentTypes}.
     */
    public enum MCBean
    {
        JTA,
        MCServer,
        Security,
        ServerConfig,
        ServerInfo,
        ServicebindingManager,
        ServicebindingMetadata,
        ServiceBindingSet,
        ServiceBindingStore;

        public String type()
        {
            return this.getClass().getSimpleName();
        }

        public String subtype()
        {
            return this.name();
        }

        public ComponentType getType()
        {
            return new ComponentType(type(), subtype());
        }
    }

    /**
     * An enum of additional WAR:* ManagedComponent types not defined in {@link KnownComponentTypes}.
     */
    public enum WAR
    {
        Context;

        public String type()
        {
            return this.getClass().getSimpleName();
        }

        public String subtype()
        {
            return this.name();
        }

        public ComponentType getType()
        {
            return new ComponentType(type(), subtype());
        }
    }
}
