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
package org.mobicents.media.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

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
import org.mobicents.media.server.spi.Connection;
import org.mobicents.media.server.spi.ConnectionListener;
import org.mobicents.media.server.spi.ConnectionState;
import org.mobicents.media.server.spi.Endpoint;
import org.mobicents.media.server.spi.MediaType;
import org.mobicents.media.server.spi.MultimediaSink;
import org.mobicents.media.server.spi.MultimediaSource;
import org.mobicents.media.server.spi.NotificationListener;
import org.mobicents.media.server.spi.ResourceGroup;
import org.mobicents.media.server.spi.ResourceUnavailableException;
import org.mobicents.media.server.spi.TooManyConnectionsException;
import org.mobicents.media.server.spi.clock.Timer;

/**
 * 
 * @author kulikov
 * @author amit bhayani
 */
public class EndpointImpl implements Endpoint {

	private String localName;
	private boolean isInUse = false;
	private Timer timer;

	private Map<String, ComponentFactory> sourceFactory;
	private Map<String, ComponentFactory> sinkFactory;

	private ComponentFactory groupFactory;

	protected ConnectionFactory connectionFactory;
	protected RtpFactory rtpFactory;

	private HashMap<MediaType, MediaSource> sources = new HashMap();
	private HashMap<MediaType, MediaSink> sinks = new HashMap();

	private ArrayList<MediaType> mediaTypes = new ArrayList();
	/** The list of indexes available for connection enumeration within endpoint */
	private ArrayList<Integer> index = new ArrayList();
	/** The last generated connection's index */
	private int lastIndex = -1;
	/** Holder for created connections */
	// protected transient HashMap<String, Connection> connections = new
	// HashMap();
	//private int maxConnections = 10;
	private static final int _CONNECTION_TAB_SIZE = 10;
	// collection of prestarted local connections
	private LocalConnectionImpl[] localConnections;

	// collection of prestarted network connections
	private RtpConnectionImpl[] networkConnections;

	// collection of active connections
	private Connection[] connections;
	private int count;

	protected ReentrantLock state = new ReentrantLock();
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

	public Collection<MediaType> getMediaTypes() {
		return mediaTypes;
	}

	public MediaSink getSink(MediaType media) {
		MediaSink sink = sinks.get(media);
		if (sink == null) {
			sink = new VirtualSink("virtual.sink");
			sinks.put(media, sink);
		}
		return sink;
	}

	public MediaSource getSource(MediaType media) {
		return sources.get(media);
	}

	/**
	 * Calculates index of the new connection.
	 * 
	 * The connection uses this method to ask endpoint for new lowerest index.
	 * The index is unique withing endpoint but it is not used as connection
	 * identifier outside of the endpoint.
	 * 
	 * @return the lowerest available integer value.
	 */
	public int getConnectionIndex() {
		return index.isEmpty() ? ++lastIndex : index.remove(0);
	}

	/**
	 * Gets the list of the media types related to specified name
	 * 
	 * @param name
	 *            the name of the media or wildrard symbol
	 * @return
	 */
	private MediaType[] getMediaTypes(String media) {
		if (media.equalsIgnoreCase("audio")) {
			return new MediaType[] { MediaType.AUDIO };
		} else if (media.equals("video")) {
			return new MediaType[] { MediaType.VIDEO };
		} else if (media.equals("*")){
			return new MediaType[] { MediaType.AUDIO, MediaType.VIDEO };
		} else
			throw new IllegalArgumentException("Unknown media type " + media);
	}

	private void createSources() throws ResourceUnavailableException {
		if (sourceFactory == null) {
			return;
		}
		// which media types are configured?
		Set<String> types = sourceFactory.keySet();
		for (String media : types) {
			// get the factory configured for each media type
			ComponentFactory factory = sourceFactory.get(media);

			// creating media source
			Component source = factory.newInstance(this);
			source.setEndpoint(this);

			// determine media types related to this source
			MediaType[] list = getMediaTypes(media);
			// apply source
			for (int i = 0; i < list.length; i++) {
				
				if(source instanceof MediaSource){
					sources.put(list[i], (MediaSource)source);
				} else if(source instanceof MultimediaSource ){
					sources.put(list[i],((MultimediaSource)source).getMediaSource(list[i]));
				} else{
					logger.warn("Component "+ source.toString()+ " is neither instance of MediaSource or MultimediaSource");
				}
				// update list of available media types
				if (!mediaTypes.contains(list[i])) {
					mediaTypes.add(list[i]);
				}
			}
		}
	}

	private void createSource(ResourceGroup group) {
		Collection<MediaType> list = group.getMediaTypes();
		for (MediaType mediaType : list) {
			MediaSource source = group.getSource(mediaType);
			if (source != null) {
				source.setEndpoint(this);
				sources.put(mediaType, source);

				// update list of available media types
				if (!mediaTypes.contains(mediaType)) {
					mediaTypes.add(mediaType);
				}
			}
		}
	}

	private void createSinks() throws ResourceUnavailableException {
		if (sinkFactory == null) {
			return;
		}
		// which media types are configured?
		Set<String> types = sinkFactory.keySet();
		for (String media : types) {
			// get the factory configured for each media type
			ComponentFactory factory = sinkFactory.get(media);

			// creating media source
			Component sink = factory.newInstance(this);
			sink.setEndpoint(this);

			// determine media types related to this source
			MediaType[] list = getMediaTypes(media);
			// apply source
			for (int i = 0; i < list.length; i++) {
				if(sink instanceof MediaSink){
					sinks.put(list[i], (MediaSink)sink);
				} else if(sink instanceof MultimediaSink){
					sinks.put(list[i], ((MultimediaSink)sink).getMediaSink(list[i]));
				} else{
					logger.warn("Component "+ sink.toString()+ " is neither instance of MediaSink or MultimediaSink");
				}
				// update list of available media types
				if (!mediaTypes.contains(list[i])) {
					mediaTypes.add(list[i]);
				}
			}
		}
	}

	private void createSink(ResourceGroup group) {
		Collection<MediaType> list = group.getMediaTypes();
		for (MediaType mediaType : list) {
			MediaSink sink = group.getSink(mediaType);
			if (sink != null) {
				sink.setEndpoint(this);
				sinks.put(mediaType, sink);
				// update list of available media types
				if (!mediaTypes.contains(mediaType)) {
					mediaTypes.add(mediaType);
				}
			}
		}
	}

	public void start() throws ResourceUnavailableException {
		networkConnections = new RtpConnectionImpl[_CONNECTION_TAB_SIZE];
		localConnections = new LocalConnectionImpl[_CONNECTION_TAB_SIZE];
		connections = new ConnectionImpl[_CONNECTION_TAB_SIZE];

		// if resource group is specified then group is used
		// as source and sink
		if (groupFactory != null) {
			ResourceGroup group = (ResourceGroup) groupFactory
					.newInstance(this);
			createSource(group);
			createSink(group);
		} else {
			// other way the individual sources and sinks should be specified
			createSources();
			createSinks();
		}

		// let's create several connections
		for (int i = 0; i < connections.length; i++) {
			localConnections[i] = new LocalConnectionImpl(this);
			this.setLifeTime(localConnections[i]);
		}

		// let's create several connections
		if (this.rtpFactory != null) {
			for (int i = 0; i < connections.length; i++) {
				networkConnections[i] = new RtpConnectionImpl(this);
				this.setLifeTime(networkConnections[i]);
			}
		}

		logger.info("Started " + localName);
	}
	
	private void setLifeTime(Connection connection){
		
		//lets set the LifeTime for connection
		if(this.connectionFactory != null && this.connectionFactory.getConnectionStateManager() != null){
			Hashtable<ConnectionState, Integer> connStateLifeTime = this.connectionFactory.getConnectionStateManager().getConnStateLifeTime();
			for(ConnectionState connState : connStateLifeTime.keySet()){
				connection.getLifeTime()[connState.getCode()] = connStateLifeTime.get(connState);
				if(logger.isDebugEnabled()){
					logger.debug("silence for state "+ connState +" is "+ connStateLifeTime.get(connState));
				}
			}
		}		
	}

	public void stop() {
		logger.info("Stopped " + localName);
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
		return null;// connections.values();
	}

	public String describe(MediaType mediaType) throws ResourceUnavailableException {
		String sdp = null;
		state.lock();
		try {
			RtpConnectionImpl connection = null;
			for (int i = 0; i < networkConnections.length; i++) {
				if (networkConnections[i].getState() == ConnectionState.NULL) {
					connection = networkConnections[i];
					break;
				}
			}

			// connection not found?
			if (connection == null) {
				throw new ResourceUnavailableException(
						"The limit of network connection is exeeded");
			}

			if(mediaType == null) {
				connection.join();
				sdp = connection.getLocalDescriptor();
				connection.close();
			} else{
				connection.join(mediaType);
				sdp = connection.getLocalDescriptor();
				connection.close(mediaType);				
			}
		} catch (Exception e) {
			logger.error("Could not create RTP connection", e);
			throw new ResourceUnavailableException(e.getMessage());
		} finally {
			state.unlock();
		}
		return sdp;
	}

	/**
	 * (Non Java-doc.)
	 * 
	 * @see org.mobicents.media.server.spi.Endpoint#createConnection(org.mobicents.media.server.spi.ConnectionMode);
	 */
	public Connection createConnection()
			throws TooManyConnectionsException, ResourceUnavailableException {
		state.lock();
		try {
			// what we need is to find the first unused connection from list
			// of network connections and return it.
			RtpConnectionImpl connection = null;
			for (int i = 0; i < networkConnections.length; i++) {
				if (networkConnections[i].getState() == ConnectionState.NULL) {
					connection = networkConnections[i];
					break;
				}
			}

			// connection not found?
			if (connection == null) {
				throw new ResourceUnavailableException(
						"The limit of network connection is exeeded");
			}

			// connection found. put this connection to the list of used
			// connections
			// searching first suitable place
			for (int i = 0; i < connections.length; i++) {
				if (connections[i] == null) {
					connection.setIndex(i);
                                        System.out.println("Connection index=" + i);
					connections[i] = connection;
					break;
				}
			}
                        
			connection.join();
			connection.bind();

			//Sync the Connection for LifeTime handling
			//logger.info("Synced Connection "+ connection);
			connection.setStartTime(System.currentTimeMillis());
			this.timer.sync(connection);		
			
			count++;
			this.isInUse = true;
			return connection;
		} catch (Exception e) {
			logger.error("Could not create RTP connection", e);
			throw new ResourceUnavailableException(e.getMessage());
		} finally {
			state.unlock();
		}
	}

	/**
	 * (Non Java-doc.)
	 * 
	 * @see org.mobicents.media.server.spi.Endpoint#createConnection(org.mobicents.media.server.spi.ConnectionMode);
	 */
	public Connection createLocalConnection()
			throws TooManyConnectionsException, ResourceUnavailableException {
		state.lock();
		try {
			// what we need is to find the first unused connection from list
			// of local connections and return it.
			LocalConnectionImpl connection = null;
			for (int i = 0; i < localConnections.length; i++) {
				if (localConnections[i].getState() == ConnectionState.NULL) {
					connection = localConnections[i];
					break;
				}
			}

			// connection not found?
			if (connection == null) {
				throw new ResourceUnavailableException(
						"The limit of network connection is exeeded");
			}

			// connection found. put this connection to the list of used
			// connections
			// searching first suitable place
			for (int i = 0; i < connections.length; i++) {
				if (connections[i] == null) {
					connection.setIndex(i);
					connections[i] = connection;
					if(logger.isInfoEnabled())
					{
						logger.info("Connection index: "+i+", on endpoint: "+this.localName);
					}
					break;
				}
			}
                        
			connection.join();
			connection.bind();
			
			//Sync the Connection for LifeTime handling
			connection.setStartTime(System.currentTimeMillis());
			this.timer.sync(connection);
			
			count++;
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
			// find this connection
			ConnectionImpl connection = null;
			for (int i = 0; i < connections.length; i++) {
				if (connections[i] != null
						&& ((ConnectionImpl) connections[i]).getId().equals(
								connectionID)) {
					connection = (ConnectionImpl) connections[i];
					// do not forget to remove it from list of active
					// connections
					connections[i] = null;
					break;
				}
			}

			if (connection != null) {
				connection.close();
				count--;
			}

			this.isInUse = count > 0;
		} finally {
			state.unlock();
		}
	}


	/**
	 * (Non Java-doc).
	 * 
	 * @see org.mobicents.media.server.spi.Endpoint#deleteAllConnections();
	 */
	public void deleteAllConnections() {
		state.lock();
		try {
			for (int i = 0; i < connections.length; i++) {
				if (connections[i] != null) {
					((ConnectionImpl) connections[i]).close();
					connections[i] = null;
				}
			}
			count = 0;
		} finally {
			state.unlock();
		}
	}

	public boolean hasConnections() {
		return count > 0;
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
		for (int i = 0; i < connections.length; i++) {
			if (connections[i] != null
					&& connections[i].getId().equals(connectionID)) {
				return connections[i];
			}
		}
		return null;
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
			return new Format[] { Format.ANY };
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
		public void evolve(Buffer buffer, long timestamp) {
			buffer.setFlags(Buffer.FLAG_SILENCE);
		}

		public Format[] getFormats() {
			return new Format[] { Format.ANY };
		}
	}

	public String getLocalAddress(String media) {
		return rtpFactory != null ? rtpFactory.getBindAddress() : "0.0.0.0";
	}

	public int getLocalPort(String media) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

}
