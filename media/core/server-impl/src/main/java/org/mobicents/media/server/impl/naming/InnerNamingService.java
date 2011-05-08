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

package org.mobicents.media.server.impl.naming;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jboss.beans.metadata.api.annotations.Install;
import org.jboss.beans.metadata.api.annotations.Uninstall;
import org.mobicents.media.server.spi.Endpoint;
import org.mobicents.media.server.spi.EndpointState;
import org.mobicents.media.server.spi.NamingService;
import org.mobicents.media.server.spi.ResourceUnavailableException;

/**
 * 
 * @author kulikov
 */
public class InnerNamingService implements NamingService {

    private HashMap<String, Endpoint> endpoints = new HashMap<String, Endpoint>();
    
    private int endpointCount = 0;
    private final static Logger logger = Logger.getLogger(InnerNamingService.class);

    public void start() {
        logger.info("Started");
    }

    public void stop() {
        logger.info("Stopped");
        Collection<Endpoint> list = endpoints.values();
        for (Endpoint endpoint : list) {
            endpoint.stop();
            endpointCount = 0;
            logger.info("Stopped endpoint: local name = " + endpoint.getLocalName());
        }
    }

    @Install
    public void addEndpoint(Endpoint endpoint) throws ResourceUnavailableException {
        endpoints.put(endpoint.getLocalName(), endpoint);
        endpointCount++;
    }

    @Uninstall
    public void removeEndpoint(Endpoint endpoint) {
        endpoints.remove(endpoint.getLocalName());
        endpointCount--;
        logger.info("Unregistered endpoint: local name " + endpoint.getLocalName());
    }

    public Endpoint lookup(String endpointName) throws ResourceUnavailableException {
        if (endpointName.endsWith("$")) {
            return findAny(endpointName); // findAny(endpointName);
        } else {
            return find(endpointName, true);
        }
    }

    public Endpoint lookup(String endpointName, boolean allowInUse) throws ResourceUnavailableException {
        if (endpointName.endsWith("$")) {
            return findAny(endpointName); // findAny(endpointName);
        } else {
            return find(endpointName, allowInUse);
        }
    // return null;
    }

    public synchronized Endpoint findAny(String name) throws ResourceUnavailableException {
        // TODO : Can name have two '$'? In this case the search will be
        // slow once we add logic for this

        Endpoint endpt = null;
        String prefix = name.substring(0, name.indexOf("$") - 1);
        String suffix = null;
        if (name.indexOf("$") + 1 > name.length()) {
            suffix = name.substring(name.indexOf("$") + 1, name.length());
        }

        Set<String> keys = endpoints.keySet();
        for (String key : keys) {
            if (key.startsWith(prefix)) {
                if (suffix != null) {
                    if (key.contains(suffix)) {
                        endpt = endpoints.get(key);
                        if (endpt.getState() == EndpointState.READY) {
                            //endpt.setInUse(true);
                            return endpt;
                        }
                    }
                } else {
                    endpt = endpoints.get(key);
                    if (endpt.getState() == EndpointState.READY) {
//                        endpt.setInUse(true);
                        return endpt;
                    }
                }
            }
        } // end of for

        if (endpt == null || endpt.getState() != EndpointState.READY) {
            throw new ResourceUnavailableException("No Endpoint found for " + name);
        }

        return endpt;

    }

    public synchronized Endpoint find(String name, boolean allowInUse) throws ResourceUnavailableException {
        Endpoint endpt = endpoints.get(name);
        if (endpt == null) {
            throw new ResourceUnavailableException("No Endpoint found for " + name);
        }
        if (endpt.getState() == EndpointState.READY && !allowInUse) {
            throw new ResourceUnavailableException("Endpoint " + name + " is in use");
        } else {
//            endpt.setInUse(true);
            return endpt;
        }
    }

    protected Collection<String> getNames(Collection<String> prefixes, NameToken token, Iterator<NameToken> tokens) {
        ArrayList<String> list = new ArrayList();
        if (!tokens.hasNext()) {
            while (token.hasMore()) {
                String s = token.next();
                for (String prefix : prefixes) {
                    list.add(prefix + "/" + s);
                }
            }
            return list;
        } else {
            Collection<String> newPrefixes = new ArrayList();
            while (token.hasMore()) {
                String s = token.next();
                for (String prefix : prefixes) {
                    newPrefixes.add(prefix + "/" + s);
                }
            }
            return getNames(newPrefixes, tokens.next(), tokens);
        }
    }

    public int getEndpointCount() {
        return this.endpointCount;
    }
}
