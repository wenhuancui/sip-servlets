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

import org.mobicents.media.server.spi.MediaType;

import jain.protocol.ip.mgcp.message.parms.RequestedAction;

/**
 *
 * @author kulikov
 */
public class DefaultDetectorFactory implements DetectorFactory {

    private MgcpPackage mgcpPackage;
    private String name;

    private int eventID;

    public MgcpPackage getPackage() {
        return mgcpPackage;
    }

    public void setPackage(MgcpPackage mgcpPackage) {
        this.mgcpPackage = mgcpPackage;
    }
        
    public void setEventName(String name) {
        this.name = name;
    }
    
    public String getEventName() {
        return name;
    }

    public int getEventID() {
        return eventID;
    }

    public void setEventID(int eventID) {
        this.eventID = eventID;
    }
        
    //public EventDetector getInstance(String params, RequestedAction[] actions) {
    //    return new DefaultEventDetector(packageName, name, resourceName, eventID, params, actions);
    //}

	public EventDetector getInstance(String params, RequestedAction[] actions,
			Class interface1, MediaType type) {
		return new DefaultEventDetector(this.mgcpPackage, name, eventID, params, actions,interface1,type);
	}

}
