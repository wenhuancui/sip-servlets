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

package org.mobicents.media.server.resource;

import org.mobicents.media.server.spi.Valve;

/**
 * Used as a factory class for creating inner pipes dynamic upon request
 * using assigned inlet's and outlet's factrories;
 * 
 * @author kulikov
 */
public class PipeFactory {
    
    private String inlet;
    private String outlet;
    private Valve valve;
    
    public String getInlet() {
        return inlet;
    }

    public String getOutlet() {
        return outlet;
    }

    public void setInlet(String inlet) {
        this.inlet = inlet;
    }

    public void setOutlet(String outlet) {
        this.outlet = outlet;
    }

    public Valve getValve() {
        return valve;
    }
    
    public void setValve(Valve valve) {
        this.valve = valve;
    }
    
    public void openPipe(Channel channel) throws UnknownComponentException {
        Pipe pipe = new Pipe(channel);
        pipe.setValve(this.valve);
        channel.openPipe(pipe, inlet, outlet);
    }
}
