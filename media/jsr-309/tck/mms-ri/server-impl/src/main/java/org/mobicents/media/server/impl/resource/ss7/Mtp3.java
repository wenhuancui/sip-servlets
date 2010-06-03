/*
 * Mobicents, Communications Middleware
 * 
 * Copyright (c) 2008, Red Hat Middleware LLC or third-party
 * contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Middleware LLC.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 *
 * Boston, MA  02110-1301  USA
 */

package org.mobicents.media.server.impl.resource.ss7;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * 
 * @author kulikov
 * @author baranowb
 */
public class Mtp3 {

	private final static int LINK_MANAGEMENT = 0;
	private final static int LINK_TESTING = 1;
	private final static int SERVICE_SCCP = 3;
	private final static int SERVICE_ISUP = 5;
	
	private SS7Layer4 layer4;
	private Mtp2 layer2;

	/**
	 * Flag indicating if we should notify upper layer
	 */
	private boolean l4IsUp = false;
	private int destinationPointCode;
	private int originationPointCode;
	private int signalingLinkSelection;
	// ss7 has subservice as 1, q704 shows the same iirc
	//private int subservice = -1;
	//private int service;
	private static final int _SERVICE_SLTM = 1;
	private static final int _SERVICE_TRA = 0;
	private static final int _DEFAULT_SUB_SERVICE_TRA = 0xC;

	// //////////////////////////
	// SLTM section for inits //
	// //////////////////////////
	private final static byte[] SLTM_PATTERN = new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x0F };
	private int sltmTries = 0;

	private String name;

	private Logger logger = Logger.getLogger(Mtp3.class);

	public Mtp3(String name, Mtp1 layer1) {
		this.name = name;
		this.layer2 = new Mtp2(name);
		this.layer2.setLayer1(layer1);
		this.layer2.setLayer3(this);
	}

	public void setOpc(int opc) {
		this.originationPointCode = opc;
	}

	public void setDpc(int dpc) {
		this.destinationPointCode = dpc;
	}

	public void setSls(int sls) {
		this.signalingLinkSelection = sls;
	}
	/**
	 * This should be called upper layer to set listener.
	 * @param layer4
	 */
	public void setLayer4(SS7Layer4 layer4) {
		this.layer4 = layer4;
	}
	
	public Mtp2 getLayer2() {
		return layer2;
	}

	public void start() throws IOException {
		Mtp2.mtpTimer.schedule(new Runnable() {

			public void run() {
				try {
					layer2.start();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}, 5, TimeUnit.SECONDS);
		
	}

	public void stop() {
		stop_T1_SLTM();
		stop_T2_SLTM();
		
		layer2.stop();
	}

	/**
	 * 
	 * @param sio
	 *            service information octet.
	 * @param msg
	 *            service information field;
	 */
	public void onMessage(int sio, byte[] sif) {
		int subserviceIndicator = sio >>> 4;
		int piority = (sio >>> 4 & 0x03);
		int si = sio & 0x0f;

		// int dpc = (sif[0] & 0xff | ((sif[1] & 0x3f) << 8));
		// int opc = ((sif[1] & 0xC0) >> 6) | ((sif[2] & 0xff) << 2) | ((sif[3]
		// & 0x0f) << 10);
		// int sls = (sif[3] & 0xf0) >>> 4;
		int dpc = _getFromSif_DPC(sif, 0);
		int opc = _getFromSif_OPC(sif, 0);
		int sls = _getFromSif_SLS(sif, 0);

		if (logger.isDebugEnabled() && isL3Debug()) {
			// logger.debug("Received MSSU [si=" + si + ", dpc=" + dpc +
			// ", opc=" + opc + ", sls=" + sls + "]");
			layer2.trace("Received MSSU [si=" + si + ", dpc=" + dpc + ", opc=" + opc + ", sls=" + sls + "]");
		}
		switch (si) {
		case LINK_MANAGEMENT:
			break;
		case LINK_TESTING:
			int h0 = sif[4] & 0x0f;
			int h1 = (sif[4] & 0xf0) >>> 4;

			int len = (sif[5] & 0xf0) >>> 4;

			if (h0 == 1 && h1 == 1) {
				if (logger.isDebugEnabled() && isL3Debug()) {
					// logger.debug("SLTM received");
					layer2.trace("Received SLTM");
				}
				// receive SLTM from remote end
				// create response
				byte[] slta = new byte[len + 7];
				slta[0] = (byte) sio;

				writeRoutingLabel(slta);
				slta[5] = 0x021;
				// +1 cause we copy LEN byte also.
				System.arraycopy(sif, 5, slta, 6, len + 1);

				if (logger.isDebugEnabled() && isL3Debug()) {
					if (logger.isDebugEnabled()) {
						// logger.debug("Link(" + name + ") SLTA received");
						// lets validate SLTA we send. this is inverted
						// procedure that we do on SLTA
						int remote_OPC = opc;
						int remote_DPC = dpc;
						int remote_SLS = sls;

						// now lets get that from sif. shift by one, we include
						// SIO in byte[] buff
						int slta_OPC = _getFromSif_OPC(slta, 1);
						int slta_DPC = _getFromSif_DPC(slta, 1);
						int slta_SLS = _getFromSif_SLS(slta, 1);
						// check pattern?

						if (remote_OPC != slta_DPC || remote_DPC != slta_OPC || remote_SLS != slta_SLS) {
							layer2.trace("Failed check on sending SLTA, values do not match, remote SLTM/SLTA check will fail\n" + "remote OPC = "
									+ remote_OPC + ", SLTA DPC = " + slta_DPC + ", remote DPC = " + remote_DPC + ", SLTA OPC = " + slta_OPC
									+ ", remote SLS = " + remote_SLS + ", SLTA SLS = " + slta_SLS);
						}
					}

					// logger.debug("Responding with SLTA");

					layer2.trace("Responding with SLTA");
				}
				layer2.queue(slta);
			} else if (h0 == 1 && h1 == 2) {
				// receive SLTA from remote end
				if (logger.isDebugEnabled() && isL3Debug()) {

					layer2.trace("Received SLTA");
				}
				// stop Q707 timer T1
				this.stop_T1_SLTM();
				StringBuilder sb = new StringBuilder();
				// check contidions for acceptance
				boolean accepted = true;
				if (opc != this.destinationPointCode) {
					if (logger.isDebugEnabled() && isL3Debug()) {
						sb.append("\nSLTA Acceptance failed, OPC = ").append(opc).append(" ,of SLTA does not match local DPC = ").append(
							this.destinationPointCode);
					}
					accepted = false;
				}

				if (dpc != this.originationPointCode) {
					if (logger.isDebugEnabled() && isL3Debug()) {
						sb.append("\nSLTA Acceptance failed, DPC = ").append(dpc).append(" ,of SLTA does not match local OPC = ").append(
							this.originationPointCode);
					}
					accepted = false;
				}

				if (!checkPattern(sif, SLTM_PATTERN)) {
					if (logger.isDebugEnabled() && isL3Debug()) {
						sb.append("\nSLTA Acceptance failed, sif pattern = ").append(Arrays.toString(sif)).append(
							" ,of SLTA does not match local pattern = ").append(Arrays.toString(SLTM_PATTERN));
					}
					accepted = false;
				}

				if (sls != this.signalingLinkSelection) {
					if (logger.isDebugEnabled() && isL3Debug()) {
						sb.append("\nSLTA Acceptance failed, sls  = ").append(sls).append(" ,of SLTA does not match local sls = ").append(
							this.signalingLinkSelection);
					}
					accepted = false;
				}

				if (accepted) {
					
					// reset counter.
					sltmTries = 0;
					if(!this.l4IsUp && this.layer4!=null)
					{
						if (logger.isDebugEnabled() && isL3Debug()) {
							layer2.trace("XXX Notify layer 4 on success SLTM handshake");
						}
						this.l4IsUp = true;
						this.layer4.linkUp();
					}
				} else {
					if (logger.isDebugEnabled() && isL3Debug()) {
						layer2.trace("SLTA acceptance failed!!! Reason: " + sb);
					}
					performSLTARetryProcedure();
				}

			} else {

				if (logger.isEnabledFor(Level.WARN) && isL3Debug()) {
					layer2.trace("XXX Unexpected message type");
				}
			}
			break;
		case SERVICE_SCCP:
			if (logger.isEnabledFor(Level.WARN) && isL3Debug()) {
				layer2.trace("XXX MSU Indicates SCCP");
			}
			//lets create byte[] which is actuall upper layer msg.
			//msbBuff.len = sif.len - 4 (routing label), after routing label there should be msg code
			byte[] msgBuff = new byte[sif.length-4];
			System.arraycopy(sif, 4, msgBuff, 0, msgBuff.length);
			if(this.layer4!=null)
			{
				this.layer4.receive(SERVICE_SCCP, subserviceIndicator,msgBuff);
			}
			break;
		case SERVICE_ISUP:
			if (logger.isEnabledFor(Level.WARN) && isL3Debug()) {
				layer2.trace("XXX MSU Indicates ISUP");
			}
			break;
			default: 
				if (logger.isEnabledFor(Level.WARN) && isL3Debug()) {
					layer2.trace("XXX MSU Indicates UNKNOWN SERVICE!!!!!!!!!!!: "+layer2.dump(sif, sif.length,false));
				}
				break;
		}
	}

	public void send(int service,int subservice,byte[] msg) {
		
		if(!this.l4IsUp)
		{
			//??
			return;
		}
		byte[] buffer = new byte[5+msg.length];
		writeRoutingLabel(buffer);
		buffer[0] =  (byte) ((service & 0x0F) | ((subservice & 0x0F) << 4));
		System.arraycopy(msg, 0, buffer, 5, msg.length);
		if(isL3Debug())
		{
			this.layer2.trace("Scheduling MSU: "+this.layer2.dump(buffer, buffer.length,false));
		}
		this.layer2.queue(buffer);
		
	}

	public void failed() {
		try {
			if (this.l4IsUp && this.layer4 != null) {
				this.layer4.linkUp();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		resetForInservice();
	}

	public void inService() {
		this.resetForInservice();
		sendTRA();
		sendSLTM();
		// schedule SLTM.
		// start ACK supervision and redo of SLTM procedure after time.

	}

	private void resetForInservice() {
		this.stop_T1_SLTM();
		this.stop_T2_SLTM();
		this.sltmTries = 0;
		this.l4IsUp = false;
	}
	
	
	private void sendTRA() {
		//int subservice = this.subservice;
		//if (subservice == -1) {
		int	subservice = _DEFAULT_SUB_SERVICE_TRA;
		//}
		byte[] buffer = new byte[6];
		writeRoutingLabel(buffer);
		// buffer[0] = (byte) (_SERVICE_TRA | ( subservice << 4));
		buffer[0] = (byte) 0xC0;
		// H0 and H1, see Q.704 section 15.11.2+
		buffer[5] = 0x17;
		if (logger.isDebugEnabled() && isL3Debug()) {
			// logger.debug("Link (" + name + ") Queue SLTM");
			layer2.trace("Queue TRA");
		}
		this.layer2.queue(buffer);
	}

	private void sendSLTM() {
		//int subservice = this.subservice;
		//if (subservice == -1) {
		//int	subservice = _DEFAULT_SUB_SERVICE_TRA;
		//}
		byte[] sltm = new byte[7 + SLTM_PATTERN.length];
		// sltm[0] = (byte) (_SERVICE_SLTM | ( this.subservice << 4));
		// sltm[0] = _SERVICE_SLTM;
		sltm[0] = (byte) 0xC1; // 1100 0001
		writeRoutingLabel(sltm);
		sltm[5] = 0x11;
		sltm[6] = (byte) (SLTM_PATTERN.length << 4);
		System.arraycopy(SLTM_PATTERN, 0, sltm, 7, SLTM_PATTERN.length);

		if (logger.isDebugEnabled() && isL3Debug()) {
			// logger.debug("Link (" + name + ") Queue SLTM");
			layer2.trace("Queue SLTM");
		}
		layer2.queue(sltm);
		this.sltmTries++;
		this.start_T1_SLTM();
		this.start_T2_SLTM();

	}

	private void writeRoutingLabel(byte[] sif) {
		sif[1] = (byte) destinationPointCode;
		sif[2] = (byte) (((destinationPointCode >> 8) & 0x3F) | ((originationPointCode & 0x03) << 6));
		sif[3] = (byte) (originationPointCode >> 2);
		sif[4] = (byte) (((originationPointCode >> 10) & 0x0F) | ((signalingLinkSelection & 0x0F) << 4));
	}

	// -6 cause we have in sif, sio+label+len of pattern - thats 1+4+1 = 6
	private final static int SIF_PATTERN_OFFSET = 6;

	private boolean checkPattern(byte[] sif, byte[] pattern) {

		if (sif.length - SIF_PATTERN_OFFSET != pattern.length) {
			return false;
		}
		for (int i = 0; i < pattern.length; i++) {
			if (sif[i + SIF_PATTERN_OFFSET] != pattern[i]) {
				return false;
			}
		}
		return true;
	}

	// /////////////////////////////
	// Timers and Future handles //
	// /////////////////////////////

	// XXX: Note, q704 and 707 define different timers under the same
	// names......

	private final static int TIMEOUT_T1_SLTM = 12;
	private final static int TIMEOUT_T2_SLTM = 90;
	private T1Action_SLTM t1Action_SLTM = new T1Action_SLTM();
	private T2Action_SLTM t2Action_SLTM = new T2Action_SLTM();

	private ScheduledFuture T1_SLTM;
	private ScheduledFuture T2_SLTM;
	

	private void stop_T1_SLTM() {
		ScheduledFuture f = this.T1_SLTM;
		if (f != null && !f.isCancelled()) {

			this.T1_SLTM = null;
			f.cancel(false);
		}
	}

	private void stop_T2_SLTM() {
		ScheduledFuture f = this.T2_SLTM;
		if (f != null && !f.isCancelled()) {

			this.T2_SLTM = null;
			f.cancel(false);
		}
	}

	private void start_T1_SLTM() {
		stop_T1_SLTM();
		this.T1_SLTM = layer2.mtpTimer.schedule(this.t1Action_SLTM, TIMEOUT_T1_SLTM, TimeUnit.SECONDS);
	}

	private void start_T2_SLTM() {
		stop_T2_SLTM();
		this.T2_SLTM = layer2.mtpTimer.schedule(this.t2Action_SLTM, TIMEOUT_T2_SLTM, TimeUnit.SECONDS);
	}

	private class T1Action_SLTM implements Runnable {
		public void run() {
			// so we can cleanly reschedule.
			T1_SLTM = null;
			performSLTARetryProcedure();
		}

	}

	private class T2Action_SLTM implements Runnable {
		public void run() {
			sendSLTM();
		}
	}

	private void performSLTARetryProcedure() {
		if (sltmTries == 1) {
			// we have second chance.
			if (logger.isEnabledFor(Level.ERROR) && isL3Debug()) {
				layer2.trace("No valid SLTA received within Q.707_T1, trying again.");
			}
			sltmTries++;
			sendSLTM();
		} else if (sltmTries > 1) {
			// this is failure, link must go down.
			if (logger.isEnabledFor(Level.ERROR) && isL3Debug()) {
				layer2.trace("No valid SLTA received within Q.707_T1, faulting link.....");
			}
			layer2.failLink();
			sltmTries = 0;
		}

	}

	// //////////////////
	// Helper methods //
	// //////////////////

	private static final int _getFromSif_DPC(byte[] sif, int shift) {
		int dpc = (sif[0 + shift] & 0xff | ((sif[1 + shift] & 0x3f) << 8));
		return dpc;
	}

	private static final int _getFromSif_OPC(byte[] sif, int shift) {
		int opc = ((sif[1 + shift] & 0xC0) >> 6) | ((sif[2 + shift] & 0xff) << 2) | ((sif[3 + shift] & 0x0f) << 10);
		return opc;
	}

	private static final int _getFromSif_SLS(byte[] sif, int shift) {
		int sls = (sif[3 + shift] & 0xf0) >>> 4;
		return sls;
	}

	////////////////
	// Debug Part //
	////////////////
	private boolean l3Debug;
	public boolean isL3Debug() {
		return l3Debug;
	}

	public void setL3Debug(boolean l3Debug) {
		this.l3Debug = l3Debug;
	}
}
