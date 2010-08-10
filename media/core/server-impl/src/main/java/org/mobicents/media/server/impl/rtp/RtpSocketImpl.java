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
package org.mobicents.media.server.impl.rtp;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Collection;

import org.mobicents.media.Format;
import org.mobicents.media.server.spi.MediaType;
import org.mobicents.media.server.spi.ResourceUnavailableException;
import org.mobicents.media.server.spi.dsp.Codec;
import org.mobicents.media.server.spi.rtp.AVProfile;
import org.mobicents.media.server.spi.rtp.RtpSocket;

/**
 * 
 * @author Oleg Kulikov
 */
public class RtpSocketImpl implements RtpSocket {

    /** Media type */
    protected MediaType media;
    
    //local address and port pair
    private DatagramSocket socket;    
    private DatagramChannel channel;
    
    private String localAddress;
    
    //remote address and port pair
    protected InetSocketAddress remoteAddress = null;    

    //jitter in milliseconds. default value is 60ms
    private int jitter = 60;
    
    //media streams
    private ReceiveStream receiveStream;
    private SendStream sendStream;    
    
    //factory instance
    private RtpFactory factory = null;
    //listener instance
    private RtpSocketListener listener;
    
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

    private SelectionKey selection;
    protected boolean registered = false;
    
    private ByteBuffer sendBuffer = ByteBuffer.allocateDirect(8192);
    
    //statisctics
    private int packetsSent;
    private int packetsReceived;
    
    private volatile boolean isClosed = false;
    
    /**
     * The prefferedPort is assigned by RtpFactory. 
     * RtpSocket will reuse the prefferedPort to bind the Socket everytime
     * till BindException is thrown.
     * 
     *  Once BindException, a new Port is obtained from RtpFactory and it becomes 
     *  as prefferedPort for all future binds
     */
    private int prefferedPort;
    
    /**
     * Creates a new instance of RtpSocket
     * 
     * @param timer
     *            used to synchronize receiver stream.
     * @param rtpMap
     *            RTP payloads list.
     */
    public RtpSocketImpl(RtpFactory factory, Collection<Codec> codecs,  MediaType media) throws IOException, ResourceUnavailableException {
        this.media = media;

        this.clock = factory.getClock(media);

        this.factory = factory;
        //this.timer = factory.getTimer();
        this.codecs = codecs;
        this.avProfile = factory.getAVProfile().clone();

        jitter = factory.getJitter();
        
        this.prefferedPort = factory.getNextPort();
        sendStream = new SendStream(this, this.avProfile);
        receiveStream = new ReceiveStream(this, factory.getJitter(), this.avProfile);

        this.localAddress = factory.getBindAddress();
    }

    /**
     * Opens channel.
     * 
     * @throws java.io.IOException
     */
    private void openChannel() throws IOException {
        channel = DatagramChannel.open();
        channel.configureBlocking(false);

        socket = channel.socket();
    }
    
    /**
     * Binds Datagram to the address sprecified.
     * 
     * @throws java.io.IOException
     * @throws org.mobicents.media.server.spi.ResourceUnavailableException
     */
    public void bind() throws IOException, ResourceUnavailableException {
        //disbale closed flag if was set
        this.isClosed = false;
        
        //opening channel
        openChannel();
        
        //binding socket to the first available port.
        while (!socket.isBound() && this.prefferedPort < factory.getHighPort()) {
            try {
                //it is expected that "preffered port" is what we need
                InetSocketAddress address = new InetSocketAddress(factory.getBindAddress(), this.prefferedPort);
                socket.bind(address);
            } catch (SocketException e) {
                //port is in use? let's try another
                this.prefferedPort = this.factory.getNextPort();
            }
        }
        
        //socket still not bound? throw exception
        if (!socket.isBound()) {
            channel.close();
            throw new ResourceUnavailableException();
        }
    }
    
    /**
     * Registers this socket usign specified selector.
     * 
     * @param selector the selector for registration.
     * @throws java.nio.channels.ClosedChannelException
     */
    protected void register(Selector selector) throws ClosedChannelException {
        selection = channel.register(selector, SelectionKey.OP_READ, this);
        registered = true;
    }
    
    
    /**
     * Gets the currently used format.
     * 
     * @return the format instance.
     */
    public Format getFormat() {
        return format;
    }

    /**
     * Gets the list of used codecs.
     * 
     * @return list of codecs.
     */
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

    /**
     * Assigns RFC2833 DTMF playload number.
     * 
     * @param dtmf the DTMF payload number.
     */
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
        return this.prefferedPort;
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
    public ReceiveStream getReceiveStream() {
        return receiveStream;
    }

    /**
     * Gets currently used audio/video profile
     * @return
     */
    public AVProfile getAVProfile() {
        return this.avProfile;
    }

    /**
     * Gets the number of received packets
     * 
     * @return the number of packets received
     */
    public int getPacketsReceived() {
        return packetsReceived;
    }
    
    /**
     * Gets the number of sent packets
     * 
     * @return the number of sent packets.
     */
    public int getPacketsSent() {
        return packetsSent;
    }
    
    /**
     * Closes this socket and resets its streams;
     * This method is called by RtpSocket user.
     * 
     */
    public void release() {
        //channel close action should be synchronized with read and write 
        //but we want to avid usage of any locks so at this stage we are 
        //setting flag that socket is closed only!
        //
        //Receiver will check this flag on selected channel and perform actual close procedure.
        this.isClosed = true;
    }

    protected void close() {
        if (selection != null) {
            selection.cancel();
        }
        //reset streams
        receiveStream.reset();
        sendStream.reset();

        //disable format and payload
        this.format = null;
        this.payloadId = -1;
        
        //clean stats
        packetsSent = 0;
        packetsReceived = 0;
        
        try {
            if (channel != null) {
                channel.close();
            }

            if (socket != null) {
                socket.disconnect();
                socket.close();
            }
        } catch (IOException e) {
        }
    }
    
    public boolean isClosed() {
        return this.isClosed;
    }
    
    /**
     * Gets the currently assigned listener.
     * 
     * @return the listener instance.
     */
    public RtpSocketListener getListener() {
        return listener;
    }

    /**
     * Assigns listener which will receive notifications.
     * 
     * @param listener the listener instance.
     */
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
        channel.connect(remoteAddress);
        factory.registerQueue.offer(this);
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
        //check state flag before sending
        if (!this.isClosed) {
            byte[] p = packet.toByteArray();
            send(p);
        }
    }

    /**
     * Sends rtp packet to the remote peer.
     * 
     * @param packet the rtp packet as binary arrary
     * @throws java.io.IOException
     */
    public void send(byte[] packet) throws IOException {
        //coverting packet to binary array and sent to the remote address.
        sendBuffer.clear();
        sendBuffer.rewind();
        sendBuffer.put(packet);
        sendBuffer.flip();
        try{
        	channel.write(sendBuffer);
        } catch (IOException e) {
            this.notify(e);
        }
        packetsSent++;
    }

    /**
     * This method is called when rtp socket receives new rtp frame.
     * 
     * @param rtpPacket
     */
    public void receive(RtpPacket rtpPacket) {
        receiveStream.process(rtpPacket);
        packetsReceived++;
    }

    /**
     * Notifies the listener that something goes wrong.
     * 
     * @param e the exception. 
     */
    protected void notify(Exception e) {
        if (listener != null) {
            listener.error(e);
        }
    }
    
    /**
     * Statistical method.
     * 
     * @return the number of bytes received.
     */
    public long getBytesReceived() {
        return receiveStream.byteCount;
    }
    
    /**
     * Statistical method.
     * 
     * @return the number of bytes sent.
     */
    public long getBytesSent() {
        return sendStream.byteCount;
    }
}
