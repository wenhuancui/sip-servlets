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

package org.jdiameter.client.api.router;

import org.jdiameter.api.AvpDataException;
import org.jdiameter.api.InternalException;
import org.jdiameter.api.RouteException;
import org.jdiameter.client.api.IMessage;
import org.jdiameter.client.api.controller.IPeer;
import org.jdiameter.client.api.controller.IPeerTable;

/**
 * This class describe Router functionality
 */
public interface IRouter  {

    /**
     * Return peer from inner peer table by predefined pameters
     * @param message message with routed avps
     * @param manager instance of peer manager
     * @return peer instance
     * @throws RouteException
     * @throws AvpDataException
     */
    IPeer getPeer(IMessage message, IPeerTable manager) throws RouteException, AvpDataException;

    /**
     * Return realm of peer by fqdn
     * @param fqdn host name
     * @return realm of peer
     */
    String getRealmForPeer(String fqdn);

    /**
     * Register route information by received request. This information will be used
     * during answer routing.
     * @param request request
     */
    void registerRequestRouteInfo(IMessage request);

    /**
     * Return Request route info
     * @param hopByHopIndentifier hop By Hop Indentifier
     * @return Array (host and realm)
     */
    String[] getRequestRouteInfo(long hopByHopIndentifier);

    /**
     * Update redirect information
     * @param answer redirect answer message
     * @throws InternalException
     * @throws RouteException
     */
    void updateRedirectInformation(IMessage answer) throws InternalException, RouteException;

    /**
     * Start inner time facilities
     */
    void start();

    /**
     * Stop inner time facilities
     */
    void stop();

    /**
     * Release all resources
     */
    void destroy();

}
