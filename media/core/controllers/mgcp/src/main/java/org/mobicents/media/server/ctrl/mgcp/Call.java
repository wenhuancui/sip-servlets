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

import java.io.Serializable;


import org.mobicents.media.server.Utils;
import org.mobicents.media.server.spi.Connection;

/**
 * Represents MGCP Call.
 * @author baranowb
 * @author kulikov
 */
public class Call implements Serializable {

	private static final int _CONNECTION_TAB_SIZE = 40;
    private String id;
    private int callTableIndex = -1;
    private MgcpController controller;
    //private ConcurrentHashMap<String, ConnectionActivity> connections = new ConcurrentHashMap<String, ConnectionActivity>();
    //private AtomicInteger connectionCount = new AtomicInteger(0);
    
    //this is called by single thread.
    private int connectionCount = 0;
    private ConnectionActivity[] connectionActivityTable;
    
    protected Call(String id, MgcpController controller) {
        this.controller = controller;
        this.id = id;
        this.connectionActivityTable = new ConnectionActivity[_CONNECTION_TAB_SIZE];
    }
    
    public String getID() {
        return id;
    }
    
    public ConnectionActivity addConnection(Connection connection) {
        ConnectionActivity l = new ConnectionActivity(this, connection);
        Utils.addObject(this.connectionActivityTable, l);
        return l;
    }
    
    
    public void removeConnection(ConnectionActivity connectionActivity) {
		if(Utils.removeObject(this.connectionActivityTable, connectionActivity))
		{
			this.connectionCount--;
			if(this.connectionCount == 0)
			{
				this.controller.removeCall(this);
			}
		}
		
	}

    /**
     * Do not modify returned value!!!
     */
    public ConnectionActivity[] getActivities()
    {
    	return this.connectionActivityTable;
    }
	public void setCallTableIndex(int index) {
		this.callTableIndex = index;
	}

	public int getCallTableIndex() {
		return this.callTableIndex;
	}

	
}
