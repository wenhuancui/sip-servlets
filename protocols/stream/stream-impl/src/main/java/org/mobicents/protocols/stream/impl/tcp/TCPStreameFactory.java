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
