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

import java.io.IOException;
import java.util.List;



public interface Mtp3 extends Mtp2Listener{

	// XXX: Note, q704 and 707 define different timers under the same
	// names......

	public final static int _TIMEOUT_T1_SLTM = 12;
	public final static int _TIMEOUT_T2_SLTM = 90;


	public void setLinkSets(List<LinkSet> linkSets);

	public void start() throws IOException;

	public void stop();

	/**
	 * This should be called upper layer to set listener.
	 * 
	 * @param layer4
	 */
	public void setLayer4(SS7Layer4 layer4);

	public boolean isL3Debug();

	public void setL3Debug(boolean l3Debug);
	
	//FIXME: move to linkset
	//public void setOpc(int opc);

	//public void setDpc(int dpc);

	public boolean send(byte sls,byte linksetId, int si, int ssf, byte[] array);
}
