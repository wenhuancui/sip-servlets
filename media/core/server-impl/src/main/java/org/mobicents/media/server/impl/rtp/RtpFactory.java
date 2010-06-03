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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

import net.java.stun4j.StunAddress;
import net.java.stun4j.StunException;
import net.java.stun4j.client.NetworkConfigurationDiscoveryProcess;
import net.java.stun4j.client.StunDiscoveryReport;

import org.apache.log4j.Logger;
import org.mobicents.media.server.impl.rtp.clock.AudioClock;
import org.mobicents.media.server.impl.rtp.clock.VideoClock;
import org.mobicents.media.server.impl.rtp.sdp.AVProfile;
import org.mobicents.media.server.spi.MediaType;
import org.mobicents.media.server.spi.ResourceUnavailableException;
import org.mobicents.media.server.spi.clock.Timer;
import org.mobicents.media.server.spi.dsp.Codec;
import org.mobicents.media.server.spi.dsp.CodecFactory;

/**
 * 
 * @author Oleg Kulikov
 * @author amit bhayani
 */
public class RtpFactory {

    //private HashMap<String, Transceiver> transceivers = new HashMap();
    private Receiver receiver;
    private Integer jitter = 60;
    private InetAddress bindAddress;
    protected InetSocketAddress publicAddress;
    private String stunHost;
    private int stunPort = 3478;
    private Timer timer;
    private AVProfile avProfile = new AVProfile();
    private Hashtable<MediaType, List<CodecFactory>> codecFactories;
    private int period;
    //private HashMap<MediaType, ArrayList<RtpSocket>> socketsPool = new HashMap();
    protected BufferConcurrentLinkedQueue<RtpSocket> registerQueue = new BufferConcurrentLinkedQueue();
    private transient Logger logger = Logger.getLogger(RtpFactory.class);
    
    private int portIndex;
    
    private int lowPort = 1024;
    private int highPort = 65535;
    
    /**
     * Creates RTP Factory instance
     */
    public RtpFactory() {
    }

    /**
     * Gets the address of stun server if present.
     * 
     * @return the address of stun server or null if not assigned.
     */
    public String getStunAddress() {
        return stunHost == null ? null : stunPort == 3478 ? stunHost : (stunHost + ":" + stunPort);
    }

    /**
     * Assigns address of the STUN server.
     * 
     * @param address
     *            the address of the stun server in format host[:port]. if port is not set then default port is used.
     */
    public void setStunAddress(String address) {
        String tokens[] = address.split(":");
        stunHost = tokens[0];
        if (tokens.length == 2) {
            stunPort = Integer.parseInt(tokens[1]);
        }
    }

    public void start() throws SocketException, IOException, StunException {
    	
    	this.portIndex = this.lowPort;
    	
        receiver = new Receiver(this);
        receiver.start();

        //prepare sockets
/*        ArrayList<RtpSocket> list = new ArrayList();
        for (int i = 0; i < 200; i++) {
            try {
                list.add(getRTPSocket("audio"));
            } catch (Exception e) {
                e.printStackTrace();
                throw new SocketException(e.getMessage());
            }
        }

        for (RtpSocket socket : list) {
            this.releaseRTPSocket(socket);
        }
*/        
//        receiver.start();
    }
    
    /**
     * Get the next port to be used by the RtpSocket to bind Socket
     * to passed port.
     * </br>
     * The portIndex increments cyclic starting from lowPort to highPort
     * and then back to lowPort
     * </br>
     * The Port is incremented by 2 as every alternate port is for RTCP.
     * @return
     */
    protected int getNextPort(){
    	this.portIndex+=2;
    	if(this.portIndex > this.highPort){
    		this.portIndex = this.lowPort;
    	}
    	return this.portIndex;
    }

    public void stop() {
        receiver.stop();
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    /**
     * Gets media processing timer used by RTP socket.
     * 
     * @return timer object.
     */
    public Timer getTimer() {
        return timer;
    }

    /**
     * Assigns media processing timer.
     * 
     * @param timer
     *            tmer object.
     */
    public void setTimer(Timer timer) {
        this.timer = timer;
    }

    /**
     * Gets the IP address to which trunk is bound. All endpoints of the trunk use this address for RTP connection.
     * 
     * @return the IP address string to which this trunk is bound.
     */
    public String getBindAddress() {
        return bindAddress != null ? bindAddress.getHostAddress() : null;
    }

    /**
     * Modify the bind address. All endpoints of the trunk use this address for RTP connection.
     * 
     * @param bindAddress
     *            IP address as string or host name.
     */
    public void setBindAddress(String bindAddress) throws UnknownHostException {
        this.bindAddress = InetAddress.getByName(bindAddress);
    }

    public int getLowPort() {
    	return lowPort;
    }
    
    public void setLowPort(int lowPort) {
		this.lowPort = lowPort;
	}

	public int getHighPort() {
        return highPort;
    }
    
    public void setHighPort(int highPort) {
		this.highPort = highPort;
	}

	/**
     * Gets the size of the jitter buffer in milliseconds.
     * 
     * Jitter buffer is used at the receiving ends of a VoIP connection. A jitter buffer stores received, time-jittered
     * VoIP packets, that arrive within its time window. It then plays stored packets out, in sequence, and at a
     * constant rate for subsequent decoding. A jitter buffer is typically filled half-way before playing out packets to
     * allow early, or late, packet-arrival jitter compensation.
     * 
     * Choosing a large jitter buffer reduces packet dropping from jitter but increases VoIP path delay
     * 
     * @return the size of the buffer in milliseconds.
     */
    public Integer getJitter() {
        return jitter;
    }

    /**
     * Modify size of the jitter buffer.
     * 
     * Jitter buffer is used at the receiving ends of a VoIP connection. A jitter buffer stores received, time-jittered
     * VoIP packets, that arrive within its time window. It then plays stored packets out, in sequence, and at a
     * constant rate for subsequent decoding. A jitter buffer is typically filled half-way before playing out packets to
     * allow early, or late, packet-arrival jitter compensation.
     * 
     * Choosing a large jitter buffer reduces packet dropping from jitter but increases VoIP path delay
     * 
     * @param jitter
     *            the new buffer's size in milliseconds
     */
    public void setJitter(Integer jitter) {
        this.jitter = jitter;
    }

    public AVProfile getAVProfile() {
        return avProfile;
    }

    public void setAVProfile(AVProfile avProfile) {
        this.avProfile = avProfile;
    }

    public RtpClock getClock(MediaType media) {
        if (media == MediaType.AUDIO) {
            return new AudioClock();
        } else if (media == MediaType.VIDEO) {
            return new VideoClock();
        }
        return null;
    }

    public Hashtable<MediaType, List<CodecFactory>> getCodecs() {
        return codecFactories;
    }

    public void setCodecs(Hashtable<MediaType, List<CodecFactory>> codecFactories) {
    	this.codecFactories = codecFactories;
    }

    protected Selector getSelector() {
        return receiver.getSelector();
    }
        
    protected void register() {
        while (!registerQueue.isEmpty()) {
            RtpSocket socket = registerQueue.poll();
            try {
                if (socket.isConnected()) {
                    socket.register(receiver.getSelector());
                } else {
                    registerQueue.offer(socket);
                }
            } catch (ClosedChannelException e) {
                socket.release();
            }
        }
    }
    /**
     * Constructs new RTP socket.
     * 
     * @return the RTPSocketInstance.
     * @throws StunException
     * @throws IOException
     * @throws SocketException
     * @throws StunException
     * @throws IOException
     */
    public RtpSocket getRTPSocket(MediaType media) throws IOException, ResourceUnavailableException {
        ArrayList<Codec> codecs = new ArrayList();
        if (codecFactories != null) {
            Collection<CodecFactory> factories = codecFactories.get(media);
            if (factories != null) {
                for (CodecFactory factory : factories) {
                    codecs.add(factory.getCodec());
                }
            }
        }
        // create and return new rtp socket
        RtpSocket rtpSocket = new RtpSocket(this, codecs, media);
        rtpSocket.setPeriod(period);
        return rtpSocket;
    }

//    public void releaseRTPSocket(RtpSocket rtpSocket) {
//        socketsPool.get(rtpSocket.media).add(rtpSocket);
//    }

    private InetSocketAddress getPublicAddress(InetSocketAddress localAddress) throws StunException {
        StunAddress local = new StunAddress(localAddress.getAddress(), localAddress.getPort());
        StunAddress stun = new StunAddress(stunHost, stunPort);

        // discovery stun server
        NetworkConfigurationDiscoveryProcess addressDiscovery = new NetworkConfigurationDiscoveryProcess(local, stun);
        try {
            addressDiscovery.start();
            StunDiscoveryReport report = addressDiscovery.determineAddress();
            return report.getPublicAddress().getSocketAddress();
        } finally {
            addressDiscovery.shutDown();
        }
    }
}
