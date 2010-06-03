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
package org.mobicents.media.server.impl.rtp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import java.util.Collection;

import org.mobicents.media.Format;
import org.mobicents.media.MediaSource;
import org.mobicents.media.server.impl.rtp.sdp.AVProfile;
import org.mobicents.media.server.spi.Timer;
import org.mobicents.media.server.spi.dsp.Codec;

/**
 * 
 * @author Oleg Kulikov
 */
public class RtpSocket {

    protected String media;
    
    //local address and port pair
    private String localAddress;
    private int localPort;
    
    //remote address and port pair
    protected InetSocketAddress remoteAddress = null;

    //jitter in milliseconds. default value is 60ms
    private int jitter = 60;
    
    private ReceiveStream receiveStream;
    private SendStream sendStream;    
    
    //synchronization
    protected Timer timer;
    
    //factory instance
    private RtpFactory rtpFactory = null;
    //listener instance
    private RtpSocketListener listener;
    
    //UDP transceiver
    private Transceiver transceiver;
    
    private int period = 20;

    //the default format map between rtp payload and format
    private AVProfile avProfile = new AVProfile();
    
    //collection of codecs
    protected Collection<Codec> codecs;
    
    //RTP clock instance
    private RtpClock clock;
    
    //the format and rtp payload number negotiatiated for conversation
    //this values are valid after negotiation and till this socket will be released
    private Format format;
    private int payloadId;
    
    /**
     * Creates a new instance of RtpSocket
     * 
     * @param timer
     *            used to synchronize receiver stream.
     * @param rtpMap
     *            RTP payloads list.
     */
    public RtpSocket(RtpFactory rtpFactory, Transceiver transceiver, Timer timer,
            Collection<Codec> codecs, AVProfile avProfile, String media) {
        this.media = media;
        this.clock = rtpFactory.getClock(media);
        this.rtpFactory = rtpFactory;
        this.transceiver = transceiver;
        this.timer = timer;
        this.codecs = codecs;
        this.avProfile = avProfile.clone();

        jitter = rtpFactory.getJitter();

        sendStream = new SendStream(this, this.avProfile);
        receiveStream = new ReceiveStream(this, rtpFactory.getJitter(), this.avProfile);

        this.localAddress = rtpFactory.getBindAddress();
        this.localPort = rtpFactory.getLocalPorts().get(media);
    }

    protected Timer getTimer() {
        return timer;
    }

    public Format getFormat() {
        return format;
    }
    
    public void setPeriod(int period) {
        this.period = period;
        receiveStream.setPeriod(period);
    }
    
    public Collection<Codec> getCodecs() {
        return codecs;
    }
    /**
     * Specifies format and payload id which will be used by this socket for transmission 
     * 
     * This methods should be used by other components which are responsible for SDP negotiation.
     * The socket itself can not negotiate SDP. 
     * 
     * 
     * @param payloadId rtp payload number
     * @param format the format object.
     */
    public void setFormat(int payloadId, Format format) {
        //checking input parameters
        if (payloadId < 0) {
            throw new IllegalArgumentException("Illegal payload number");
        }
        
        if (format == null) {
            throw new IllegalArgumentException("Format can not be null");
        }

        // No any checks with formatConfig!
        // format conf is used as default configuration for creating local session 
        // description when remote session description is not known yet.
        // just apply values as is
        this.payloadId = payloadId;
        this.format = format;

        //initialize streams
        sendStream.setFormat(payloadId, format);        
        receiveStream.setFormat(payloadId, format);
    }
    
    public void setDtmfPayload(int dtmf) {
        receiveStream.setDtmf(dtmf);
        sendStream.setDtmf(dtmf);
    }
    /**
     * Gets address to which this socked is bound.
     * 
     * @return either local address to which this socket is bound or public
     *         address in case of NAT translation.
     */
    public String getLocalAddress() {
        return localAddress;
    }

    /**
     * Returns port number to which this socked is bound.
     * 
     * @return port number or -1 if socket not bound.
     */
    public int getLocalPort() {
        return localPort;
    }

    /**
     * Gets the jitter for time of packet arrival
     * 
     * @return the value of jitter in milliseconds.
     */
    public int getJitter() {
        return this.jitter;
    }

    /**
     * Assign new value of packet time arrival jitter.
     * 
     * @param jitter
     *            the value of jitter in milliseconds.
     */
    public void setJitter(int jitter) {
        this.jitter = jitter;
    }

    /**
     * Assign RTP clock implementation.
     * 
     * @param clock the RTP clock instance;
     */
    public void setClock(RtpClock clock) {
        this.clock = clock;
    }
    
    /**
     * Gets current RTP clock instance.
     * 
     * @return the RTP clock instance.
     */
    public RtpClock getClock() {
        return clock;
    }
    
    /**
     * Gets receiver stream.
     * 
     * @return receiver stream instance.
     */
    public MediaSource getReceiveStream() {
        return receiveStream;
    }

    public AVProfile getAVProfile() {
        return this.avProfile;
    }
    /**
     * Closes this socket and resets its streams;
     */
    public void release() {
        rtpFactory.releaseRTPSocket(this);        
        
        //reset streams
        receiveStream.reset();
        sendStream.reset();
        
        //disable format and payload
        this.format = null;
        this.payloadId = -1;        
    }

    public RtpSocketListener getListener() {
        return listener;
    }

    public void setListener(RtpSocketListener listener) {
        this.listener = listener;
    }

    /**
     * Assigns remote end.
     * 
     * @param address
     *            the address of the remote party.
     * @param port
     *            the port number of the remote party.
     */
    public void setPeer(InetAddress address, int port) throws IOException {
        remoteAddress = new InetSocketAddress(address, port);
        rtpFactory.streamMap.put(remoteAddress, this);
    }

    /**
     * (Non Java-doc).
     * 
     * @see org.mobicents.media.server.impl.rtp.RtpSocket#startSendStream(PushBufferDataSource);
     */
    public SendStream getSendStream() {
        return sendStream;
    }

    /**
     * Sends media data to remote peer.
     * 
     * This method uses blocking sending to make sure that data is out in time.
     * 
     * @param RtpPacket - 
     *            the packet which contains media data and rtp header
     * @throws java.io.IOException
     */
    public void send(RtpPacket packet) throws IOException {
        //coverting packet to binary array and sent to the remote address.
        byte[] p = packet.toByteArray();
        transceiver.send(p, p.length, remoteAddress);
    }

    public void send(byte[] packet) throws IOException {
        //coverting packet to binary array and sent to the remote address.
        //System.out.println("Sending " + packet.length + " bytes to " + remoteAddress);
        transceiver.send(packet, packet.length, remoteAddress);
    }
    
    /**
     * This method is called when rtp socket receives new rtp frame.
     * 
     * @param rtpPacket
     */
    public void receive(RtpPacket rtpPacket) {
        receiveStream.process(rtpPacket);
    }

    protected void notify(Exception e) {
        if (listener != null) {
            listener.error(e);
        }
    }
}
