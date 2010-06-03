/*
 * Mobicents, Communications Middleware
 * 
 * Copyright (c) 2008, Red Hat Middleware LLC or third-party
 * contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Middleware LLC.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 *
 * Boston, MA  02110-1301  USA
 */
package org.mobicents.media.server.impl.clock;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.mobicents.media.MediaSource;
import org.mobicents.media.server.spi.Timer;



/**
 * Provides repited execution at a reqular time intervals.
 * 
 * @author Oleg Kulikov
 */
public class TimerImpl implements Timer {

    public static final int _DEFAULT_T_PRIORITY = Thread.MAX_PRIORITY;
    
    private transient final ScheduledExecutorService timer = 
            Executors.newSingleThreadScheduledExecutor(new MMSClockThreadFactory());    
    private int heartBeat = 20;
    private volatile ConcurrentHashMap<String, ScheduledFuture> controls = new ConcurrentHashMap();
    
    private volatile long timestamp;
    private Clock clock = new Clock();
    private ScheduledFuture clockControl;
    
    private ConcurrentHashMap<String, Task> tasks = new ConcurrentHashMap();
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
        return heartBeat;
    }

    /**
     * Modify interval between timer tick
     * 
     * @param heartBeat
     *            the new value of interval in milliseconds.
     */
    public void setHeartBeat(int heartBeat) {
        this.heartBeat = heartBeat;
    }

    /**
     * (Non Java-doc.)
     * 
     * @see org.mobicents.media.server.spi.Timer#getTimestamp() 
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * (Non Java-doc.)
     * 
     * @see org.mobicents.media.server.spi.Timer#sync(org.mobicents.media.MediaSource) 
     */
    public void sync(MediaSource mediaSource) throws IllegalArgumentException {
/*        if (mediaSource.getPeriod() > 0) {
            //period have to be multiple to timer's heard beat.
            //so we are recalculating the actual packetization period.
            int period = (mediaSource.getPeriod() / heartBeat) * heartBeat;
            ScheduledFuture control = timer.scheduleAtFixedRate(mediaSource, 0, period, TimeUnit.MILLISECONDS);
            controls.put(mediaSource.getId(), control);
        } else throw new IllegalArgumentException(mediaSource + " can not be synchronized from this source");
 * */
        Task task = new Task(mediaSource);
        tasks.put(task.id, task);
    }

    /**
     * (Non Java-doc.)
     * 
     * @see org.mobicents.media.server.spi.Timer#unsync(org.mobicents.media.MediaSource) 
     */
    public void unsync(MediaSource mediaSource) {
        tasks.remove(mediaSource.getId());
/*        ScheduledFuture control = controls.remove(mediaSource.getId());
        if (control != null) {
            control.cancel(false);
        }
 */
    }
    
    /**
     * (Non Java-doc.)
     * 
     * @see org.mobicents.media.server.spi.Timer#start() 
     */
    public void start() {
        if (clockControl == null || clockControl.isCancelled()) {
            clockControl = timer.scheduleAtFixedRate(clock, heartBeat, heartBeat, TimeUnit.MILLISECONDS);
        }
    }
    
    /**
     * (Non Java-doc.)
     * 
     * @see org.mobicents.media.server.spi.Timer#stop() 
     */
    public void stop() {
        if (clockControl != null && !clockControl.isCancelled()) {
            clockControl.cancel(false);
        }
    }
    
    private void notifyTasks() {
        Collection<Task> queue = tasks.values();
        for (Task task : queue) {
            task.source.run();
        }
    }
    
    private class Clock implements Runnable {
        public void run() {
            timestamp += heartBeat;
            notifyTasks();
        }
    }
    
    private class Task {
        protected String id;
        protected int period;
        protected long timestamp;
        protected MediaSource source;
        
        public Task(MediaSource source) {
            this.source = source;
            this.id = source.getId();
            this.period = source.getPeriod();
        }
        
        public void update(int period) {
            this.period = period;
        }
    }
}

class MMSClockThreadFactory implements ThreadFactory {

    public static final AtomicLong sequence = new AtomicLong(0);
    private ThreadGroup factoryThreadGroup = new ThreadGroup("MMSClockThreadGroup[" + sequence.incrementAndGet() + "]");

    public Thread newThread(Runnable r) {
        Thread t = new Thread(this.factoryThreadGroup, r);
        t.setPriority(TimerImpl._DEFAULT_T_PRIORITY);
        // ??
        //t.start();
        return t;
    }
}