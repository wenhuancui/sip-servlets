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

package org.jdiameter.server.api;

import org.jdiameter.api.ApplicationId;
import org.jdiameter.api.LocalAction;
import org.jdiameter.api.Realm;

import java.util.Set;

/**
 * This interface describe extends methods of base class
 */
public interface IRouter extends org.jdiameter.client.api.router.IRouter{

    /**
     * Add real to realm table
     * @param name name of realm
     * @param applicationId applicationId of realm
     * @param localAction local action of realm
     * @param dynamic on/off dynamic
     * @param expirationTime experation time of record
     * @param peers array of host names
     * @return Realm instance
     */
    Realm addRealm(String name, ApplicationId applicationId, LocalAction localAction, boolean dynamic, long expirationTime,  String... peers);

    /**
     * Remove realm
     * @param name name of realm
     * @return removed realm
     */
    Realm remRealm(String name);

    /**
     * Return set of realms
     * @return
     */
    Set<Realm> getRealms();

    /**
     * Set network instance
     * @param network network instance
     */
    void setNetWork(INetwork network);
}
