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

package org.mobicents.media.server.ctrl.mgcp.evt;

import org.mobicents.media.server.ctrl.mgcp.Request;
import org.mobicents.media.server.spi.Connection;
import org.mobicents.media.server.spi.Endpoint;

/**
 *
 * @author kulikov
 */
public abstract class SignalGenerator {
    
    private String params;
    private Endpoint endpoint;
    private Connection connection;


    public SignalGenerator(String params) {
        this.params = params;
    }

    public Connection getConnection() {
        return connection;
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }
         
    public boolean verify(Connection connection) {
        this.connection = connection;
        return this.doVerify(connection);
    }
    
    public boolean verify(Endpoint endpoint) {
        this.endpoint = endpoint;
        return this.doVerify(endpoint);
    }

    protected abstract boolean doVerify(Connection connection);
    protected abstract boolean doVerify(Endpoint endpoint);

    public abstract void start(Request request);
    public abstract void cancel();
    
    /**
     * Method configures detector - passes media type and detector interface class in general design
     * @param det
     */
    public abstract void configureDetector(EventDetector det);
}
