/*
 * JBoss, Home of Professional Open Source
 * Copyright XXXX, Red Hat Middleware LLC, and individual contributors as indicated
 * by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a full listing
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 */
package org.mobicents.media.server.spi.resource.ss7;

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
	public void receive(byte sls,byte linksetId, int service, int subservice, byte[] msgBuff);


}
