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

package org.mobicents.protocols.stream.impl.tlv;

public enum LinkStatus {

	LinkUp((byte) 1), LinkDown((byte) 0), Query((byte) 2), StateAck((byte) 3);

	private byte status;
	private LinkStatus acked;

	LinkStatus(byte b) {
		this.status = b;
	}

	/**
	 * @return the status
	 */
	public byte getStatus() {
		return status;
	}

	public void setAcked(LinkStatus fromByte) {
		this.acked = fromByte;

	}

	public LinkStatus getAcked() {
		return this.acked;

	}

	public LinkStatus getFromByte(byte b) {
		if (b == 1) {
			return LinkUp;

		} else if (b == 0) {
			return LinkDown;
		} else if (b == 2) {
			return Query;
		} else if (b == 3) {
			return StateAck;
		} else {
			throw new IllegalArgumentException("No state associated with: " + b);
		}
	}

}
