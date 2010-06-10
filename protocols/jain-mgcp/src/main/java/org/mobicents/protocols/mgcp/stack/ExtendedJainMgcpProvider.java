/**
 * 
 */
package org.mobicents.protocols.mgcp.stack;

import jain.protocol.ip.mgcp.JainMgcpProvider;

/**
 * Provides method to send events in async way. Note that errors wont be visible in application.
 * @author baranowb
 *
 */
public interface ExtendedJainMgcpProvider extends JainMgcpProvider{
	
	/**
	 * Enqueues events in stack buffer. Current thread does not loose CPU for I/O and other operations. On next stacks worker thread/s buffer is sent.
	 * @param arg0
	 * @throws java.lang.IllegalArgumentException
	 */
	public void sendAsyncMgcpEvents(jain.protocol.ip.mgcp.JainMgcpEvent[] arg0) throws java.lang.IllegalArgumentException;
}
