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

public interface Mtp2 {

	final static int MTP2_OUT_OF_SERVICE = 0;
	/**
	 * Initial state of IAC phase, we send "O" here. "E","O","N" have never been
	 * received.
	 */
	final static int MTP2_NOT_ALIGNED = 1;
	/**
	 * Second state of IAC, we received one of: E,O,N. Depending on state we
	 * send E or N.
	 */
	final static int MTP2_ALIGNED = 2;
	/**
	 * Third state, entered from ALIGNED on receival of N or E
	 */
	final static int MTP2_PROVING = 3;
	/**
	 * Etnered on certain condition when T4 fires.
	 */
	final static int MTP2_ALIGNED_READY = 4;
	/**
	 * In service state, entered from ALIGNED_READY on FISU/MSU
	 */
	final static int MTP2_INSERVICE = 5;
	
	
	public boolean queue(byte[] msg);

	public void _startLink() throws IOException;

	public void _stopLink();

	public void _closeLink();

	public void failLink();

	public byte getSls();

	public LinkSet getLinkSet();

	public void trace(String msg);

	public void threadTick(long thisTickStamp);

	public boolean isEmergency();

	public void setEmergency(boolean emergency);

	public void restartSltmTries();

	public int incrementSltmTries();

	public int getSltmTries();

	public int getSubService();

	public void setSubService(int subservice);

	public void setT1_SLTMTimerAction(Runnable r);

	public void setT2_SLTMTimerAction(Runnable r);

	public void setLayer1(Mtp1 layer1);

	public void setLayer3(Mtp3 layer3);

	public boolean isL2Debug();

	public void setL2Debug(boolean l2Debug);

	public boolean isEnableDataTrace();

	public void setEnableDataTrace(boolean enableDataTrace);

	public boolean isEnableSuTrace();

	public void setEnableSuTrace(boolean enableSuTrace);

	/**
	 * Returns current state of MTP2 link. It can have on of following values:
	 * <ul>
	 * <li>{@link #MTP2_OUT_OF_SERVICE}</li>
	 * <li>{@link #MTP2_NOT_ALIGNED}</li>
	 * <li>{@link #MTP2_ALIGNED}</li>
	 * <li>{@link #MTP2_ALIGNED_READY}</li>
	 * <li>{@link #MTP2_PROVING}</li>
	 * <li>{@link #MTP2_PROVING}</li>
	 * </ul>
	 */
	public int getState();
	
	// //////////////////////
	// MTP3 hack for MTP2 //
	// //////////////////////
	/**
	 * Starts/restarts T17 for this link
	 */
	public void start_T17();

	/**
	 * Stops T17 timer for this link
	 */
	public void stop_T17();

	/**
	 * 
	 * @return <ul>
	 *         <li><b>true</b> - if T17 is already running</li>
	 *         <li><b>false</b> - if T17 is not running</li>
	 *         </ul>
	 */
	public boolean isT17();

	public void stop_T1_SLTM();

	public void stop_T2_SLTM();

	public void start_T1_SLTM();

	public void start_T2_SLTM();

	public boolean isT1_SLTM();

	public boolean isT2_SLTM();
	
	public void setLinkSet(LinkSet linkSet);
}
