/*
 * JBoss, Home of Professional Open Source
 * Copyright XXXX, Red Hat Middleware LLC, and individual contributors as indicated
 * by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a full listing
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
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
