package org.mobicents.media.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

import javax.sdp.SdpFactory;

import org.apache.log4j.Logger;
import org.mobicents.media.Component;
import org.mobicents.media.ComponentFactory;
import org.mobicents.media.MediaSource;
import org.mobicents.media.server.impl.resource.video.AVPlayer;
import org.mobicents.media.server.impl.rtp.RtpFactory;
import org.mobicents.media.server.spi.Connection;
import org.mobicents.media.server.spi.ConnectionListener;
import org.mobicents.media.server.spi.ConnectionMode;
import org.mobicents.media.server.spi.Endpoint;
import org.mobicents.media.server.spi.NotificationListener;
import org.mobicents.media.server.spi.ResourceUnavailableException;
import org.mobicents.media.server.spi.Timer;
import org.mobicents.media.server.spi.TooManyConnectionsException;
import org.mobicents.media.server.spi.resource.Player;

public class RtspEndpointImpl implements Endpoint {

    private String localName;
    private boolean isInUse = false;
    
    private Timer timer;

    private transient RtpFactory rtpFactory;
    protected transient HashMap<String, Connection> connections = new HashMap();
    
    protected ReentrantLock state = new ReentrantLock();
    private transient SdpFactory sdpFactory = SdpFactory.getInstance();
    
    /** The list of indexes available for connection enumeration within endpoint */
    private ArrayList<Integer> index = new ArrayList();
    
    private Player player;
    private ComponentFactory playerFactory;
    
    /** The last generated connection's index*/
    private int lastIndex = -1;
    private final Logger logger = Logger.getLogger(RtspEndpointImpl.class);
    
    protected HashMap<String, String> tracks = new HashMap();
    protected HashMap<String, Long> ssrc = new HashMap();
    
    public RtspEndpointImpl() {
    }

    public RtspEndpointImpl(String localName) {
        this.localName = localName;
    }

    public String getLocalName() {
        return localName;
    }

    public void setLocalName(String localName) {
        this.localName = localName;
    }

    public String getLocalAddress(String media) {
        return rtpFactory != null ? rtpFactory.getBindAddress() : "0.0.0.0";
    }

    public int getLocalPort(String media) {
        String mediaType = media.startsWith("track")? tracks.get(media) : media;
        int port = rtpFactory != null && rtpFactory.getLocalPorts().containsKey(mediaType)? 
                rtpFactory.getLocalPorts().get(mediaType): 0;
        return port;
    }
    
    public Timer getTimer() {
        return timer;
    }

    public void setTimer(Timer timer) {
        this.timer = timer;
    }

    public boolean hasConnections() {
        return !connections.isEmpty();
    }

    public boolean isInUse() {
        return this.isInUse;
    }

    public SdpFactory getSdpFactory() {
        return sdpFactory;
    }

    public void setPlayer(ComponentFactory playerFactory) {
        this.playerFactory = playerFactory;
    }
    
    public ComponentFactory getPlayer() {
        return playerFactory;
    }
    
    public Collection<String> getMediaTypes() {
        return ((AVPlayer)player).getMediaTypes();
    }
    
    public String getSdp(String mediaType) {
        //@TODO remove hardcoded avplayer and multimedia source
        return ((AVPlayer)player).getSdp(mediaType);
    }

    public MediaSource getSource(String mediaType) {
        return ((AVPlayer) player).getSource(mediaType);
    }
    
    public void setRtpFactory(RtpFactory rtpFactory) {
        this.rtpFactory = rtpFactory;
    }

    public RtpFactory getRtpFactory() {
        return this.rtpFactory;
    }

    public void addConnectionListener(ConnectionListener listener) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addNotificationListener(NotificationListener listener) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Connection createConnection(ConnectionMode mode) throws TooManyConnectionsException,
            ResourceUnavailableException {
        state.lock();
        try {
            if (logger.isDebugEnabled()) {
                logger.debug(getLocalName() + ", creating RTP connection, mode=" + mode);
            }
            RtspRtpConnectionImpl connection = new RtspRtpConnectionImpl(this, mode);
            connections.put(connection.getId(), connection);
            this.isInUse = true;
            return connection;
        } catch (Exception e) {
            logger.error("Could not create RTP connection", e);
            throw new ResourceUnavailableException(e.getMessage());
        } finally {
            state.unlock();
        }
    }

    public Connection createLocalConnection(ConnectionMode mode) throws TooManyConnectionsException,
            ResourceUnavailableException {
        // TODO Auto-generated method stub
        return null;
    }

    public void deleteAllConnections() {
        state.lock();
        try {
            Connection[] list = new Connection[connections.size()];
            connections.values().toArray(list);
            for (int i = 0; i < list.length; i++) {
                deleteConnection(list[i].getId());
            }
        } finally {
            state.unlock();
        }

    }

    public void deleteConnection(String connectionID) {
        state.lock();
        try {
            RtspRtpConnectionImpl connection = (RtspRtpConnectionImpl) connections.remove(connectionID);
            if (connection != null) {
//                if (logger.isDebugEnabled()) {
//                    logger.debug(getLocalName() + ", Deleting connection " + connection.getIndex());
//                }
                connection.close();
//                index.add(connection.getIndex());
            }
            isInUse = connections.size() > 0;
        } finally {
            state.unlock();
        }
    }

    public Component getComponent(String resourceName) {
        return player;
    }

    public Connection getConnection(String connectionID) {
        return connections.get(connectionID);
    }


    public void removeConnectionListener(ConnectionListener listener) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeNotificationListener(NotificationListener listener) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setInUse(boolean inUse) {
        this.isInUse = inUse;
    }

    public void start() throws ResourceUnavailableException {
        this.player = (Player) playerFactory.newInstance(this);
    }

    public void stop() {
        logger.info("Stopped " + localName);
    }

    public int getConnectionIndex() {
        return index.isEmpty() ? ++lastIndex : index.remove(0);
    }

    @Override
    public Endpoint clone() {
        RtspEndpointImpl enp = new RtspEndpointImpl();
        enp.setTimer(this.getTimer());
        enp.setRtpFactory(this.getRtpFactory());
        enp.setPlayer(playerFactory);
        return enp;
    }
}
