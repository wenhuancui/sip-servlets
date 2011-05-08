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

package org.rhq.plugins.jbossas5;

import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.managed.api.ManagedComponent;
import org.jboss.managed.api.ManagedDeployment;
import org.jboss.managed.api.ManagedObject;

/**
 * @author Ian Springer
 */
public class StandaloneConvergedSipWebApplicationDiscoveryComponent extends StandaloneManagedDeploymentDiscoveryComponent {
	
	private final Log log = LogFactory.getLog(this.getClass());
	
	@Override
    protected boolean accept(ManagedDeployment managedDeployment) {
    	boolean accept = super.accept(managedDeployment);
    	if(!accept) {
    		return accept;
    	}
    	if(log.isDebugEnabled()) {
    		getInfo(managedDeployment);
    	}
    	if(managedDeployment.getManagedObjectNames().contains("SipApplicationNameMO")) {
    		accept = true;
    	} else {
    		accept = false;
    	}
    	
        return accept;
    }

	private void getInfo(ManagedDeployment managedDeployment) {
		log.debug("managed deployment "  + managedDeployment.getName());
    	for(Entry<String, ManagedComponent> entry : managedDeployment.getComponents().entrySet()) {
    		log.debug("component key " + entry.getKey() + " name " + entry.getValue().toString());
    	}
    	for(String objectName : managedDeployment.getManagedObjectNames()) {
    		log.debug("managed object Name " + objectName);
    	}
    	for(Entry<String, ManagedObject> entry : managedDeployment.getManagedObjects().entrySet()) {
    		log.debug("managed Object key " + entry.getKey() + " name " + entry.getValue().toString());
    	}    	
	}
}
