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

package org.mobicents.media.server.spi;

import org.mobicents.media.MediaSource;
import org.mobicents.media.server.spi.clock.Task;
import org.mobicents.media.server.spi.clock.TimerTask;

/**
 * The component which is a source of synchronization for other components.
 * 
 * The example of source of synchronization is a Timer. The another example is a source component 
 * which is synchronized from stream.
 * 
 * @author kulikov
 */
public interface SyncSource {
    /**
     * Synchronize the process of media content processing from this timer.
     * 
     * @param mediaSource the media sources to synchronize.
     */
    public void sync(MediaSource mediaSource);
    
    public TimerTask sync(Task task);
    /**
     * Disable synchronization process.
     * 
     * @param mediaSource the media source component.
     */
    public void unsync(MediaSource mediaSource);
    
    /**
     * Gets the absolute timestamp value.
     * 
     * @return the timestamp value in milliseconds.
     */
    public long getTimestamp(); 
        
    /**
     * Starts this component.
     */
    public void start();
    
    /**
     * Stops this component.
     */
    public void stop();

}
