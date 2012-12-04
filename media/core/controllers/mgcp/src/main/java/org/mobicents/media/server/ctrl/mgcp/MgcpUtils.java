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

package org.mobicents.media.server.ctrl.mgcp;

import jain.protocol.ip.mgcp.message.parms.ConnectionMode;

/**
 * Used as Util to convert some MGCP values to Media Server SPI.
 * 
 * @author kulikov
 * @author amit bhayani
 */
public class MgcpUtils {

	public org.mobicents.media.server.spi.ConnectionMode getMode(ConnectionMode mode) {
		switch (mode.getConnectionModeValue()) {
		case ConnectionMode.RECVONLY:
			return org.mobicents.media.server.spi.ConnectionMode.RECV_ONLY;
		case ConnectionMode.SENDONLY:
			return org.mobicents.media.server.spi.ConnectionMode.SEND_ONLY;
		case ConnectionMode.SENDRECV:
			return org.mobicents.media.server.spi.ConnectionMode.SEND_RECV;
		case ConnectionMode.INACTIVE:
			return org.mobicents.media.server.spi.ConnectionMode.INACTIVE;
		default:
			return null;
		}
	}

	public ConnectionMode getMode(org.mobicents.media.server.spi.ConnectionMode mode) {
		switch (mode) {
		case INACTIVE:
			return ConnectionMode.Inactive;
		case SEND_ONLY:
			return ConnectionMode.SendOnly;
		case RECV_ONLY:
			return ConnectionMode.RecvOnly;
		case SEND_RECV:
			return ConnectionMode.SendRecv;
		default:
			return null;
		}
	}
}
