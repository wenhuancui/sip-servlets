package org.mobicents.media.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

import javax.sdp.MediaDescription;
import javax.sdp.SdpException;
import javax.sdp.SdpFactory;
import javax.sdp.SessionDescription;

import org.mobicents.media.Component;
import org.mobicents.media.Format;
import org.mobicents.media.MediaSource;
import org.mobicents.media.server.impl.rtp.RtpFactory;
import org.mobicents.media.server.impl.rtp.RtpSocket;
import org.mobicents.media.server.spi.Connection;
import org.mobicents.media.server.spi.ConnectionListener;
import org.mobicents.media.server.spi.ConnectionMode;
import org.mobicents.media.server.spi.ConnectionState;
import org.mobicents.media.server.spi.Endpoint;
import org.mobicents.media.server.spi.NotificationListener;
import org.mobicents.media.server.spi.ResourceUnavailableException;

public class RtspRtpConnectionImpl implements Connection {

    private String id;
    private static int GEN = 1;
    
    private HashMap<String, RtpSocket> rtpSockets = new HashMap<String, RtpSocket>();
    private RtspEndpointImpl endpoint;
    
    private String remoteDescriptor;
    private String localDescriptor;
    private SessionDescription localSdp;
    
    private SdpFactory sdpFactory;
    private HashMap<String, String> tracks = new HashMap();
    
    public RtspRtpConnectionImpl(RtspEndpointImpl endpoint, ConnectionMode mode) throws ResourceUnavailableException {
        this.endpoint = endpoint;
        sdpFactory = endpoint.getSdpFactory();
        
        // obtain rtp factories
        RtpFactory rtpFactory = endpoint.getRtpFactory();

        // obtain list of supported media types
        Collection<String> mediaTypes = endpoint.getMediaTypes();

        // creating channel for each media type
        for (String media : mediaTypes) {
            // create rtp socket
            RtpSocket socket = null;
            try {
                socket = rtpFactory.getRTPSocket(media);
                socket.setFormat(0, Format.ANY);
                // save reference
                rtpSockets.put(media, socket);
            } catch (Exception e) {
                throw new ResourceUnavailableException(e);
            }
        }

        createLocalDescriptor();
    }

    private void createLocalDescriptor() {
        SessionDescription sdp = null;
        String userName = "MediaServer";

        long sessionID = System.currentTimeMillis() & 0xffffff;
        long sessionVersion = sessionID;

        String networkType = javax.sdp.Connection.IN;
        String addressType = javax.sdp.Connection.IP4;

        String address = rtpSockets.get("audio").getLocalAddress();

        try {
            sdp = sdpFactory.createSessionDescription();
            sdp.setVersion(sdpFactory.createVersion(0));
            sdp.setOrigin(sdpFactory.createOrigin(userName, sessionID, sessionVersion, networkType, addressType,
                    address));
            sdp.setSessionName(sdpFactory.createSessionName("session"));
            sdp.setConnection(sdpFactory.createConnection(networkType, addressType, address));
        } catch (SdpException e) {
        }
        localDescriptor = sdp.toString();
        
            // encode formats
        Collection<String> mediaTypes = endpoint.getMediaTypes();
        for (String mediaType : mediaTypes) {
            String md = endpoint.getSdp(mediaType);
            String trackID = getTrackID(md);
            if (trackID != null) {
                tracks.put(trackID, mediaType);
            }
            localDescriptor += md;
        }
        endpoint.tracks = tracks;
    }
    
    private String getTrackID(String md) {
        int pos = md.indexOf("trackID=");
        if (pos < 0) {
            return null;
        }
        return md.substring(pos, pos + 9);
    }
    
    public String getLocalDescriptor() {
        return this.localDescriptor;
    }

    public long getPacketsReceived(String media) {
        return rtpSockets.get(media).getSendStream().getBytesReceived();
    }

    public long getPacketsTransmitted(String media) {
        return rtpSockets.get(media).getReceiveStream().getPacketsTransmitted();
    }

    public String getRemoteDescriptor() {
        return this.remoteDescriptor;
    }

    public void txStart(String media) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void rxStart(String media) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void txStop(String media) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void rxStop(String media) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setOtherParty(Connection other) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");

    }

    public void setRemoteDescriptor(String descriptor) throws SdpException, IOException, ResourceUnavailableException {
        this.remoteDescriptor = descriptor;

//        if (getState() != ConnectionState.HALF_OPEN && getState() != ConnectionState.OPEN) {
//            throw new IllegalStateException("State is " + getState());
//        }

        // parse session descriptor
        SessionDescription sdp = sdpFactory.createSessionDescription(descriptor);

        // determine address of the remote party
        InetAddress address = InetAddress.getByName(sdp.getConnection().getAddress());

        // analyze media descriptions
        Vector<MediaDescription> mediaDescriptions = sdp.getMediaDescriptions(false);
        for (MediaDescription md : mediaDescriptions) {
            // determine media type
            String mediaType = md.getMedia().getMediaType();
            
            //we do not parse formats because we agree on any
            
            RtpSocket rtpSocket = rtpSockets.get(mediaType);

            // assign preffered format
            rtpSocket.setFormat(0, Format.ANY);

            int port = md.getMedia().getMediaPort();
            rtpSocket.setPeer(address, port);
            
            MediaSource source = endpoint.getSource(mediaType);
            source.connect(rtpSocket.getSendStream());


            //FIXME : This will start the AVPlayer and Recorder too :(
            rtpSocket.getSendStream().start();
//            setMode(mode);
        }

//        setState(ConnectionState.OPEN);
    }

    public void close() {
        Set<String> mediaTypes = rtpSockets.keySet();
        for (String media : mediaTypes) {
            RtpSocket socket = rtpSockets.get(media);

            socket.getReceiveStream().stop();
            socket.getSendStream().stop();
            
            MediaSource source = endpoint.getSource(media);
            source.stop();
            
            if (source.isConnected()) {
                source.disconnect(socket.getSendStream());
            }

            socket.release();
        }


        rtpSockets.clear();
    }

    public void error(Exception e) {
        endpoint.deleteAllConnections();
    }

    /**
     * Generates unique identifier for this connection.
     * 
     * @return hex view of the unique integer.
     */
    private String genID() {
        GEN++;
        if (GEN == Integer.MAX_VALUE) {
            GEN = 1;
        }
        return Integer.toHexString(GEN);
//        return (new UID()).toString();
    }

    public String getId() {
        return this.id;
    }

    public ConnectionState getState() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getLifeTime() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setLifeTime(int lifeTime) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ConnectionMode getMode() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setMode(ConnectionMode mode) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Endpoint getEndpoint() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addListener(ConnectionListener listener) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addNotificationListener(NotificationListener listener) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeListener(ConnectionListener listener) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeNotificationListener(NotificationListener listener) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Component getComponent(String name, int chanID) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setOtherParty(String media, InetSocketAddress address) throws IOException {
        String mediaType = media.startsWith("track")? tracks.get(media) : media;
        RtpSocket socket = rtpSockets.get(mediaType);
        socket.setPeer(address.getAddress(), address.getPort());
        
        MediaSource source = endpoint.getSource(mediaType);
        if (source != null) {
            socket.getSendStream().connect(endpoint.getSource(mediaType));
//            socket.getSendStream().start();
        }
    }
    
}
