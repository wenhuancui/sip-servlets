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

package org.rhq.plugins.jbossas5.connection;

import javax.naming.InitialContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.jboss.deployers.spi.management.ManagementView;
import org.jboss.deployers.spi.management.deploy.DeploymentManager;
import org.jboss.profileservice.spi.ProfileService;

/**
 * A connection provider for connecting to a local Profile Service.
 *
 * @author Ian Springer
 */
public class LocalProfileServiceConnectionProvider extends AbstractProfileServiceConnectionProvider {
    private static final String PROFILE_SERVICE_LOCAL_JNDI_NAME = "java:ProfileService";

    private final Log log = LogFactory.getLog(this.getClass());

    protected BasicProfileServiceConnection doConnect() {
        log.debug("Connecting to Profile Service via local JNDI...");
        InitialContext initialContext = createInitialContext(null);
        ProfileService profileService = (ProfileService) lookup(initialContext, PROFILE_SERVICE_LOCAL_JNDI_NAME);
        ManagementView managementView = profileService.getViewManager();
        DeploymentManager deploymentManager = profileService.getDeploymentManager();
        return new BasicProfileServiceConnection(this, profileService, managementView, deploymentManager);
    }

    protected void doDisconnect() {
        return;
    }
}
