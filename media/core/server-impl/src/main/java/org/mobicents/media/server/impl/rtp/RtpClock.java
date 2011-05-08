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

package org.mobicents.media.server.impl.rtp;

import org.mobicents.media.Format;

/**
 *
 * @author kulikov
 */
public abstract class RtpClock {
    private Format format;
    
    protected long now;
    private boolean isSynchronized;
    
    public RtpClock() {
    }
    
    public Format getFormat() {
        return format;
    }
    
    public void setFormat(Format format) {
        this.format = format;
    }
    
    public void synchronize(long initial) {
        now = initial;
        this.isSynchronized = true;
    }
    
    public boolean isSynchronized() {
        return this.isSynchronized();
    }
    
    protected long now() {
        return now;
    }
    
    public void reset() {
        now = 0;
        this.isSynchronized = false;
        this.format = null;
    }
    
    /**
     * Returns the time in milliseconds
     * 
     * @param timestamp the rtp timestamp
     * @return the time in milliseconds
     */
    public abstract long getTime(long timestamp);
    
    /**
     * Calculates RTP timestamp
     * 
     * @param time the time in milliseconds
     * @return rtp timestamp.
     */
    public abstract long getTimestamp(long time);
}
