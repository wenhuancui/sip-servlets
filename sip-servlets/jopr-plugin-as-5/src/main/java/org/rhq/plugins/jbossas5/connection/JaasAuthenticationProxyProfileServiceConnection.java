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

import java.lang.reflect.Proxy;

import org.jboss.deployers.spi.management.ManagementView;
import org.jboss.deployers.spi.management.deploy.DeploymentManager;
import org.jboss.profileservice.spi.ProfileService;

/**
 * @author Ian Springer
 */
public class JaasAuthenticationProxyProfileServiceConnection extends AbstractProfileServiceConnection {    
    private ProfileService profileService;
    private ManagementView managementView;
    private DeploymentManager deploymentManager;

    protected JaasAuthenticationProxyProfileServiceConnection(RemoteProfileServiceConnectionProvider connectionProvider,
        ProfileService profileService, ManagementView managementView, DeploymentManager deploymentManager) {
        super(connectionProvider);

        JaasAuthenticationInvocationHandler profileServiceInvocationHandler =
                new JaasAuthenticationInvocationHandler(profileService,
                    connectionProvider.getPrincipal(), connectionProvider.getCredentials());
        this.profileService = (ProfileService)Proxy.newProxyInstance(this.getClass().getClassLoader(),
                new Class[] {ProfileService.class}, profileServiceInvocationHandler);

        JaasAuthenticationInvocationHandler managementViewInvocationHandler =
                new JaasAuthenticationInvocationHandler(managementView,
                    connectionProvider.getPrincipal(), connectionProvider.getCredentials());
        this.managementView = (ManagementView)Proxy.newProxyInstance(this.getClass().getClassLoader(),
                new Class[] {ManagementView.class}, managementViewInvocationHandler);

        JaasAuthenticationInvocationHandler deploymentManagerInvocationHandler =
                new JaasAuthenticationInvocationHandler(deploymentManager,
                    connectionProvider.getPrincipal(), connectionProvider.getCredentials());
        this.deploymentManager = (DeploymentManager)Proxy.newProxyInstance(this.getClass().getClassLoader(),
                new Class[] {DeploymentManager.class}, deploymentManagerInvocationHandler);
    }

    public ProfileService getProfileService() {
        return this.profileService;
    }

    public ManagementView getManagementView() {
        return this.managementView;
    }

    public DeploymentManager getDeploymentManager() {
        return this.deploymentManager;
    }
}