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
package org.mobicents.media.server.ctrl.mgcp;

import org.mobicents.media.server.spi.Connection;
import org.mobicents.media.server.spi.ConnectionListener;
import org.mobicents.media.server.spi.ConnectionMode;
import org.mobicents.media.server.spi.ConnectionState;

/**
 * Used as a listener for an actual connection and sends events to the MGCP CA.
 * 
 * @author kulikov
 */
public class ConnectionActivity implements ConnectionListener {

    private static int GEN = 1;
    
    protected Connection connection;
    private String id;
    private Call call;
    
    protected ConnectionActivity(Call call, Connection connection) {
        this.call = call;
        this.id = Integer.toHexString(GEN++);
        if (GEN == Integer.MAX_VALUE) {
            GEN = 1;
        }
        this.connection = connection;
        connection.addListener(this);
    }
    
    public String getID() {
        return id;
    }
    
    public void onStateChange(Connection connection, ConnectionState oldState) {
    }

    public void onModeChange(Connection connection, ConnectionMode oldMode) {
    }
    
    public Connection getMediaConnection() {
        return connection;
    }
    
    public void close() {
        try {
            //call.removeConnection(id);
        	call.removeConnection(this);
        } catch (Exception e) {
        }
    	connection.getEndpoint().deleteConnection(connection.getId());
        connection.removeListener(this);
        this.connection = null;
    }

}
