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
package org.mobicents.media.server.spi;

import org.mobicents.media.server.spi.clock.Timer;
import java.io.Serializable;

import org.mobicents.media.Component;
import org.mobicents.media.MediaSink;
import org.mobicents.media.MediaSource;
import org.mobicents.media.format.AudioFormat;

/**
 * The basic implementation of the endpoint.
 *
 * An Endpoint is a logical representation of a physical entity, such as an
 * analog phone or a channel in a trunk. Endpoints are sources or sinks of data
 * and can be physical or virtual. Physical endpoint creation requires hardware
 * installation while software is sufficient for creating a virtual Endpoint.
 * An interface on a gateway that terminates a trunk connected to a PSTN switch
 * is an example of a physical Endpoint. An audio source in an audio-content
 * server is an example of a virtual Endpoint.
 *
 * @author Oleg Kulikov.
 * @author amit.bhayani
 */
public interface Endpoint extends Serializable {

    public final static AudioFormat LINEAR_AUDIO = 
            new AudioFormat(AudioFormat.LINEAR, 8000, 16, 1,AudioFormat.LITTLE_ENDIAN, AudioFormat.SIGNED);
    public final static AudioFormat PCMA = new AudioFormat(AudioFormat.ALAW, 8000, 8, 1);
    public final static AudioFormat PCMU = new AudioFormat(AudioFormat.ULAW, 8000, 8, 1);
    public final static AudioFormat SPEEX = new AudioFormat(AudioFormat.SPEEX, 8000, 8, 1);
    public final static AudioFormat G729 = new AudioFormat(AudioFormat.G729, 8000, 8, 1);
    public final static AudioFormat GSM = new AudioFormat(AudioFormat.GSM, 8000, 8, 1);
    public final static AudioFormat DTMF = new AudioFormat("telephone-event/8000");
    
    /**
     * Gets the local name attribute.
     *
     * @return the local name.
     */
    public String getLocalName();

    public Timer getTimer();
    public void setTimer(Timer timer);
    
    /**
     * Gets local IP Address to which specified media channel is attached
     * 
     * @param media the media type
     * @return IP adress in text form.
     */
    public String getLocalAddress(String media);
    
    /**
     * Gets the port number to which specified media channel is attached
     * 
     * @param media the media type
     * @return port number
     */
    public int getLocalPort(String media);
    
    /**
     * Starts endpoint.
     */
    public void start() throws ResourceUnavailableException ;
    
    /**
     * Terminates endpoint's execution.
     */
    public void stop();
    
    /**
     * Creates new connection with specified mode.
     *
     * @param mode the constant which identifies mode of the connection to be created.
     */
    public Connection createConnection()
            throws TooManyConnectionsException, ResourceUnavailableException;

    /**
     * Creates new connection with specified mode.
     *
     * @param mode the constant which identifies mode of the connection to be created.
     */
    public Connection createLocalConnection()
            throws TooManyConnectionsException, ResourceUnavailableException;

    /**
     * Deletes specified connection.
     *
     * @param connectionID the identifier of the connection to be deleted.
     */
    public void deleteConnection(String connectionID);

    /**
     * Deletes all connection associated with this Endpoint.
     */
    public void deleteAllConnections();

    /**
     * Indicates does this endpoint has connections.
     *
     * @return true if endpoint has connections.
     */
    public boolean hasConnections();
    
    public Connection getConnection(String connectionID);
    public Component getComponent(String resourceName);
    
    /**
     * Shows is this endpoint in use
     * 
     * @return true if this endpoint is in use.
     */
    public boolean isInUse();
    
    /**
     * Marks this endpoint as used/unsed.
     * 
     * @param inUse true if endpoint is in use.
     */
    public void setInUse(boolean inUse);
    
    /**
     * Register NotificationListener to listen for <code>MsNotifyEvent</code>
     * which are fired due to events detected by Endpoints like DTMF. Use above
     * execute methods to register for event passing appropriate
     * <code>RequestedEvent</code>
     * 
     * @param listener
     */
    public void addNotificationListener(NotificationListener listener);

    /**
     * Remove the NotificationListener
     * 
     * @param listener
     */
    public void removeNotificationListener(NotificationListener listener);

    /**
     * Register <code>ConnectionListener</code> to listen for changes in MsConnection state
     * handled by this Endpoint
     * 
     * @param listener
     */
    public void addConnectionListener(ConnectionListener listener);
    /**
     * Removes the ConnectionListener
     * 
     * @param listener
     */
    public void removeConnectionListener(ConnectionListener listener);
    
    public int getConnectionIndex();
    
    public Endpoint clone();
    
    public void setLocalName(String localName);

    public MediaSink getSink(MediaType media) ;

    public MediaSource getSource(MediaType media);
    
    public String describe(MediaType mediaType) throws ResourceUnavailableException;
    
}
