/*
 * Mobicents, Communications Middleware
 *
 * Copyright (c) 2008, Red Hat Middleware LLC or third-party
 * contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors. All third-party contributions are
 * distributed under license by Red Hat Middleware LLC.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 *
 * Boston, MA 02110-1301 USA
 */
package org.mobicents.media.server.impl.resource.zap;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.log4j.Logger;
import org.mobicents.media.Server;
import org.mobicents.media.server.impl.clock.LocalTask;
import org.mobicents.media.server.spi.clock.Task;
import org.mobicents.protocols.link.DataLink;
import org.mobicents.protocols.link.LinkState;
import org.mobicents.protocols.link.LinkStateListener;
import org.mobicents.protocols.ss7.mtp.Mtp1;
import org.mobicents.protocols.ss7.mtp.Mtp2;
import org.mobicents.protocols.ss7.mtp.Mtp3;
import org.mobicents.protocols.ss7.mtp.Mtp3Listener;
import org.mobicents.protocols.stream.api.SelectorKey;
import org.mobicents.protocols.stream.api.SelectorProvider;
import org.mobicents.protocols.stream.api.Stream;
import org.mobicents.protocols.stream.api.StreamSelector;

/**
 *
 * @author kulikov
 */
public class MTPUser implements Task, Mtp3Listener, LinkStateListener {

    private String name;
    /** Originated point code */
    private int opc;
    /** Destination point code */
    private int dpc;
    /** physical channels */
    private List<Mtp1> channels;
    /** MTP layer 3 */
    private Mtp3 mtp3;
    private Logger logger = Logger.getLogger(MTPUser.class);
    private volatile boolean isActive = false;
    private LocalTask task;

    private DataLink dataLink;
    private StreamSelector selector;
    
    private InetSocketAddress address;
    private InetSocketAddress remote;
    
    private int localPort;
    private int remotePort;
    
    private String localAddress;
    private String remoteAddress;
    
    private byte[] rxBuffer = new byte[279];
    private ConcurrentLinkedQueue<byte[]> queue = new ConcurrentLinkedQueue();
    
    /**
     * Gets the name of the linkset.
     *
     * @return the name of the linkset
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the linkset.
     *
     * @param name the alhanumeric name of the linkset.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Assigns originated point code
     *
     * @param opc the value of the originated point code in decimal format
     */
    public void setOpc(int opc) {
        this.opc = opc;
    }

    /**
     * Assigns destination point code.
     *
     * @param dpc the destination point code value in decimal format.
     */
    public void setDpc(int dpc) {
        this.dpc = dpc;
    }

    /**
     * @return the opc
     */
    public int getOpc() {
        return opc;
    }

    /**
     * @return the dpc
     */
    public int getDpc() {
        return dpc;
    }

    public void setLocalAddress(String address) {
        this.localAddress = address;
    }
    
    public String getLocalAddress() {
        return localAddress;
    }
    
    public void setLocalPort(int port) {
        this.localPort = port;
    }
    
    public int getLocalPort() {
        return localPort;
    }
    
    public void setRemoteAddress(String address) {
        this.remoteAddress = address;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }
    
    public void setRemotePort(int port) {
        this.remotePort = port;
    }

    public int getRemotePort() {
        return remotePort;
    }
    
    /**
     * Assigns signalling channels.
     *
     * @param channels the list of available physical channels.
     */
    public void setChannels(List<Mtp1> channels) {
        this.channels = channels;
    }

    /**
     * Activates link set.
     */
    public void start() throws IOException, ClassNotFoundException {
        try {
            address = new InetSocketAddress(localAddress, localPort);
            remote = new InetSocketAddress(remoteAddress, remotePort);
            
            dataLink = DataLink.open(address, remote);
            dataLink.setListener(this);
            selector = SelectorProvider.getSelector("org.mobicents.protocols.link.SelectorImpl");
            dataLink.register(selector);
            dataLink.activate();
            
            logger.info("Created MTP layer 3");
            ArrayList<Mtp2> linkset = new ArrayList();
            for (Mtp1 channel : channels) {
                Mtp2 link = new Mtp2(name + "-" + channel.getCode(), channel);
                linkset.add(link);
            }
            // assigning physical channel
            mtp3 = new Mtp3(name);
            mtp3.setDpc(dpc);
            mtp3.setOpc(opc);
            mtp3.setLinks(linkset);
            mtp3.addMtp3Listener(this);
            logger.info("Point codes are configured");

            // set user part
//			if (mtp3Listener != null) {
//				mtp3.addMtp3Listener(mtp3Listener);
//			}

            // starting layer 3
            mtp3.start();
            this.isActive = true;
            task = Server.scheduler.execute(this);


        } catch (RuntimeException re) {
            re.printStackTrace();
            throw re;
        }
    }

    /**
     * Deactivates linkset.
     */
    public void stop() {
        task.cancel();
        //to stop IO selection just cancel selector's key.

        //and close all channels
//        mtp3.close();
    }

    public void cancel() {
        this.isActive = false;
    }

    public boolean isActive() {
        return this.isActive;
    }

    public int perform() {
        //Poll MTP3
        mtp3.run();
       
        try {
            //Polling DataLink for RX
            Collection<SelectorKey> keys = selector.selectNow(StreamSelector.OP_READ, 20);
            for (SelectorKey key : keys) {
                int len = key.getStream().read(rxBuffer);
                mtp3.send(rxBuffer, len);
            }
        } catch (IOException e) {
            return -1;
        }
        //Polling DataLink for TX

        try {
            Collection<SelectorKey> keys = selector.selectNow(StreamSelector.OP_WRITE, 20);
            for (SelectorKey key : keys) {
                if (!queue.isEmpty()) {
                    byte[] msu = queue.poll();
                    key.getStream().write(msu);
                }
            }
        } catch (IOException e) {
        }
        return 1;
    }


    public void linkUp() {
        dataLink.activate();
        queue.clear();
    }

    public void linkDown() {
        dataLink.deactivate();
    }

    public void receive(byte[] data) {
        queue.offer(data);
    }

    public void onStateChange(LinkState state) {
        if (state == LinkState.ACTIVE) {
            queue.clear();
        }
    }
}
