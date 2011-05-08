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

import java.util.List;

/**
 * This interface introduces a capability to work with a network.
 * You can get instance of this interface over stack instance:
 * <code>
 * if (stack.isWrapperFor(RealmTable.class)) {
 *       RealmTable realmTabke = stack.unwrap(RealmTable.class);
 *       .....
 * }
 * </code>
 * @version 1.5.1 Final
 */

public interface RealmTable extends Wrapper {

    /**
     * Return different network statistics
     * @param realmName realmName
     * @return network statistics
     */
    Statistic getStatistic(String realmName);

    /**
     * Return realm entry
     * @param realmName realm name
     * @param applicationId application id associated with realm
     * @return realm entry
     */
    Realm getRealm(String realmName, ApplicationId applicationId);

    /**
     * Return no mutable list of elements realm table
     * @return list of elements realm table
     */
    List<Realm> getAllRealms();

    /**
     * Add new realm to realm table
     * @param realmName name of realm
     * @param applicationId application id of realm
     * @param action action of realm
     * @param dynamic commCode of realm
     * @param expirationTime expiration time of realm
     * @return instance of created realm
     */
    Realm addRealm(String realmName, ApplicationId applicationId, LocalAction action, boolean dynamic, long expirationTime);

    /**
     * Remove realm from realm table
     * @param realmName name of realm
     * @return realm
     */
    Realm removeRealm(String realmName);
}
