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

package org.mobicents.media;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.apache.log4j.Logger;
import org.mobicents.media.server.impl.naming.InnerNamingService;
import org.mobicents.media.server.impl.naming.NameParser;
import org.mobicents.media.server.impl.naming.NameToken;
import org.mobicents.media.server.spi.Endpoint;
//import org.mobicents.media.server.spi.NamingService;
import org.mobicents.media.server.spi.EndpointFactory;
import org.mobicents.media.server.spi.ResourceUnavailableException;

/**
 *
 * @author kulikov
 */
public class EndpointManager {
    private ArrayList<EndpointFactory> endpoints = new ArrayList();
    
    private InnerNamingService namingService;
    
    private Logger logger = Logger.getLogger(EndpointManager.class);
    
    public EndpointManager() {
    }
    
    public void setNamingService(InnerNamingService namingService) throws ResourceUnavailableException {
        this.namingService = namingService;
    }
    
    
    public void activate() throws ResourceUnavailableException {
        for (EndpointFactory factory : endpoints) {
            Collection<Endpoint> list = factory.install();
            for (Endpoint e : list) {
                e.start();
                logger.info("Started endpoint " + e.getLocalName());
                namingService.addEndpoint(e);
            }
        }
    }
    
    public void addEndpoint(EndpointFactory endpoint) throws ResourceUnavailableException {
        endpoints.add(endpoint);
        logger.info("Installed Endpoint Factory : " + endpoint);
    }
    
    public void removeEndpoint(EndpointFactory endpoint) {
    }
    
}
