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

import java.util.Arrays;

import org.apache.log4j.Logger;

import org.mobicents.media.server.spi.resource.ss7.Mtp3;
import org.mobicents.media.server.spi.resource.ss7.Utils;

public class Mtp3TwoLinksContainerMessageLocalTest extends TwoLinksTestHarrness {
	public Mtp3TwoLinksContainerMessageLocalTest(String name) {
		super(name);

	}

	private static final int _MAX_WAIT_COUNT_ = 10;

	private static final int _WAIT_TIMEOUT_ = 5000;

	private Layer4DataHandler handler;
	private Layer4LoopDataHandler loopHandler;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.media.server.impl.resource.ss7.TwoLinksTestHarrness#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		this.handler = new Layer4DataHandler();
		this.loopHandler = new Layer4LoopDataHandler();
		super.setUp();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.media.server.impl.resource.ss7.TwoLinksTestHarrness#tearDown
	 * ()
	 */
	@Override
	protected void tearDown() throws Exception {
		// TODO Auto-generated method stub
		super.tearDown();
	}

	public void testLessThan63() {
/*		this.handler.msgLength = 50;
		if (ensureLinkUpsForTest())
		{
			this.handler.sendMsg();
			if(this.handler.waitForMsg(_WAIT_TIMEOUT_))
			{
				this.handler.validate();
			}
			
		}
 */ 
	}

	public void test62() {
/*		
		this.handler.msgLength = 57;
		if (ensureLinkUpsForTest())
		{
			this.handler.sendMsg();
			if(this.handler.waitForMsg(_WAIT_TIMEOUT_))
			{
				this.handler.validate();
			}
			
		}
 */ 
	}

	public void test63() {
/*		this.handler.msgLength = 58;
		if (ensureLinkUpsForTest())
		{
			this.handler.sendMsg();
			if(this.handler.waitForMsg(_WAIT_TIMEOUT_))
			{
				this.handler.validate();
			}
			
		}
 */ 
	}

	public void test64() {
/*            
		this.handler.msgLength = 59;
		if (ensureLinkUpsForTest())
		{
			this.handler.sendMsg();
			if(this.handler.waitForMsg(_WAIT_TIMEOUT_))
			{
				this.handler.validate();
			}
			
		}
 */ 
	}

	public void testMoreThan63() {
/*            
		this.handler.msgLength = 70;
		if (ensureLinkUpsForTest())
		{
			this.handler.sendMsg();
			if(this.handler.waitForMsg(_WAIT_TIMEOUT_))
			{
				this.handler.validate();
			}
			
		}
 */ 
	}
	
	public void test272() {
/*            
		this.handler.msgLength = 265;
		if (ensureLinkUpsForTest())
		{
			this.handler.sendMsg();
			if(this.handler.waitForMsg(_WAIT_TIMEOUT_))
			{
				this.handler.validate();
			}
			
		}
 */ 
	}

	@Override
	protected ExtendedSS7Layer4 getLeg1DataHandler() {

		return handler;
	}

	@Override
	protected ExtendedSS7Layer4 getLeg2DataHandler() {

		return loopHandler;
	}

	@Override
	protected int getMaxWaitCountForLinkUp() {
		return _MAX_WAIT_COUNT_;
	}

	@Override
	protected int getWaitTimeForLinkUp() {
		return _WAIT_TIMEOUT_;
	}

	private class Layer4DataHandler implements ExtendedSS7Layer4 {
		private final Logger logger = Logger.getLogger(Layer4DataHandler.class);
		private Mtp3 leg;
		private boolean linksUp;
		//3 is minimal ok msg, its routing label of mtp3
		private int msgLength = 3;
		
		private byte[] sentData;
		private byte[] receivedData;
		private long startTime;
		
		public Layer4DataHandler() {
			super();

		}

		public void validate() {
			if(Arrays.equals(this.receivedData, sentData))
			{
				
			}else
			{
				fail("Missmatch. \nReceived:\n"+Utils.dump(receivedData, receivedData.length,false)+"\nSent:\n"+
						Utils.dump(sentData, sentData.length,false));
			}
			
		}

		public boolean waitForMsg(int waitTimeout) {
			while(this.receivedData == null)
			{
				if(System.currentTimeMillis()<this.startTime+waitTimeout)
				{
					try {
						Thread.currentThread().sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}else
				{
					return false;
				}
			}
			
			return true;
		}

		public void sendMsg() {
			sentData = new byte[this.msgLength];
			int cCount = (int) (Math.random() * 10);
			for (int j = 0; j < cCount; j++) {
				int index = (j * cCount + 1) % sentData.length;
				
				sentData[index] = (byte) (Math.random() * 100);
			}
			if (!this.send(_SLS_, _LINKSET_ID_, _SCCP_SERVICE, _SCCP_SUB_SERVICE, sentData)) {
				fail("Failed to send buffer through first leg.");
				return;
			}else
			{
				this.startTime = System.currentTimeMillis();
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

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.mobicents.media.server.impl.resource.ss7.ExtendedSS7Layer4#isLinkUp
		 * ()
		 */
		public boolean isLinkUp() {
			return this.linksUp;
		}

		public void receive(byte sls, byte linksetId, int service,
				int subservice, byte[] msgBuff) {
			if (!linksUp) {
				logger.error("Received msg when link is down....");
			}
			this.receivedData = msgBuff;

		}

		public boolean send(byte sls, byte linksetId, int si, int ssf, byte[] b) {
			if (linksUp) {
				if (this.leg.send(sls, linksetId, si, ssf, b)) {

					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		}

		public void setLayer3(Mtp3 mtp3) {
			this.leg = mtp3;

		}
	}

	private class Layer4LoopDataHandler implements ExtendedSS7Layer4 {
		private final Logger logger = Logger
				.getLogger(Layer4LoopDataHandler.class);
		private Mtp3 leg;
		private boolean linksUp;

		public Layer4LoopDataHandler() {

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

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.mobicents.media.server.impl.resource.ss7.ExtendedSS7Layer4#isLinkUp
		 * ()
		 */
		public boolean isLinkUp() {
			return this.linksUp;
		}

		public void receive(byte sls, byte linksetId, int service,
				int subservice, byte[] msgBuff) {
			// this is simple loop, we just send back
			if (!linksUp) {
				logger.error("Received msg when link is down....");
				return;
			}
			if (!leg.send(sls, linksetId, service, subservice, msgBuff)) {
				fail("Failed to loop message: "
						+ Utils.dump(msgBuff, msgBuff.length, false));
			}

		}

		public void setLayer3(Mtp3 mtp3) {
			this.leg = mtp3;

		}

	}
	//li[63] rl[66] rxl[72]
	//82 83 3f 03 35 77 cd 0d 00 1b 00 00 00 00 00 00 00 11 00 00 00 00 00 00 00 0e 00 00 00 00 00 00 00 61 00 00 00 00 00 00 00 3e 00 00 00 00 00 00 00 55 00 00 00 00 00 00 00 52 00 00 00 00 00 00 00 0c 00 00 00 00 a1 aa
	
	
	//li[62] rl[61] rxl[67]
	
}
