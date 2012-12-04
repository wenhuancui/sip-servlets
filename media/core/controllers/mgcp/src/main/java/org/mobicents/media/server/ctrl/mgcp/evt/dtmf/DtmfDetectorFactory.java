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

package org.mobicents.media.server.ctrl.mgcp.evt.dtmf;

import jain.protocol.ip.mgcp.message.parms.RequestedAction;
import org.mobicents.media.server.ctrl.mgcp.evt.DetectorFactory;
import org.mobicents.media.server.ctrl.mgcp.evt.EventDetector;
import org.mobicents.media.server.ctrl.mgcp.evt.MgcpPackage;
import org.mobicents.media.server.spi.MediaType;
import org.mobicents.media.server.spi.events.NotifyEvent;
import org.mobicents.media.server.spi.resource.DtmfDetector;

/**
 *
 * @author kulikov
 */
public class DtmfDetectorFactory implements DetectorFactory {

    private String name;
    private MgcpPackage mgcpPackage;

    private int eventID;
    
    public String getEventName() {
        return this.name;
    }

    public MgcpPackage getPackage() {
        return this.mgcpPackage;
    }

    public void setEventName(String eventName) {
        this.name = eventName;
    }

    public void setPackage(MgcpPackage mgcpPackage) {
        this.mgcpPackage = mgcpPackage;
    }

    public int getEventID() {
        return eventID;
    }

    public void setEventID(int eventID) {
        this.eventID = eventID;
    }
    
    public EventDetector getInstance(String params, RequestedAction[] actions,
            Class interface1, MediaType type) {
        return new MgcpDtmfEvent(this.mgcpPackage, name, eventID, params, actions, DtmfDetector.class);
    }

    private class MgcpDtmfEvent extends EventDetector {

        public MgcpDtmfEvent(MgcpPackage mgcpPackage, String eventName, int eventID, String params,
            RequestedAction[] actions, Class interface1) {
            super(mgcpPackage, eventName, eventID, params, actions);
            this.setDetectorInterface(interface1);
            this.setMediaType(MediaType.AUDIO);
        }
        
        @Override
        public void start() {
            super.start();
            ((DtmfDetector) component).start();
        }
        
        @Override
        public void performAction(NotifyEvent event, RequestedAction action) {
            getRequest().sendNotify(this.getEventName());
            //((DtmfDetector) component).stop();
        }
    }
}
