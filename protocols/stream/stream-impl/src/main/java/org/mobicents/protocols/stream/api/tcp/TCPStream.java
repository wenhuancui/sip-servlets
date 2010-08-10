/**
 * 
 */
package org.mobicents.protocols.stream.api.tcp;

import java.net.InetSocketAddress;

import org.mobicents.protocols.stream.api.Stream;

/**
 * @author baranowb
 *
 */
public interface TCPStream extends Stream{

	/**
	 * Marks this streamer as ready(if true). This is also indicated on remote side.
	 * @param b
	 */
	public void ready(boolean b);
	/**
	 * Determines if stream connection is ready.
	 * @return
	 */
	public boolean isReady();
	
	public InetSocketAddress getAddress();
	
	public InetSocketAddress getRemoteAddress();
	
	public StreamState getState();
	
}
