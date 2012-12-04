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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import org.apache.log4j.Logger;
import org.mobicents.media.Server;
import org.mobicents.media.server.spi.clock.Task;
import org.mobicents.media.server.spi.clock.TimerTask;

/**
 *
 * @author kulikov
 */
public class Receiver implements Task {

    /** Poll period measured in milliseconds */
    public final static int POLL_PERIOD = 20;
    
    /** Channel selector */
    private Selector selector;
    
    /** UDP buffer definition */
    private int bufferSize = 8196;
    private ByteBuffer readBuffer = ByteBuffer.allocateDirect(bufferSize);
    
    /** Flag, indicates the state of Receiver*/
    private volatile boolean started;
    
    /** RTP Manager, running this receiver. */
    private RtpFactory factory;
    
    /** Logger instance */
    private Logger logger = Logger.getLogger(Receiver.class);

    /** Scheduler manager*/
    private TimerTask worker;
    
    /**
     * Creates new instance of Receiver.
     * 
     * @param factory RTP manager running this receiver.
     * @throws java.io.IOException
     */
    public Receiver(RtpFactory factory) throws IOException {
        this.factory = factory;
        this.selector = SelectorProvider.provider().openSelector();
    }

    /**
     * Gets datagram channel selector.
     * 
     * @return selector instance.
     */
    public Selector getSelector() {
        return selector;
    }

    /**
     * Starts receiver.
     */
    public void start() {
        started = true;
        worker = Server.scheduler.execute(this);
    }

    /**
     * Stops receiver.
     */
    public void stop() {
        worker.cancel();
        started = false;
        try {
            selector.close();
        } catch (IOException e) {
        }
    }

    /**
     * Stops receiver.
     */
    public void cancel() {
        stop();
    }

    /**
     * Shows the state of the receiver.
     * 
     * @return true if receiver is started and running and false if receiver is stopped.
     */
    public boolean isActive() {
        return started;
    }

    /**
     * Perform IO operations and operation which depends from IO.
     * 
     * @return the intyerval in milliseconds for reschedule.
     */
    public int perform() {
        //skip everything if receiver is stopped
        if (!started) {
            //this task wont more scheduled
            return -1;
        }
        
        //select channels ready for IO
        try {
            selector.selectNow();
        } catch (IOException e) {
            //notify rtp manager that some unexpected IO error has occured
            factory.notify(e);
            //disable receiver
            return -1;
        }
        
        //getting list of selected channels
        Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
        while (keys.hasNext()) {
            //take channel from list
            SelectionKey key = keys.next();
            keys.remove();
            
            //get references to channel and associated RTP socket
            DatagramChannel channel = (DatagramChannel) key.channel();
            RtpSocketImpl socket = (RtpSocketImpl) key.attachment();
            
            //consider channels ready for READ only
            if (!key.isReadable()) {
                continue;
            }
            
            //release and skip closed sockets
            if (socket.isClosed()) {
                socket.close();
                continue;
            }
            
            //now read ALL datagrams from buffer!
            int len = 1;
            while (len > 0) {
                try {
                    //reading datagram
                    if (socket.isClosed()) {
                        len = 0;
                        continue;
                    }
                    len = channel.read(readBuffer);
                    readBuffer.flip();
                    
                    //break loop if nothing read
                    if (len <= 0) {
                        readBuffer.clear();
                        break;
                    }
                    
                    //convert datagram into RTP packet and process it 
                    RtpPacket rtpPacket = new RtpPacket(readBuffer);
                    socket.receive(rtpPacket);
                    readBuffer.clear();
                } catch (IOException e) {
                    //TODO count error before failing
                }
            }
        }
        
        //registering awaiting channels 
        factory.register();
        
        //reschedule task
        return POLL_PERIOD;
    }
    
}
