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
package org.mobicents.media.server.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.log4j.Logger;
import org.mobicents.media.Buffer;
import org.mobicents.media.Format;
import org.mobicents.media.Inlet;
import org.mobicents.media.MediaSink;
import org.mobicents.media.MediaSource;
import org.mobicents.media.server.spi.SyncSource;
import org.mobicents.media.server.spi.events.NotifyEvent;

/**
 * The base implementation of the Media source.
 * 
 * <code>AbstractSource</code> and <code>AbstractSink</code> are implement general wirring contruct. All media
 * components have to extend one of these classes.
 * 
 * @author Oleg Kulikov
 */
public abstract class AbstractSource extends BaseComponent implements MediaSource, Runnable {

    protected transient MediaSink otherParty;
    private SyncSource syncSource;
    private ReentrantLock state = new ReentrantLock();
    private long sequenceNumber = 1;
    private long packetsTransmitted;
    private long bytesTransmitted;
    private volatile boolean started;
    private NotifyEvent evtStarted;
    private NotifyEvent evtCompleted;
    private NotifyEvent evtStopped;
    protected Logger logger;
    /** packetization period in millseconds */
    private int period = 20;
    private volatile long timestamp = -1;
//    private volatile long duration;
    private int frameRate = 50;
    private ArrayList<Buffer> buffers = new ArrayList();
    /**
     * Creates new instance of source with specified name.
     * 
     * @param name
     *            the name of the sink to be created.
     */
    public AbstractSource(String name) {
        super(name);
        logger = Logger.getLogger(getClass());
        evtStarted = new NotifyEventImpl(this, NotifyEvent.STARTED);
        evtCompleted = new NotifyEventImpl(this, NotifyEvent.COMPLETED);
        evtStopped = new NotifyEventImpl(this, NotifyEvent.STOPPED);
    }

    /**
     * (Non Java-doc).
     * 
     * @see org.mobicents.media.MediaSource#getSyncSource()
     */
    public SyncSource getSyncSource() {
        return syncSource;
    }

    /**
     * (Non Java-doc).
     * 
     * @see org.mobicents.media.MediaSource#setSyncSource(SyncSource)
     */
    public void setSyncSource(SyncSource syncSource) {
        this.syncSource = syncSource;
    }

    /**
     * (Non Java-doc).
     * 
     * @see org.mobicents.media.MediaSource#getPeriod() 
     */
    public int getPeriod() {
        return period;
    }

    /**
     * (Non Java-doc).
     * 
     * @see org.mobicents.media.MediaSource#setPeriod(int) 
     */
    public void setPeriod(int period) {
        this.period = period;
    }

    public void setFrameRate(int frameRate) {
        this.frameRate = frameRate;
    }

    public int getFrameRate() {
        return frameRate;
    }

    protected int getFrameCount() {
        return period / (1000 / frameRate);
    }

    /**
     * This method is called just before start.
     * 
     * The descendant classes can verride this method and put additional logic
     */
    protected void beforeStart() throws Exception {
    }

    /**
     * (Non Java-doc).
     * 
     * @see org.mobicents.media.MediaSource#start().
     */
    public void start() {
        state.lock();
        try {
            if (started) {
                return;
            }
            
            started = true;
            timestamp = -1;
            sequenceNumber = 0;
            
            beforeStart();

            // synchronize 
            if (syncSource == null) {
                throw new IllegalStateException("No source of synchronization: " + this);
            }

            if (otherParty != null && !otherParty.isStarted()) {
                otherParty.start();
            }

            //obtain current timestamp and schedule periodic execution
            //timestamp = syncSource.getTimestamp();
            syncSource.sync(this);

            //started!
            if (logger.isDebugEnabled()) {
                logger.debug(this + " started, period =" + period + ", format=" + format);
            }
            started();
        } catch (Exception e) {
            e.printStackTrace();
            started = false;
            failed(NotifyEvent.START_FAILED, e);
        } finally {
            state.unlock();
        }
    }

    /**
     * (Non Java-doc).
     * 
     * @see org.mobicents.media.MediaSource#stop().
     */
    public void stop() {
        state.lock();
        try {
            started = false;
            syncSource.unsync(this);
            if (logger.isDebugEnabled()) {
                logger.debug(this + " stopped");
            }

            if (otherParty != null && otherParty.isStarted()) {
                otherParty.stop();
            }

            stopped();
            afterStop();
            timestamp = -1;
        } finally {
            state.unlock();
        }
    }

    /**
     * This method is called immediately after processing termination.
     * 
     */
    public void afterStop() {
    }

    public boolean isMultipleConnectionsAllowed() {
        return false;
    }

    /**
     * This methods is called by media sink to setup preffered format.
     * Media source in opposite direction can ask media sink to get 
     * preffered format by calling <code>sink.getPreffred(Collection<Format>)</code>
     * where collection is a subset of common formats.
     * 
     * @param format preffred format.
     */
    public void setPreffered(Format format) {
        this.format = format;
    }

    /**
     * (Non Java-doc).
     * 
     * @see org.mobicents.media.MediaSource#connect(MediaSink).
     */
    public void connect(MediaSink otherParty) {
        state.lock();
        try {
            // we can not connect with null
            if (otherParty == null) {
                throw new IllegalArgumentException("Other party can not be null");
            }

            //this implemntation suppose that other party is instance of AbstractSink
            if (!(otherParty instanceof AbstractSink)) {
                throw new IllegalArgumentException("Can not connect: " +
                        otherParty + " does not extends AbstractSink");
            }

            //if other party allows multiple connection (like mixer or mux/demux
            //we should delegate connection procedure to other party because other party
            //maintances internal components
            if (otherParty.isMultipleConnectionsAllowed()) {
                otherParty.connect(this);
                return;
            }

            //if we are here then this is the most common case when we are joining 
            //two components
            AbstractSink sink = (AbstractSink) otherParty;

            //calculating common formats
            Collection<Format> subset = this.subset(getFormats(), otherParty.getFormats());

            //connection is possible if and only if both components have common formats
            if (subset.isEmpty()) {
                throw new IllegalArgumentException("Format missmatch");
            }

            //now we have to ask sink to select preffered format
            //if sink can not determine preffred format at this time it will return null
            //and from now the sink is responsible for assigning preffred format to this sink
            Format preffered = sink.getPreffered(subset);
            if (preffered != null) {
                setPreffered(preffered);
            }

            //creating cross refernces to each other
            sink.otherParty = this;
            this.otherParty = sink;


            if (logger.isDebugEnabled()) {
                logger.debug(this + " is connected to " + otherParty);
            }
        } finally {
            state.unlock();
        }
    }

    /**
     * (Non Java-doc).
     * 
     * @see org.mobicents.media.MediaSource#diconnection(MediaSink).
     */
    public void disconnect(MediaSink otherParty) {
        state.lock();
        try {
            // check argument
            if (otherParty == null) {
                throw new IllegalArgumentException("Other party can not be null");
            }

            //this implementation suppose to work with AbstractSink
            if (!(otherParty instanceof AbstractSink)) {
                throw new IllegalArgumentException("Can not disconnect: " + otherParty + " is not connected");
            }

            //if other party allows multiple connections then we have to deligate call to other party
            if (otherParty.isMultipleConnectionsAllowed()) {
                otherParty.disconnect(this);
                return;
            }

            //in this case we are checking that other party is connected to this component
            if (otherParty != this.otherParty) {
                throw new IllegalArgumentException("Can not disconnect: " + otherParty + " is not connected");
            }

            //indeed the other party is connected so we can break this connection 
            //by removing cross reference and cleaning formats
            AbstractSink sink = (AbstractSink) otherParty;

            //cleaning formats
            setPreffered(null);
            sink.getPreffered(null);
            
            //cleaning references
            sink.otherParty = null;
            this.otherParty = null;
        } finally {
            state.unlock();
        }
    }

    public void connect(Inlet inlet) {
        connect(inlet.getInput());
    }

    public void disconnect(Inlet inlet) {
        disconnect(inlet.getInput());
    }

    /**
     * (Non Java-doc).
     * 
     * @see org.mobicents.media.MediaSink#isConnected().
     */
    public boolean isConnected() {
        return otherParty != null;
    }

    /**
     * (Non Java-doc).
     * 
     * @see org.mobicents.media.MediaSource#isStarted().
     */
    public boolean isStarted() {
        return this.started;
    }

    /**
     * This method must be overriden by concrete media source. The media have to fill buffer with media data and
     * attributes.
     * 
     * @param buffer the buffer object for media.
     * @param sequenceNumber
     *            the number of timer ticks from the begining.
     */
    public abstract void evolve(Buffer buffer, long timestamp, long sequenceNumber);

    protected String getSupportedFormatList() {
        String s = "";
        if (otherParty != null) {
            Format[] formats = otherParty.getFormats();
            for (int i = 0; i < formats.length; i++) {
                s += formats[i] + ";";
            }
        }
        return s;
    }

    public void run() {
        buffers.clear();
        //determine curent time using synchronization source
        long now = syncSource.getTimestamp();
        if (timestamp == -1) {
            timestamp = now;
        }
        //we should not process task if:
        //1. this is not first run (timestamp > 0)
        //2. it is too early
        if (timestamp > 0 && timestamp > now) {
            return;
        }
        
        long duration = 0;
        try {
            //filling media buffer
            Buffer buffer = null;
            do {
                //it is a time to process media data. let's go
                //preparing media buffer 
                buffer = new Buffer();
                buffer.setFormat(format);
                
                //fill buffer
                evolve(buffer, now, sequenceNumber);                
                
                buffers.add(buffer);
                duration = buffer.getDuration();
            } while (duration == -1);
        } catch (Exception e) {
            logger.error(this + "Error during data evolving", e);
            this.failed(NotifyEvent.TX_FAILED, e);
            return;
        }

        long t = timestamp;
        timestamp += duration;        
        //now we have to schedule next processing. 
        //we are using duration of this packet to schedule next run
        for (Buffer buffer : buffers) {
            buffer.setTimeStamp(t);
            buffer.setSequenceNumber(sequenceNumber++);
            
            //generate COMPLETED event if end of media reached.
            if (buffer.isEOM()) {
                completed();
            }
            
            //if media buffer is empty, then nothing to deliver to consumer
            if (buffer.getLength() == 0) {
                return;
            }
            //if buffer is marked as discarde we should not send it to consumer
            if (buffer.isDiscard()) {
                return;
            }
        
        
            if (logger.isDebugEnabled()) {
                logger.debug(this + " sending " + buffer + " to " + otherParty);
            }
       
            //let's do final check because other thread can disconect parties.
            if (otherParty == null) {
                return;
            }
            //delivering data to the other party.
            try {
                otherParty.receive(buffer);
            } catch (Exception e) {
                logger.error("Can not deliver packet to " + otherParty, e);
                failed(NotifyEvent.TX_FAILED, e);
            }

            //at the final step we are incrementing stats
            packetsTransmitted++;
            bytesTransmitted += buffer.getLength();
        }
    }

    /**
     * Sends notification that media processing has been started.
     */
    protected void started() {
        sendEvent(evtStarted);
    }

    /**
     * Sends failure notification.
     * 
     * @param eventID
     *            failure event identifier.
     * @param e
     *            the exception caused failure.
     */
    protected void failed(int eventID, Exception e) {
        FailureEventImpl failed = new FailureEventImpl(this, eventID, e);
        syncSource.unsync(this);
        sendEvent(failed);
    }

    /**
     * Sends notification that signal is completed.
     * 
     */
    protected void completed() {
        syncSource.unsync(this);
        System.out.println("Stopping " + this);
        this.started = false;
        sendEvent(evtCompleted);
    }

    /**
     * Sends notification that detection is terminated.
     * 
     */
    protected void stopped() {
        sendEvent(evtStopped);
    }

    /**
     * (Non Java-doc).
     * 
     * @see org.mobicents.media.MediaSource#getPacketsReceived()
     */
    public long getPacketsTransmitted() {
        return packetsTransmitted;
    }

    /**
     * (Non Java-doc).
     * 
     * @see org.mobicents.media.MediaSource#getBytesTransmitted()
     */
    public long getBytesTransmitted() {
        return bytesTransmitted;
    }

    @Override
    public void resetStats() {
        this.packetsTransmitted = 0;
        this.bytesTransmitted = 0;
    }
}
