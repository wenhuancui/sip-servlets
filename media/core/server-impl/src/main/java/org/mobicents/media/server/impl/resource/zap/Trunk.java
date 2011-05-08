/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.mobicents.media.server.impl.resource.zap;

import java.util.ArrayList;
import java.util.Collection;
import org.apache.log4j.Logger;
import org.mobicents.media.server.ConnectionFactory;
import org.mobicents.media.server.spi.Endpoint;
import org.mobicents.media.server.spi.EndpointFactory;
import org.mobicents.media.server.spi.ResourceUnavailableException;
import org.mobicents.media.server.spi.rtp.RtpManager;

/**
 *
 * @author kulikov
 */
public class Trunk implements EndpointFactory {

    private ArrayList<Endpoint> endpoints = new ArrayList();
    private String name;
    private RtpManager rtpFactory;
    private ConnectionFactory connectionFactory;
    private int span;
    private String ranges;
    private int[] firstCIC;
    private boolean linkUp = false;

    private static Logger logger = Logger.getLogger(Trunk.class);
    
    public Trunk() {
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFirstCIC(String cic) {
        String tokens[] = cic.split(",");
        firstCIC = new int[tokens.length];

        for (int i = 0; i < tokens.length; i++) {
            firstCIC[i] = Integer.parseInt(tokens[i]);
        }
    }

    public void setSpan(int span) {
        this.span = span;
    }

    public void setChannels(String ranges) {
        this.ranges = ranges;
    }

    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void setRtpManager(RtpManager rtpFactory) {
        this.rtpFactory = rtpFactory;
    }

    public RtpManager getRtpManager() {
        return this.rtpFactory;
    }


    public void linkUp() {
        if (logger.isInfoEnabled()) {
            logger.info("Received L4 Up event from layer3.");
        }
        this.linkUp = true;
//        this.streamData(_LINK_STATE_UP);
    }

    public void linkDown() {
        if (logger.isInfoEnabled()) {
            logger.info("Received L4 Down event from layer3.");
        }
        this.linkUp = false;
        // FIXME: proper actions here.
        // this.txBuff.clear();
        // this.txBuff.limit(0);
        // this.readBuff.clear();
//        this.streamData(_LINK_STATE_DOWN);
    }

    public void receive(byte[] arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<Endpoint> install() throws ResourceUnavailableException {
        String[] subranges = ranges.split(",");
        for (int i = 0; i < subranges.length; i++) {
            String tokens[] = subranges[i].split("-");
            int low = Integer.parseInt(tokens[0]);
            int high = Integer.parseInt(tokens[1]);

            for (int k = low; k <= high; k++) {
                int zapid = 31 * (span - 1) + k;
                String path = "/dev/dahdi/" + zapid;
                
                DahdiEndpointImpl endpoint = new DahdiEndpointImpl(name + "/" + k, path);
                endpoint.setRtpManager(rtpFactory);
                endpoint.setConnectionFactory(connectionFactory);
                endpoints.add(endpoint);
            }
        }
        return endpoints;
    }

    public void uninstall() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
