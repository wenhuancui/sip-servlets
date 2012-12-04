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
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Set;
import javax.sdp.SdpException;
import org.mobicents.media.server.resource.Channel;
import org.mobicents.media.server.resource.ChannelFactory;
import org.mobicents.media.server.spi.Connection;
import org.mobicents.media.server.spi.ConnectionMode;
import org.mobicents.media.server.spi.ResourceUnavailableException;

/**
 *
 * @author kulikov
 */
public class LocalConnectionImpl extends ConnectionImpl {

    private LocalConnectionImpl otherConnection;

    public LocalConnectionImpl(EndpointImpl endpoint, ConnectionMode mode) throws ResourceUnavailableException {
        super(endpoint, mode);
        ConnectionFactory factory = endpoint.getConnectionFactory();
        
        Map<String,ChannelFactory> rxFactories = factory.getRxChannelFactory();
        Set<String> types = rxFactories.keySet();
        
        for (String media : types) {
            ChannelFactory channelFactory = rxFactories.get(media);
            Channel channel = channelFactory.newInstance(endpoint);
            channel.setConnection(this);
            channel.setEndpoint(endpoint);
            endpoint.rxConnect(channel, media);
            rxChannels.put(media, channel);
        }

        Map<String,ChannelFactory> txFactories = factory.getTxChannelFactory();
        types = txFactories.keySet();
        
        for (String media : types) {
            ChannelFactory channelFactory = txFactories.get(media);
            Channel channel = channelFactory.newInstance(endpoint);
            channel.setConnection(this);
            channel.setEndpoint(endpoint);
            endpoint.txConnect(channel, media);
            txChannels.put(media, channel);
        }
        
        this.mode = mode;
    }
    
    public String getLocalDescriptor() {
        return null;
    }

    public String getRemoteDescriptor() {
        return null;
    }

    public void setRemoteDescriptor(String descriptor) throws SdpException, IOException, ResourceUnavailableException {
        
    }
    
    public void setOtherParty(Connection other) throws IOException {
        //hold reference for each other
        this.otherConnection = (LocalConnectionImpl) other;
        otherConnection.otherConnection = this;

        //join channels
        Set<String> types = txChannels.keySet();
        for (String media : types) {
            Channel txChannel = txChannels.get(media);
            Channel rxChannel = otherConnection.rxChannels.get(media);
            
            if (txChannel != null && rxChannel != null) {
                txChannel.connect(rxChannel);
                txChannel.start(); 
                rxChannel.start();
//                this.getEndpoint().txStart(media);
//                otherConnection.getEndpoint().rxStart(media);
            }
            
        }

        types = rxChannels.keySet();
        for (String media : types) {
            Channel rxChannel = rxChannels.get(media);
            Channel txChannel = otherConnection.txChannels.get(media);
            
            if (txChannel != null && rxChannel != null) {
                txChannel.connect(rxChannel);
                txChannel.start();
                rxChannel.start();
//                this.getEndpoint().rxStart(media);
//                otherConnection.getEndpoint().txStart(media);
            }
        }
        
        setMode(mode);        
    }

    @Override
    public void close() {
        if (this.otherConnection == null) {
            return;
        }
        
        Set<String> types = txChannels.keySet();
        for (String media : types) {
            Channel txChannel = txChannels.get(media);
            Channel rxChannel = otherConnection.rxChannels.get(media);
            
            if (txChannel != null && rxChannel != null) {
                txChannel.disconnect(rxChannel);
            }
            
            if (txChannel != null) {
                ((EndpointImpl)getEndpoint()).txDisconnect(txChannel, media);
            }
        }

        types = rxChannels.keySet();
        for (String media : types) {
            Channel rxChannel = rxChannels.get(media);
            Channel txChannel = otherConnection.txChannels.get(media);
            
            if (txChannel != null && rxChannel != null) {
                rxChannel.disconnect(txChannel);
            }
            
            if (rxChannel != null) {
                ((EndpointImpl)getEndpoint()).rxDisconnect(rxChannel, media);
            }
        }
        
        txChannels.clear();
        rxChannels.clear();
        
        otherConnection.otherConnection = null;
        this.otherConnection = null;
        
        super.close();
    }
    
    public long getPacketsReceived(String media) {
        Channel rxChannel = rxChannels.get(media);
        return rxChannel != null ? rxChannel.getPacketsTransmitted() : 0;
    }
    
    public long getPacketsTransmitted(String media) {
        Channel txChannel = txChannels.get(media);
        return txChannel != null ? txChannel.getPacketsTransmitted() : 0;
    }
    
    @Override
    public String toString() {
        return "Local Connection [" + getEndpoint().getLocalName() + ", idx=" + getIndex() + "]";
    }

    public void txStart(String media) {
        if (txChannels.containsKey(media)) {
            txChannels.get(media).start();
        }        
    }

    public void rxStart(String media) {
        if (rxChannels.containsKey(media)) {
            rxChannels.get(media).start();
        }        
    }

    public void txStop(String media) {
        if (txChannels.containsKey(media)) {
            txChannels.get(media).stop();
        }        
    }

    public void rxStop(String media) {
        if (rxChannels.containsKey(media)) {
            rxChannels.get(media).stop();
        }        
    }

    public void setOtherParty(String media, InetSocketAddress address) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
