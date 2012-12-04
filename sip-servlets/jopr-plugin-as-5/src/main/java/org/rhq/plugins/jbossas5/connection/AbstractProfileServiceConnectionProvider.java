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

package org.rhq.plugins.jbossas5.connection;

import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Ian Springer
 */
public abstract class AbstractProfileServiceConnectionProvider implements ProfileServiceConnectionProvider {
    private final Log log = LogFactory.getLog(this.getClass());

    private ProfileServiceConnection existingConnection;

    public final ProfileServiceConnection connect() {
        this.existingConnection = doConnect();
        this.existingConnection.init();
        return this.existingConnection;
    }

    protected abstract ProfileServiceConnection doConnect();

    public boolean isConnected() {
        // TODO: Ping the connection to make sure it's not defunct?
        return (this.existingConnection != null);
    }

    public final void disconnect() {
        if (isConnected()) {
            this.existingConnection = null;
            doDisconnect();
        }
    }

    protected abstract void doDisconnect();

    public ProfileServiceConnection getExistingConnection() {
        return this.existingConnection;
    }

    protected InitialContext createInitialContext(Properties env) {
        InitialContext initialContext;
        this.log.debug("Creating JNDI InitialContext with env [" + env + "]...");
        try {
            initialContext = new InitialContext(env);
        } catch (NamingException e) {
            throw new RuntimeException("Failed to create JNDI InitialContext.", e);
        }
        this.log.debug("Created JNDI InitialContext [" + initialContext + "].");
        return initialContext;
    }

    protected Object lookup(InitialContext initialContext, String name) {
        try {
            Object found = initialContext.lookup(name);
            return found;
        } catch (NamingException e) {
            throw new RuntimeException("Failed to lookup JNDI name '" + name + "' from InitialContext.", e);
        }
    }
}
