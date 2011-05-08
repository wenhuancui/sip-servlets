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

package org.mobicents.protocols.stream.impl.tcp;


import java.net.InetSocketAddress;

import org.mobicents.protocols.stream.api.Stream;
import org.mobicents.protocols.stream.api.tcp.TCPStream;

public class TCPStreameFactory {

	private TCPStreameFactory() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * Creates streamer implementation which awaits connection from other instance.
	 * @param local
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static TCPStream create(InetSocketAddress local) throws IllegalArgumentException
	{
		return new ServerTCPStreamImpl(local);
	}
	
	
	/**
	 * Creates streamer implementation which seeks connection to remote instance.
	 * @param local
	 * @param remote
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static TCPStream create(InetSocketAddress local, InetSocketAddress remote) throws IllegalArgumentException
	{
		return new ClientTCPStreamImpl(remote,local);
	}
	
}
