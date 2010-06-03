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
package org.mobicents.media.server.impl.clock;


import java.util.HashMap;
import org.mobicents.media.MediaSource;
import org.mobicents.media.server.spi.clock.Task;
import org.mobicents.media.server.spi.clock.Timer;
import org.mobicents.media.server.spi.clock.TimerTask;



/**
 * Provides repited execution at a reqular time intervals.
 * 
 * @author Oleg Kulikov
 */
public class TimerImpl implements Timer {

    private Scheduler scheduler = new Scheduler();
    private HashMap<String, LocalTask> tasks = new HashMap();
    /**
     * Creates new instance of the timer.
     */
    public TimerImpl() {
    }

    /**
     * Gets value of interval between timer ticks.
     * 
     * @return the int value in milliseconds.
     */
    public int getHeartBeat() {
        return 1;
    }

    /**
     * Modify interval between timer tick
     * 
     * @param heartBeat
     *            the new value of interval in milliseconds.
     */
    public void setHeartBeat(int heartBeat) {
    }

    /**
     * (Non Java-doc.)
     * 
     * @see org.mobicents.media.server.spi.Timer#getTimestamp() 
     */
    public long getTimestamp() {
        return System.currentTimeMillis();
    }

    /**
     * (Non Java-doc.)
     * 
     * @see org.mobicents.media.server.spi.Timer#sync(org.mobicents.media.MediaSource) 
     */
    public void sync(MediaSource mediaSource) throws IllegalArgumentException {
        LocalTask task = scheduler.execute(mediaSource);
        tasks.put(mediaSource.getId(), task);
    }

    public TimerTask sync(Task task) {
        return scheduler.execute(task);
    }
    /**
     * (Non Java-doc.)
     * 
     * @see org.mobicents.media.server.spi.Timer#unsync(org.mobicents.media.MediaSource) 
     */
    public void unsync(MediaSource mediaSource) {
        LocalTask task = tasks.remove(mediaSource.getId());
        if (task != null) {
            task.cancel();
        }
    }
    
    /**
     * (Non Java-doc.)
     * 
     * @see org.mobicents.media.server.spi.Timer#start() 
     */
    public void start() {
        scheduler.start();
    }
    
    /**
     * (Non Java-doc.)
     * 
     * @see org.mobicents.media.server.spi.Timer#stop() 
     */
    public void stop() {
        scheduler.stop();
    }
    
}

