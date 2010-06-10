package org.mobicents.protocols.mgcp.stack.test.transactionretransmisson;

import org.apache.log4j.Logger;
import org.mobicents.protocols.mgcp.stack.test.MessageFlowHarness;

public class TxRetransmissionTest extends MessageFlowHarness {

	private static Logger logger = Logger.getLogger("mgcp.test");

	private CA ca;
	private MGW mgw;

	public TxRetransmissionTest() {
		super("TxRetransmissionTest");
	}

	public void setUp() {
		try {
			super.setUp();

			ca = new CA(caProvider, mgProvider);
			mgw = new MGW(mgProvider);

		} catch (Exception ex) {
			ex.printStackTrace();
			fail("Unexpected Exception");
		}
	}

	//TODO : We Just have 1 Thread now. Provisional Response for Re-Transmission will never be sent with this test. Need to modify test first
	
	public void testDummy() {
		assertTrue(true);
	}
	
//	public void testReTransmissionCreateConnection() {
//		
//		ca.setCommand("CRCX");
//		mgw.setCommand("CRCX");
//		this.ca.sendReTransmissionCreateConnection();
//		waitForRetransmissionTimeout();
//	}
//	
//	public void testReTransmissionDeleteConnection() {
//		ca.setCommand("DLCX");
//		mgw.setCommand("DLCX");
//		this.ca.sendReTransmissionDeleteConnection();
//		waitForRetransmissionTimeout();
//	}
//	
//	public void testReTransmissionModifyConnection() {
//		ca.setCommand("MDCX");
//		mgw.setCommand("MDCX");
//		this.ca.sendReTransmissionModifyConnection();
//		waitForRetransmissionTimeout();
//	}	
//	
//	public void testReTransmissionNotificationRequest() {
//		ca.setCommand("RQNT");
//		mgw.setCommand("RQNT");
//		this.ca.sendReTransmissionNotificationRequest();
//		waitForRetransmissionTimeout();
//	}
//	
//	public void testReTransmissionNotify() {
//		ca.setCommand("NTFY");
//		mgw.setCommand("NTFY");
//		this.ca.sendReTransmissionNotify();
//		waitForRetransmissionTimeout();
//	}	
	
	public void tearDown() {
		try {
			super.tearDown();
		} catch (Exception ex) {

		}
		
//		this.ca.checkState();
//		this.mgw.checkState();
		logTestCompleted();

	}
}
