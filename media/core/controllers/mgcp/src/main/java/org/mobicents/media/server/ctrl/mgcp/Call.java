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
	
	protected MgcpController getController(){
		return this.controller;
	}

	
}
