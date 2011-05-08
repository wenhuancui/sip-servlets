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

package org.jdiameter.api;

/**
 * The Realm class implements rows in the Diameter Realm routing table.
 * @version 1.5.1 Final
 */

public abstract class Realm {

    protected String name;
    protected ApplicationId appId;
    protected LocalAction action;
    protected boolean dynamic;
    protected long expirationTime;

    protected Realm(String name, ApplicationId appId, LocalAction action, boolean dynamic, long expirationTime) {
        this.name = name;
        this.appId = appId;
        this.action = action;
        this.dynamic = dynamic;
        this.expirationTime = expirationTime;
    }

    /**
     * Return name of this realm
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Return applicationId associated with this realm
     * @return applicationId
     */
    public ApplicationId getApplicationId() {
        return appId;
    }

    /**
     * Return realm local action for this realm
     * @return realm local action
     */
    public LocalAction getLocalAction() {
        return action;
    }

    /**
     * Return list of real peers
     * @return array of realm peers
     */
    public abstract String[] getPeerHosts();

    /**
     * Append new host (peer) to this realm
     * @param host name of peer host
     */
    public abstract void addPeerName(String host);

    /**
     * Remove peer from this realm
     * @param host name of peer host
     */
    public abstract void removePeerName(String host);

    /**
     * Return true if this realm is dynamic updated
     * @return true if this realm is dynamic updated
     */
    public boolean isDynamic() {
        return dynamic;
    }

    /**
     * Return expiration time for this realm in milisec
     * @return expiration time
     */
    public long getExpirationTime() {
        return expirationTime;
    }
}
