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

package org.mobicents.media.server.ctrl.mgcp.evt;

import jain.protocol.ip.mgcp.message.parms.RequestedAction;

import org.mobicents.media.server.spi.MediaType;
import org.mobicents.media.server.spi.events.NotifyEvent;

/**
 * 
 * @author kulikov
 */
public class DefaultEventDetector extends EventDetector {

	// private ActionNotify actionNotify;

	public DefaultEventDetector(MgcpPackage pkgName, String eventName, int eventID,
			String params, RequestedAction[] actions) {
		super(pkgName, eventName, eventID, params, actions);
	}

	public DefaultEventDetector(MgcpPackage mgcpPackage, String eventName,
			int eventID, String params,
			RequestedAction[] actions, Class interface1, MediaType type) {
		this(mgcpPackage, eventName, eventID, params, actions);
		super._interface = interface1;
		super.mediaType = type;
	}

	@Override
	public void performAction(NotifyEvent event, RequestedAction action) {

		// this check is not required since each component/resoruce gets
		// dedicated detector!
		// if (!event.getSource().getName().matches(this.getResourceName())) {
		// return;
		// }

		if (event.getEventID() != this.getEventID()) {
			return;
		}

		// @TODO implement action selector
		getRequest().sendNotify(this.getEventName());

	}

}
