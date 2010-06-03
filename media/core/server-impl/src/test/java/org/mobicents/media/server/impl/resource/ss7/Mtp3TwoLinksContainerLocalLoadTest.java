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
package org.mobicents.media.server.impl.resource.ss7;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.mobicents.media.server.spi.resource.ss7.Mtp3;
import org.mobicents.media.server.spi.resource.ss7.Utils;

/**
 * Simple load test for mtp[1-3] layers set up to loop. Uses two distinct links
 * 
 * @author baranowb
 * 
 */
public class Mtp3TwoLinksContainerLocalLoadTest extends TwoLinksTestHarrness {

	private final static Logger logger = Logger.getLogger(Mtp3TwoLinksContainerLocalLoadTest.class);

	public static final byte _SLS_ = 0;
	public static final byte _LINKSET_ID_ = 0;
	// we mockup SCCP :)
	public static final int _SCCP_SERVICE = 3;
	public static final int _SCCP_SUB_SERVICE = 0;

	// lower layer. retrieved from container mockup.
	
	private Layer4LoopDataHandler loopHandler;
	private Layer4DataHandler dataHandler;

	public Mtp3TwoLinksContainerLocalLoadTest(String name) {
		super(name);
	}

	private static final String _FILE_LOG4J_CONF = "mtp-log4j.xml";
	@Override
	protected void setUp() throws Exception {
		
		
		this.dataHandler = new Layer4DataHandler(null, (byte) 0);
		this.loopHandler = new Layer4LoopDataHandler(null);
		super.setUp();


	}

	@Override
	protected void shutdown() {
		
		super.shutdown();
	}

	@Override
	protected void tearDown() throws Exception {
		super.mtp3_leg1.setLayer4(null);
		super.mtp3_leg2.setLayer4(null);
		super.tearDown();
	}

	private static final int _MAX_WAIT_COUNT_ = 10;

	private static final int _WAIT_TIMEOUT_ = 5000;

	private static final int _MSGS_NUM_ = 127;

	private static final int _WAIT_FOR_DATA_RESOLUTION_ = 5;

	/**
	 * This test sends _MSGS_NUM_ msgs and awaits them. It does not run in loop.
	 * @throws IOException 
	 */
	public void testOneTimeBufferFill() throws IOException {
/*		int _DATA_LENGTH_ = 25;

		
		if (ensureLinkUpsForTest()) {

			long startTime = System.currentTimeMillis();
			for (int i = 0; i < _MSGS_NUM_; i++) {
				byte[] b = new byte[_DATA_LENGTH_];
				b[b.length - 1] = (byte) i;
				int cCount = (int) (Math.random() * 5);
				for (int j = 0; j < cCount; j++) {
					int index = (j * cCount + 1) % b.length;
					if (index == b.length - 1)
						index--;
					b[index] = (byte) (Math.random() * 100);
				}
				// FIXME: and how we send in case of initial msgs :)
				// ....
				
				if (!this.dataHandler.send(_SLS_, _LINKSET_ID_, _SCCP_SERVICE, _SCCP_SUB_SERVICE, b)) {
					fail("Failed to send buffer through first leg.");
					return;
				}
			}
			
			
			this.dataHandler.performDataComparison(startTime, _MSGS_NUM_, _DATA_LENGTH_, _WAIT_TIMEOUT_, new Comparator<byte[]>() {

				public int compare(byte[] o1, byte[] o2) {
					int index1 = o1[o1.length-1];
					int index2 = o2[o2.length-1];
					return (int) (index1 - index2);
				}

			});
		}
 */ 
	}

	public void testContinousBufferFill() throws InterruptedException, IOException {
/*		byte _DATA_LENGTH_ = 10;

		if (ensureLinkUpsForTest()) {

			long startTime = System.currentTimeMillis();
			this.dataHandler.msgLength = _DATA_LENGTH_;
			this.dataHandler.start();
			// DATA count/ms count number of msgs
			long sleepTime = (_DATA_LENGTH_ + 12) * 10000;
			logger.info("Sleeping for: " + sleepTime + "ms");

			Thread.currentThread().sleep(sleepTime);

			this.dataHandler.performDataComparison(startTime, -1, _DATA_LENGTH_, _WAIT_TIMEOUT_);

		}
 */ 
	}

	

	private class Layer4DataHandler implements ExtendedSS7Layer4, Runnable {
		private final Logger logger = Logger.getLogger(Layer4DataHandler.class);
		private Mtp3 leg;
		private boolean linksUp;
		private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		private Future future;
		// sent and received MAY not be in order, its not guaranted
		private ArrayList<byte[]> sentData = new ArrayList<byte[]>();
		private ArrayList<byte[]> receivedData = new ArrayList<byte[]>();
		private byte msgLength = 50;

		public Layer4DataHandler(Mtp3 leg, byte msgLength) {
			super();
			this.leg = leg;
			this.msgLength = msgLength;
			if (leg != null) {
				leg.setLayer4(this);
			}
		}

		public void start() {

			long scheduleResolution = (msgLength + 12); // 12 ?
			this.future = this.executor.scheduleAtFixedRate(this, 100, scheduleResolution, TimeUnit.MILLISECONDS);

		}

		public void stop() {
			if (this.future != null) {
				this.future.cancel(false);
				this.future = null;
			}
		}

		// ///////////////////////
		// Callbacks from mtp3 //
		// ///////////////////////
		public void linkDown() {
			this.linksUp = false;
		}

		public void linkUp() {
			this.linksUp = true;
		}

		/* (non-Javadoc)
		 * @see org.mobicents.media.server.impl.resource.ss7.ExtendedSS7Layer4#isLinkUp()
		 */
		public boolean isLinkUp() {
			return this.linksUp;
		}
		public void receive(byte sls, byte linksetId, int service, int subservice, byte[] msgBuff) {
			if (!linksUp) {
				logger.error("Received msg when link is down....");
			}
			
			this.receivedData.add(msgBuff);
		}

		public void run() {
			// this should be run depending on actual data we do send
			int _DATA_LENGTH_ = msgLength;

			byte[] b = new byte[_DATA_LENGTH_];

			int cCount = (int) (Math.random() * 100);
			for (int j = 0; j < cCount; j++) {
				int index = (j * cCount + 1) % b.length;
				// -4, see encodeIndex and decodeIndex
				if (index >= b.length - 1)
					index--;
				encodeIndex(this.sentData.size(), b);
			}
			//Utils.getInstance().append("Sending: "+Utils.dump(b, b.length,false));
			if (!this.send(_SLS_, _LINKSET_ID_, _SCCP_SERVICE, _SCCP_SUB_SERVICE, b)) {
				fail("Failed to send buffer through first leg.");
				return;
			}

		}

		public boolean send(byte sls, byte linksetId, int si, int ssf, byte[] b) {
			if (linksUp) {
				if (this.leg.send(sls, linksetId, si, ssf, b)) {
					this.sentData.add(b);
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		}

		// /////////////////////////////////////////////////////
		// Method called to test correctness send vs received //
		// //////////////////////////////////////////////////////
		public void performDataComparison(long startTime, long _MSGS_NUM_, long _DATA_LENGTH_, long _WAIT_TIMEOUT_) {
			this.performDataComparison(startTime, _MSGS_NUM_, _DATA_LENGTH_, _WAIT_TIMEOUT_, new Comparator<byte[]>() {

				public int compare(byte[] o1, byte[] o2) {
					int index1 = decodeIndex(o1);
					int index2 = decodeIndex(o2);
					return (int) (index1 - index2);
				}

			});
		}

		
		//Actually for long nums it will flip. but we dont care, just number of msgs must match.
		private int decodeIndex(byte[] o1) {
			// int has 4B
			int x = 0 | (o1[o1.length - 4] << 24) | o1[o1.length - 3] << 16 | o1[o1.length - 2] << 8 | o1[o1.length - 1];

			return x;
		}

		private void encodeIndex(int index, byte[] b) {
			b[b.length - 1] = (byte) (index & 0x0F);
			b[b.length - 2] = (byte) ((index >> 8) & 0xFF);
			b[b.length - 3] = (byte) ((index >> 16) & 0xFF);
			b[b.length - 4] = (byte) ((index >> 24) & 0xFF);

		}

		public void performDataComparison(long startTime, long _MSGS_NUM_,
				long _DATA_LENGTH_, long _WAIT_TIMEOUT_,
				Comparator<byte[]> sorter) {
			if (_MSGS_NUM_ != -1)
			{
				while (this.receivedData.size() != _MSGS_NUM_) {
					// lets check;
					long currentTime = System.currentTimeMillis();
					if (currentTime > (startTime + _WAIT_TIMEOUT_)) {
						// we waited to looong, some messages did not make it ;[
						fail("Some messages did not make it. Sent data count: "
								+ this.sentData.size()
								+ ", received data count: "
								+ this.receivedData.size());
						return;
					} else {
						try {
							Thread.currentThread().sleep(
									_WAIT_FOR_DATA_RESOLUTION_);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}else
			{
				if(this.receivedData.size()!=this.sentData.size())
				{
					fail("Some messages did not make it. Sent data count: "
							+ this.sentData.size()
							+ ", received data count: "
							+ this.receivedData.size());
					return;
				}
			}
	
			
			// if we are here, data has been transfered.
			long endTime = System.currentTimeMillis();

			// now lets count time
			long dataCount = _MSGS_NUM_ * (_DATA_LENGTH_ + 12); // +12 ?
			logger.info("Time spent on data send: " + (endTime - startTime)
					+ "ms, data count: " + dataCount);
			logger.info("Checking data correctnes...");

			//sort both, we care only for content.
			Collections.sort(this.receivedData, sorter);
			Collections.sort(this.sentData, sorter);
			for (int index = 0; index < this.receivedData.size(); index++) {
				byte[] sentBuff = this.sentData.get(index);
				byte[] revBuff = this.receivedData.get(index);
				if (Arrays.equals(sentBuff, revBuff)) {

				} else {
					StringBuffer sb = new StringBuffer();
					for (int ii = 0; ii < this.receivedData.size(); ii++) {
						byte bb[] = this.receivedData.get(ii);
						sb.append("Received["
								+ bb
								+ "]"
								+ Utils.getInstance()
										.dump(bb, bb.length, false) + "\n");
					}
					for (int ii = 0; ii < this.sentData.size(); ii++) {
						byte bb[] = this.sentData.get(ii);
						sb.append("Sent["
								+ bb
								+ "]"
								+ Utils.getInstance()
										.dump(bb, bb.length, false) + "\n");
					}
					fail("Send and receive buffer do not match. Index[" + index
							+ "]. Sent Data: \n"
							+ Utils.dump(sentBuff, sentBuff.length, false)
							+ "\nReceived:\n"
							+ Utils.dump(revBuff, revBuff.length, false) + "\n"
							+ sb);
					return;
				}
			}
		}

		public void setLayer3(Mtp3 mtp3) {
			this.leg = mtp3;
			
		}
	}

	private class Layer4LoopDataHandler implements ExtendedSS7Layer4 {
		private final Logger logger = Logger.getLogger(Layer4LoopDataHandler.class);
		private Mtp3 leg;
		private boolean linksUp;

		public Layer4LoopDataHandler(Mtp3 leg) {
			super();
			this.leg = leg;
			if (leg != null) {
				leg.setLayer4(this);
			}
		}

		// ///////////////////////
		// Callbacks from mtp3 //
		// ///////////////////////
		public void linkDown() {
			this.linksUp = false;
			
		}

		public void linkUp() {
			this.linksUp = true;

		}

		/* (non-Javadoc)
		 * @see org.mobicents.media.server.impl.resource.ss7.ExtendedSS7Layer4#isLinkUp()
		 */
		public boolean isLinkUp() {
			return this.linksUp;
		}

		public void receive(byte sls, byte linksetId, int service, int subservice, byte[] msgBuff) {
			// this is simple loop, we just send back
			if (!linksUp) {
				logger.error("Received msg when link is down....");
				return;
			}
			if (!leg.send(sls, linksetId, service, subservice, msgBuff)) {
				fail("Failed to loop message: " + Utils.dump(msgBuff, msgBuff.length, false));
			}

		}

		public void setLayer3(Mtp3 mtp3) {
			this.leg = mtp3;
			
		}

	}

	@Override
	protected ExtendedSS7Layer4 getLeg1DataHandler() {
		return this.loopHandler;
	}

	@Override
	protected ExtendedSS7Layer4 getLeg2DataHandler() {
		
		return this.dataHandler;
	}

	@Override
	protected int getMaxWaitCountForLinkUp() {
		return _MAX_WAIT_COUNT_;
	}

	@Override
	protected int getWaitTimeForLinkUp() {
		return _WAIT_TIMEOUT_;
	}
}
