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
package org.mobicents.protocols.link;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.log4j.Logger;
import org.mobicents.protocols.stream.api.SelectorKey;
import org.mobicents.protocols.stream.api.SelectorProvider;
import org.mobicents.protocols.stream.api.Stream;
import org.mobicents.protocols.stream.api.StreamSelector;

/**
 *
 * @author kulikov
 */
public class DataLink implements Stream {

    private final static int BUFF_SIZE = 8192;
    
    /* Datagram channel */
    protected DatagramChannel channel;
    
    /** Link state  and state listener */
    protected LinkState state = LinkState.NULL;
    private LinkStateListener listener;
    
    /** RX and TX buffers */
    private ByteBuffer rxBuffer = ByteBuffer.allocateDirect(BUFF_SIZE);
    private ByteBuffer txBuffer = ByteBuffer.allocateDirect(BUFF_SIZE);
    
    private ConcurrentLinkedQueue<PDU> txQueue = new ConcurrentLinkedQueue();
    private ConcurrentLinkedQueue<byte[]> rxQueue = new ConcurrentLinkedQueue();
    
    /** next available sequence number */
    private int seq;
    
    /** flag indicating that stream is available for writting */
    protected boolean isWritable;
    /** flag indicating that stream is available for reading */
    protected boolean isReadbale;

    /** PDU currently transmitted */
    private PDU sendPDU;
    
    /** Last acknowledged sequence number */
    private int lastAcked;
    
    private long sentTime;
    private long rtr;
    
    /* Logger instance */
    private static final Logger logger = Logger.getLogger(DataLink.class);
    
    protected DataLink(InetSocketAddress address, InetSocketAddress remote) throws IOException {
        //create and configure datagram channel
        channel = DatagramChannel.open();
        channel.configureBlocking(false);

        //bind it to local address and connect to remote address 
        channel.socket().bind(address);
        logger.info("Link " + address + " state = " + state + ", is bound to " + address);

        channel.connect(remote);
        setState(LinkState.INACTIVE);
        logger.info("Link " + address + " state = " + state + ", is connected to " + remote);
    }

    public static DataLink open(InetSocketAddress address, InetSocketAddress remote) throws IOException {
        return new DataLink(address, remote);
    }

    public void activate() {
        setState(LinkState.ACTIVATING);
    }

    public void deactivate() {
        setState(LinkState.INACTIVE);
    }

    public void setListener(LinkStateListener listener) {
        this.listener = listener;
    }

    protected boolean isConnected() {
        return channel.isConnected();
    }

    public SelectorKey register(StreamSelector selector) throws IOException {
        return ((SelectorImpl) selector).register(this);
    }

    public LinkState getState() {
        return state;
    }

    public int read(byte[] b) throws IOException {
        //if state is no active then ignore call
        if (state != LinkState.ACTIVE) {
            return 0;
        }

        //if rxQueue is empty then nothing to read
        if (rxQueue.isEmpty()) {
            return 0;
        }
        
        //poll next packet from queue and return its length
        byte[] packet = rxQueue.poll();        
        System.arraycopy(packet, 0, b, 0, packet.length);
        
        return packet.length;
    }

    public int write(byte[] d) throws IOException {
        //if link is not active ignore call
        if (state != LinkState.ACTIVE) {
            return 0;
        }

        //wrap data with PDU
        sendPDU = new PDU(PDU.DATA, seq, d);
        
        //write PDU to the tx buffer
        sendPDU.write(txBuffer);
        
        //send data
        channel.write(txBuffer);
        
        sentTime = System.currentTimeMillis();
        rtr = 0;
        
        if (logger.isDebugEnabled()) {
            logger.debug("Link " + channel.socket().getLocalSocketAddress() + ", state=" + state + ")  ---> " + sendPDU);
        }
        return d.length;
    }

    private void reset() {
        seq = 0;
        rxQueue.clear();
        txQueue.clear();
    }
    
    protected boolean processRx() throws IOException {
        if (!channel.isConnected()) {
            return false;
        }
        //clean rxQueue and discard all packets which was not written
        rxQueue.clear();
        
        //reading data
        int len = 1;

        while (len > 0) {
            try {
                len = channel.read(rxBuffer);
            } catch (IOException e) {
                len = 0;
            }
            rxBuffer.flip();

            //check length
            if (len <= 0) {
                //clean buffer and exit
                rxBuffer.clear();
                break;
            }

            //creating PDU
            PDU pdu = new PDU(rxBuffer, len);
            if (logger.isDebugEnabled()) {
                logger.debug("Link " + channel.socket().getLocalSocketAddress()  + ", state=" + state + ")  <---- " + pdu);
            }
            
            switch (state) {
                case INACTIVE:
                    //Valid packets are: ACTIVATING
                    switch (pdu.getType()) {
                        //The other end tries to activate
                        //switch state at this end to active and send ack
                        case PDU.ACTIVATING:
                            setState(LinkState.ACTIVE);
                            reset();
                            //scheduling at least one ACTIVATING PDU in response
                            PDU ack = new PDU(PDU.ACTIVATING, 0, null);
                            txQueue.offer(ack);
                            break;
                        default:
                        //ignore all other packets
                    }
                    break;
                case ACTIVATING:
                    //Expected packets are: ACTIVATING
                    switch (pdu.getType()) {
                        //switch state to active and ack received packet
                        case PDU.ACTIVATING:
                            setState(LinkState.ACTIVE);
                            reset();
                            //scheduling at least one ACTIVATING PDU in response
                            PDU ack = new PDU(PDU.ACTIVATING, 0, null);
                            txQueue.offer(ack);
                            break;
                    }
                //Expected packets: DATA, ACK, OS    
                case ACTIVE:
                    switch (pdu.getType()) {
                        case PDU.DATA:
                            //TODO handle RTR
                            rxQueue.offer(pdu.getPayload());

                            //scheduling acks
                            PDU ack = new PDU(PDU.ACK, pdu.getSeq(), null);
                            txQueue.offer(ack);
                            
                            //store seq of last acknowledged PDU
                            lastAcked = pdu.getSeq();
                            break;
                        case PDU.ACK:
                            //check seq number
                            if (pdu.isRTR() && pdu.getSeq() == lastAcked) {
                                System.out.println("IGNORE====");
                                //acknowledgement already sent and packet received
                                break;
                            }
                            if (pdu.getSeq() == seq) {
                                //last sent message is acknowledged
                                sendPDU = null;
                                lastAcked = pdu.getSeq();
                                //increment sequence number
                                seq++;
                                
                                //allow to write next packet
                                isWritable = true;
                            } else {
                                logger.info("Link " + channel.socket().getLocalSocketAddress() + " state = " + state + " receive ack, wrong seq!!! " + seq);
                            }
                            break;
                        case PDU.OS :
                            setState(LinkState.INACTIVE);
                            break;
                    }
                    break;
            }

            rxBuffer.clear();
        }
        rxBuffer.clear();
        return !rxQueue.isEmpty();
    }

    private void fillTxQueue() {
        switch (state) {
            case INACTIVE:
                txQueue.offer(new PDU(PDU.OS, 0, null));
                break;
            case ACTIVATING:
                txQueue.offer(new PDU(PDU.ACTIVATING, seq, null));
                break;
            case ACTIVE:
//                pdu = txQueue.poll();
                break;
        }
    }

    private boolean timedout() {
        long now = System.currentTimeMillis();
        return now - sentTime > 1000;
    }
    
    protected void processTX() throws IOException {
        //initialy try to resend last pdu if it is not null (needs to be rentransmitted)
        if (sendPDU == null) {
            if (state == LinkState.ACTIVE) {
                isWritable = true;
            }
        } else if (sendPDU != null && timedout() & rtr < 3 & state == LinkState.ACTIVE ) {
            //TODO handle max RTRs
            sendPDU.setRetransmission();
            sendPDU.write(rxBuffer);
            channel.write(txBuffer);
            if (logger.isDebugEnabled()) {
                logger.debug("(Link " + channel.socket().getLocalSocketAddress() + ", state=" + state + ") ---> " + sendPDU);
            }
            rtr++;
        } else {
            sendPDU = null;
            if (state == LinkState.ACTIVE) {
                isWritable = true;
            }
        }
        
        //fill with metadata if required
        if (txQueue.isEmpty()) {
            fillTxQueue();
        }

        //send all the queue
        while (!txQueue.isEmpty()) {
            PDU pdu = txQueue.poll();

            pdu.write(txBuffer);
            channel.write(txBuffer);
            if (logger.isDebugEnabled()) {
                logger.debug("Link " + channel.socket().getLocalSocketAddress()  + ", state=" + state + ")  ---> " + pdu);
            }
        }        
    }

    public boolean isReadable() {
        return this.isReadbale;
    }

    public boolean isWriteable() {
        return this.isWritable;
    }

    public void close() {
        try {
        	channel.socket().disconnect();
        	channel.socket().close();
        
        } catch (Exception e) {
        	e.printStackTrace();
        }
        try {
        	
            channel.close();
        } catch (IOException e) {
        	e.printStackTrace();
        }
    }

    public SelectorProvider provider() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected void setState(LinkState state) {
        logger.info("Link " + channel.socket().getLocalSocketAddress()  + ", state=" + state + ")");
        this.state = state;
        if (listener != null) {
            listener.onStateChange(state);
        }
    }
}
