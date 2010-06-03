/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
