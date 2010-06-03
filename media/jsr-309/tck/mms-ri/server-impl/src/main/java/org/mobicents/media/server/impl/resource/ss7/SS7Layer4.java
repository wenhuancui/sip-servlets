package org.mobicents.media.server.impl.resource.ss7;

public interface SS7Layer4 {

	/**
	 * Callback method from lower layers MTP3-. This is called once MTP3
	 * determines that link is stable and is able to send/receive messages
	 * properly. This method should be called only once. Every linkup event.
	 */
	public void linkUp();

	/**
	 * Callback method from MTP3 layer, informs upper layers that link is not
	 * operable.
	 */
	public void linkDown();

	/**
	 * 
	 * @param service
	 *            - type, this is generaly content of service part - contains
	 *            constant defined for ISUP, SCCP or any other
	 * @param subservice
	 *            - as above, it contains other 4 buts of SIO byte.
	 * @param msgBuff
	 */
	public void receive(int service, int subservice, byte[] msgBuff);

	// there is no such API in intel... ;[
}
