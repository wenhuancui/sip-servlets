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
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import java.util.Set;
import net.java.stun4j.StunAddress;
import net.java.stun4j.StunException;
import net.java.stun4j.client.NetworkConfigurationDiscoveryProcess;
import net.java.stun4j.client.StunDiscoveryReport;

import org.apache.log4j.Logger;
import org.mobicents.media.server.impl.rtp.clock.AudioClock;
import org.mobicents.media.server.impl.rtp.clock.VideoClock;
import org.mobicents.media.server.impl.rtp.sdp.AVProfile;
import org.mobicents.media.server.spi.Timer;
import org.mobicents.media.server.spi.dsp.Codec;
import org.mobicents.media.server.spi.dsp.CodecFactory;

/**
 * 
 * @author Oleg Kulikov
 */
public class RtpFactory {

    private HashMap<String, Transceiver> transceivers = new HashMap();
    private Integer jitter = 60;
    private InetAddress bindAddress;
    private Hashtable<String, Integer> localPorts;
    protected InetSocketAddress publicAddress;
    private String stunHost;
    private int stunPort = 3478;
    private Timer timer;
    private AVProfile avProfile = new AVProfile();
    private Hashtable<String, List<CodecFactory>> codecFactories;
    private int period;
    protected volatile HashMap<SocketAddress, RtpSocket> streamMap = new HashMap();
    private HashMap<String, ArrayList<RtpSocket>> socketsPool = new HashMap();
    private transient Logger logger = Logger.getLogger(RtpFactory.class);

    /**
     * Creates RTP Factory instance
     */
    public RtpFactory() {
    }

    public Hashtable<String, Integer> getLocalPorts() {
        return localPorts;
    }

    public void setLocalPorts(Hashtable<String, Integer> localPorts) {
        this.localPorts = localPorts;
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
        //creating transcievers for each local port defined
        Set<String> mediaTypes = localPorts.keySet();
        for (String media : mediaTypes) {
            socketsPool.put(media, new ArrayList());
            int localPort = localPorts.get(media);

            InetSocketAddress address = new InetSocketAddress(bindAddress, localPort);
            logger.info("Binding RTP transceiver to " + bindAddress + ":" + localPort);

            Transceiver transceiver = new Transceiver(streamMap, bindAddress, localPort);
            transceivers.put(media, transceiver);
            transceiver.start();

            logger.info("Bound RTP transceiver to " + bindAddress + ":" + localPort + ", NAT public address is " + publicAddress);
        }



        //prepare sockets
        ArrayList<RtpSocket> list = new ArrayList();
        for (int i = 0; i < 10; i++) {
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
    }

    public void stop() {
        Collection<Transceiver> list = transceivers.values();
        for (Transceiver transceiver : list) {
            transceiver.stop();
        }
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

    public RtpClock getClock(String media) {
        if (media.equals("audio")) {
            return new AudioClock();
        } else if (media.equals("video")) {
            return new VideoClock();
        }
        return null;
    }

    public Hashtable<String, List<CodecFactory>> getCodecs() {
        return codecFactories;
    }

    public void setCodecs(Hashtable<String, List<CodecFactory>> codecFactories) {
        this.codecFactories = codecFactories;
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
    public RtpSocket getRTPSocket(String media) {
        // return object from local if present
        Collection<RtpSocket> sockets = socketsPool.get(media);
        if (!sockets.isEmpty()) {
            return socketsPool.get(media).remove(0);
        }

        ArrayList<Codec> codecs = new ArrayList();
        if (codecFactories != null) {
            Collection<CodecFactory> factories = codecFactories.get(media);
            if (codecFactories != null) {
                for (CodecFactory factory : factories) {
                    codecs.add(factory.getCodec());
                }
            }
        }
        // create and return new rtp socket
        RtpSocket rtpSocket = new RtpSocket(this, transceivers.get(media), timer, codecs, avProfile, media);
        rtpSocket.setPeriod(period);
        return rtpSocket;
    }

    public void releaseRTPSocket(RtpSocket rtpSocket) {
        streamMap.remove(rtpSocket.remoteAddress);
        socketsPool.get(rtpSocket.media).add(rtpSocket);
    }

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
