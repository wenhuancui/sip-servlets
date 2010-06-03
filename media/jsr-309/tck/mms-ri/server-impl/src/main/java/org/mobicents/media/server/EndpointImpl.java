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
package org.mobicents.media.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import javax.sdp.SdpFactory;

import org.apache.log4j.Logger;
import org.mobicents.media.Buffer;
import org.mobicents.media.Component;
import org.mobicents.media.ComponentFactory;
import org.mobicents.media.Format;
import org.mobicents.media.MediaSink;
import org.mobicents.media.MediaSource;
import org.mobicents.media.server.impl.AbstractSink;
import org.mobicents.media.server.impl.AbstractSource;
import org.mobicents.media.server.impl.rtp.RtpFactory;
import org.mobicents.media.server.resource.Channel;
import org.mobicents.media.server.spi.Connection;
import org.mobicents.media.server.spi.ConnectionListener;
import org.mobicents.media.server.spi.ConnectionMode;
import org.mobicents.media.server.spi.Endpoint;
import org.mobicents.media.server.spi.NotificationListener;
import org.mobicents.media.server.spi.ResourceGroup;
import org.mobicents.media.server.spi.ResourceUnavailableException;
import org.mobicents.media.server.spi.Timer;
import org.mobicents.media.server.spi.TooManyConnectionsException;

/**
 * 
 * @author kulikov
 */
public class EndpointImpl implements Endpoint {

    private String localName;
    private boolean isInUse = false;
    private Timer timer;
    private Map<String, ComponentFactory> sourceFactory;
    private Map<String, ComponentFactory> sinkFactory;
    private ComponentFactory groupFactory;
    private ConnectionFactory connectionFactory;
    private RtpFactory rtpFactory;
    private HashMap<String, MediaSource> sources = new HashMap();
    private HashMap<String, MediaSink> sinks = new HashMap();
    private ResourceGroup resourceGroup;
    private ArrayList<String> mediaTypes = new ArrayList();
    /** The list of indexes available for connection enumeration within endpoint */
    private ArrayList<Integer> index = new ArrayList();
    /** The last generated connection's index */
    private int lastIndex = -1;
    /** Holder for created connections */
    protected transient HashMap<String, Connection> connections = new HashMap();
    protected ReentrantLock state = new ReentrantLock();
    private SdpFactory sdpFactory = SdpFactory.getInstance();
    private final Logger logger = Logger.getLogger(EndpointImpl.class);

    public EndpointImpl() {
    }

    public EndpointImpl(String localName) {
        this.localName = localName;
    }

    public String getLocalName() {
        return localName;
    }

    public void setLocalName(String localName) {
        this.localName = localName;
    }

    public Collection<String> getMediaTypes() {
        return mediaTypes;
    }

    public MediaSink getSink(String media) {
        MediaSink sink = sinks.get(media);
        if (sink == null) {
            sink = new VirtualSink("virtual.sink");
            sinks.put(media, sink);
        }
        return sink;
    }

    public MediaSource getSource(String media) {
        return sources.get(media);
    }

    /**
     * Calculates index of the new connection.
     * 
     * The connection uses this method to ask endpoint for new lowerest index. The index is unique withing endpoint but
     * it is not used as connection identifier outside of the endpoint.
     * 
     * @return the lowerest available integer value.
     */
    public int getConnectionIndex() {
        return index.isEmpty() ? ++lastIndex : index.remove(0);
    }

    public void start() throws ResourceUnavailableException {
        if (sourceFactory != null) {
            Set<String> types = sourceFactory.keySet();
            for (String media : types) {
                ComponentFactory factory = sourceFactory.get(media);
                MediaSource source = (MediaSource) factory.newInstance(this);
                source.setEndpoint(this);
                sources.put(media, source);
                if (!mediaTypes.contains(media)) {
                    mediaTypes.add(media);
                }
            }
        }

        if (sinkFactory != null) {
            Set<String> types = sinkFactory.keySet();
            for (String media : types) {
                ComponentFactory factory = sinkFactory.get(media);
                MediaSink sink = (MediaSink) factory.newInstance(this);
                sink.setEndpoint(this);
                sinks.put(media, sink);
                if (!mediaTypes.contains(media)) {
                    mediaTypes.add(media);
                }
            }
        }

        if (groupFactory != null) {
            resourceGroup = (ResourceGroup) groupFactory.newInstance(this);
            mediaTypes.addAll(resourceGroup.getMediaTypes());

            for (String media : mediaTypes) {
                MediaSink sink = resourceGroup.getSink(media);
                if (sink != null) {
                    sink.setEndpoint(this);
                    sinks.put(media, sink);
                } else if (!sinks.containsKey(media)) {
                    sink = new VirtualSink(getLocalName());
                    sinks.put(media, sink);
                }

                MediaSource source = resourceGroup.getSource(media);
                if (source != null) {
                    source.setEndpoint(this);
                    source.setSyncSource(timer);
                    sources.put(media, source);
                }
            }
        }

        logger.info("Started " + localName);
    }

    public void stop() {
        logger.info("Stopped " + localName);
    }

    protected SdpFactory getSdpFactory() {
        return sdpFactory;
    }

    public Timer getTimer() {
        return timer;
    }

    public void setTimer(Timer timer) {
        this.timer = timer;
    }

    public void setSourceFactory(Map<String, ComponentFactory> sourceFactory) {
        this.sourceFactory = sourceFactory;
    }

    public Map<String, ComponentFactory> getSourceFactory() {
        return sourceFactory;
    }

    public void setSinkFactory(Map<String, ComponentFactory> sinkFactory) {
        this.sinkFactory = sinkFactory;
    }

    public Map<String, ComponentFactory> getSinkFactory() {
        return sinkFactory;
    }

    public ComponentFactory getGroupFactory() {
        return groupFactory;
    }

    public void setGroupFactory(ComponentFactory groupFactory) {
        this.groupFactory = groupFactory;
    }

    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void setRtpFactory(RtpFactory rtpFactory) {
        this.rtpFactory = rtpFactory;
    }

    public RtpFactory getRtpFactory() {
        return this.rtpFactory;
    }

    public Collection<Connection> getConnections() {
        return connections.values();
    }

    public Connection createConnection(ConnectionMode mode) throws TooManyConnectionsException,
            ResourceUnavailableException {
        state.lock();
        try {
            if (logger.isDebugEnabled()) {
                logger.debug(getLocalName() + ", creating RTP connection, mode=" + mode);
            }
            RtpConnectionImpl connection = new RtpConnectionImpl(this, mode);
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
        state.lock();
        try {
            if (logger.isDebugEnabled()) {
                logger.debug(getLocalName() + ", creating Local connection, mode=" + mode);
            }
            LocalConnectionImpl connection = new LocalConnectionImpl(this, mode);
            connections.put(connection.getId(), connection);
            this.isInUse = true;
            return connection;
        } catch (Exception e) {
            logger.error("Could not create Local connection", e);
            throw new ResourceUnavailableException(e.getMessage());
        } finally {
            state.unlock();
        }
    }

    public void deleteConnection(String connectionID) {
        state.lock();
        try {
            ConnectionImpl connection = (ConnectionImpl) connections.remove(connectionID);
            if (connection != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug(getLocalName() + ", Deleting connection " + connection.getIndex());
                }
                connection.close();
                index.add(connection.getIndex());
            }
            isInUse = connections.size() > 0;
        } finally {
            state.unlock();
        }
    }

    protected void txConnect(Channel channel, String media) throws ResourceUnavailableException {
        MediaSource source = sources.get(media);
        if (source != null) {
            channel.connect(source);
        }
    }

    protected void txDisconnect(Channel channel, String media) {
        channel.stop();
        MediaSource source = sources.get(media);
        if (source != null) {
            source.stop();
            channel.disconnect(source);
        }
    }

    protected void rxConnect(Channel channel, String media) throws ResourceUnavailableException {
        channel.stop();
        MediaSink sink = sinks.get(media);
        if (sink == null) {
            sink = new VirtualSink("virtual");
            sink.start();
            sinks.put(media, sink);
        }
        channel.connect(sink);
    }

    protected void rxDisconnect(Channel channel, String media) {
        MediaSink sink = sinks.get(media);
        channel.stop();
        channel.disconnect(sink);
    }

    /**
     * (Non Java-doc).
     * 
     * @see org.mobicents.media.server.spi.Endpoint#deleteAllConnections();
     */
    public void deleteAllConnections() {
        state.lock();
        try {
            ConnectionImpl[] list = new ConnectionImpl[connections.size()];
            connections.values().toArray(list);
            for (int i = 0; i < list.length; i++) {
                deleteConnection(list[i].getId());
            }
        } finally {
            state.unlock();
        }
    }

    public boolean hasConnections() {
        return !connections.isEmpty();
    }

    public boolean isInUse() {
        return this.isInUse;
    }

    public void setInUse(boolean inUse) {
        this.isInUse = inUse;
    }

    public void addNotificationListener(NotificationListener listener) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeNotificationListener(NotificationListener listener) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addConnectionListener(ConnectionListener listener) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeConnectionListener(ConnectionListener listener) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String[] getSupportedPackages() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Connection getConnection(String connectionID) {
        return connections.get(connectionID);
    }

    public Component getComponent(String name) {
        Collection<MediaSource> components = sources.values();
        for (MediaSource source : components) {
            if (source.getName().matches(name)) {
                return source;
            }
        }

        Collection<MediaSink> components2 = sinks.values();
        for (MediaSink sink : components2) {
            if (sink.getName().matches(name)) {
                return sink;
            }
        }

        return null;
    }

    @Override
    public Endpoint clone() {
        EndpointImpl enp = new EndpointImpl();
        enp.setTimer(this.getTimer());
        enp.setRtpFactory(this.getRtpFactory());
        enp.setConnectionFactory(this.getConnectionFactory());
        enp.setSourceFactory(this.getSourceFactory());
        enp.setSinkFactory(this.getSinkFactory());
        enp.setGroupFactory(this.getGroupFactory());
        return enp;
    }

    private class VirtualSink extends AbstractSink {

        public VirtualSink(String name) {
            super(name);
        }

        public Format[] getFormats() {
            return new Format[]{Format.ANY};
        }

        public boolean isAcceptable(Format format) {
            return true;
        }

        @Override
        public void onMediaTransfer(Buffer buffer) throws IOException {
            // do nothing
        }
    }

    private class VirtualSource extends AbstractSource {

        public VirtualSource(String name) {
            super(name);
        }

        @Override
        public void evolve(Buffer buffer, long timestamp, long sequenceNumber) {
            buffer.setFlags(Buffer.FLAG_SILENCE);
        }

        public Format[] getFormats() {
            return new Format[]{Format.ANY};
        }
    }

    public String getLocalAddress(String media) {
        return rtpFactory != null ? rtpFactory.getBindAddress() : "0.0.0.0";
    }

    public int getLocalPort(String media) {
        return rtpFactory != null && rtpFactory.getLocalPorts().contains(media)? 
            rtpFactory.getLocalPorts().get(media): 0;
    }
}
