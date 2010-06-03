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

import java.net.URL;

import org.apache.log4j.xml.DOMConfigurator;
import org.jboss.test.kernel.junit.MicrocontainerTest;

import org.mobicents.media.server.spi.resource.ss7.Mtp3;
import org.mobicents.media.server.spi.resource.ss7.SS7Layer4;

public abstract class TwoLinksTestHarrness extends MicrocontainerTest{

	
	public static final String _MTP_3_BEAN_NAME1_ = "SS7.Mtp3.1";
	public static final String _MTP_3_BEAN_NAME2_ = "SS7.Mtp3.2";
	public static final byte _SLS_ = 0;
	public static final byte _LINKSET_ID_ = 0;
	// we mockup SCCP :)
	public static final int _SCCP_SERVICE = 3;
	public static final int _SCCP_SUB_SERVICE = 0;

	// lower layer. retrieved from container mockup.
	protected Mtp3 mtp3_leg1;
	protected Mtp3 mtp3_leg2;
	
	public TwoLinksTestHarrness(String name) {
		super(name);
	}

	private static final String _FILE_LOG4J_CONF = "mtp-log4j.xml";
	@Override
	protected void setUp() throws Exception {
		URL url =Thread.currentThread().getContextClassLoader().getResource(_FILE_LOG4J_CONF);
		if(url!=null)
		{
			
			try {
				DOMConfigurator xmlConf = new DOMConfigurator();
				xmlConf.configure(url);
				
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
				
			}
			
		}else
		{
			
		}
		super.setUp();
		
		this.mtp3_leg1 = (Mtp3) super.getBean(_MTP_3_BEAN_NAME1_);
		this.mtp3_leg2 = (Mtp3) super.getBean(_MTP_3_BEAN_NAME2_);

		this.getLeg1DataHandler().setLayer3(this.mtp3_leg1);
		this.getLeg2DataHandler().setLayer3(this.mtp3_leg2);
		
		this.mtp3_leg1.setLayer4(this.getLeg1DataHandler());
		this.mtp3_leg2.setLayer4(this.getLeg2DataHandler());
		
		this.mtp3_leg1.stop();
		this.mtp3_leg2.stop();
		this.mtp3_leg1.start();
		this.mtp3_leg2.start();
		
	}
	
	@Override
	protected void tearDown() throws Exception {
		this.mtp3_leg1.setLayer4(null);
		this.mtp3_leg2.setLayer4(null);

		this.mtp3_leg1 = null;
		this.mtp3_leg2 = null;
		super.tearDown();
	}
	
	
	protected abstract ExtendedSS7Layer4 getLeg1DataHandler();
	protected abstract ExtendedSS7Layer4 getLeg2DataHandler();
	
	protected boolean ensureLinkUpsForTest() {
		int waitCount = 0;
		while (!this.getLeg1DataHandler().isLinkUp() || !this.getLeg2DataHandler().isLinkUp()) {
			waitCount++;
			try {
				Thread.currentThread().sleep(getWaitTimeForLinkUp());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (waitCount == getMaxWaitCountForLinkUp()) {
				fail("Links did not go up!");
				return false;
			}
		}
		return true;
	}

	protected abstract int getMaxWaitCountForLinkUp();

	protected abstract int getWaitTimeForLinkUp();
}
