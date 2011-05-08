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

package org.mobicents.media.server.ctrl.rtsp;

import java.util.HashMap;

/**
 * 
 * @author amit bhayani
 * 
 */
public class Session {

	private static int GEN = 1;
	private String id;
	private SessionState state = SessionState.INIT;
	private HashMap attributes = new HashMap();

	protected Session() {

		this.id = Integer.toHexString(GEN++);
		if (GEN == Integer.MAX_VALUE) {
			GEN = 1;
		}
	}

	protected String getId() {
		return this.id;
	}

	protected SessionState getState() {
		return state;
	}

	protected void setState(SessionState state) {
		this.state = state;
	}

	public void addAttribute(String name, Object value) {
		attributes.put(name, value);
	}

	public Object getAttribute(String name) {
		return attributes.get(name);
	}

	public void removeAttribute(String name) {
		attributes.remove(name);
	}
}
