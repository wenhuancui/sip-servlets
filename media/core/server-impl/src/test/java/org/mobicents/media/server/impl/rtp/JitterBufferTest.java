/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.mobicents.media.server.impl.rtp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mobicents.media.server.impl.rtp.clock.AudioClock;
import org.mobicents.media.server.spi.rtp.AVProfile;

/**
 * 
 * @author amit bhayani
 * 
 */
public class JitterBufferTest {

	private int period = 20;
	private int jitter = 40;

	private JitterBuffer jitterBuffer;
	private RtpClock clock;

	@BeforeClass
	public static void setUpClass() throws Exception {
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
	}

	@Before
	public void setUp() {
		jitterBuffer = new JitterBuffer(jitter);
		clock = new AudioClock();
		jitterBuffer.setClock(clock);
		jitterBuffer.setFormat(AVProfile.PCMU);
	}

	@After
	public void tearDown() {
	}

	private RtpPacket createBuffer(int seq) {
		return new RtpPacket((byte) 0, seq, seq * 160, 1, new byte[160]);
	}

	@Test
	public void testAccuracy() {
		jitterBuffer.write(createBuffer(1));

		RtpPacket p = null;
		p = jitterBuffer.read(0);
		assertEquals("Jitter Buffer not full yet", null, p);

		jitterBuffer.write(createBuffer(2));
		p = jitterBuffer.read(20);
		assertEquals("Jitter Buffer not full yet", null, p);

		jitterBuffer.write(createBuffer(3));
		p = jitterBuffer.read(40);
		assertEquals("Jitter Buffer not full yet", null, p);

		jitterBuffer.write(createBuffer(4));
		p = jitterBuffer.read(60);
		assertTrue("Jitter Buffer not full yet", p != null);

		jitterBuffer.write(createBuffer(5));
		p = jitterBuffer.read(80);
		assertTrue("Jitter Buffer should be full", p != null);
	}

	@Test
	public void testAccuracy1() {
		RtpPacket p = null;
		jitterBuffer.write(createBuffer(1));
		jitterBuffer.write(createBuffer(2));
		jitterBuffer.write(createBuffer(3));
		jitterBuffer.write(createBuffer(4));
		p = jitterBuffer.read(0);
		assertTrue("Jitter Buffer not full yet", p != null);

		jitterBuffer.write(createBuffer(5));
		p = jitterBuffer.read(20);
		assertTrue("Jitter Buffer should be full", p != null);
	}

	@Test
	public void testOverflowBeforeStart() {

		// This will cause buffer to overflow, now first read MUST return packet
		// with CSeq == 2,
		for (int i = 0; i < JitterBuffer._QUEUE_SIZE_ + 1; i++) {
			this.jitterBuffer.write(createBuffer(i + 1));
		}
		// -1, cause we want to have always one packet?
		for (int i = 0; i < JitterBuffer._QUEUE_SIZE_ - 1; i++) {
			long tStamp = 20 * i;
			RtpPacket p = this.jitterBuffer.read(tStamp);

			RtpPacket patternPacket = createPatternPacket(i+2); 
			makeAssertionTestRtpPacket(patternPacket, p);

		}

		// push next packet to get one we want to.
		this.jitterBuffer.write(createBuffer(JitterBuffer._QUEUE_SIZE_ + 2));
		RtpPacket p = this.jitterBuffer.read(JitterBuffer._QUEUE_SIZE_ * 20);

		RtpPacket patternPacket = createPatternPacket(JitterBuffer._QUEUE_SIZE_ + 1);
		makeAssertionTestRtpPacket(patternPacket, p);
		try {
			p = this.jitterBuffer.read(20 * (JitterBuffer._QUEUE_SIZE_ + 1));
			assertTrue("Buffer return packet, it should not!", p == null);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(
					"Failed, buffer threw exception, it should return null!: "
							+ e, false);
		}

	}

	@Test
	public void testOverflowAfterStart() {
		// here we write full buffer, read half of it, and write 3/4th of buffer
		int currentCseq = 1;
		for(int i=0;i<JitterBuffer._QUEUE_SIZE_;i++)
		{
			//we write full buffer
			this.jitterBuffer.write(createBuffer(i + 1));
		}
		
		//now lets read half of it;
		for (int i = 0; i < JitterBuffer._QUEUE_SIZE_/2; i++) {
			long tStamp = 20 * (currentCseq-1);
			RtpPacket p = this.jitterBuffer.read(tStamp);

			RtpPacket patternPacket = createPatternPacket(currentCseq); 
			makeAssertionTestRtpPacket(patternPacket, p);
			currentCseq++;

		}
		
		//lets calculate some numbers
		int packetsToWrite = (JitterBuffer._QUEUE_SIZE_*3)/4;
		//75 - for buffer 100;
		
		for(int i=0;i<packetsToWrite;i++)
		{
			//we write full buffer
			this.jitterBuffer.write(createBuffer(JitterBuffer._QUEUE_SIZE_+i + 1));
		}
		
		
		
		//now we calculate next currentReadCseq;
		//            (   Final V( How much we lack         (Free space)         ))
		currentCseq = currentCseq+(packetsToWrite-(JitterBuffer._QUEUE_SIZE_-currentCseq)) - 1 ; // -1 cause... we use current cseq twice!
	
		//now lets test rest of buffer
		for (int i = 0; i < JitterBuffer._QUEUE_SIZE_-1; i++) {
			long tStamp = 20 * (currentCseq-1);
			RtpPacket p = this.jitterBuffer.read(tStamp);

			RtpPacket patternPacket = createPatternPacket(currentCseq); 
			makeAssertionTestRtpPacket(patternPacket, p);
			currentCseq++;

		}
	}
	
	//FIXME: ADD BOUNDRY CASES FOR ALL!!!!!
	
	@Test
	public void testOverflowOn_w_r_nw() {
		//r - readCursor,w - writeCursor, nw - new(next)WriteCursor
		//(r,w)
		int nextWriteCseq=prefilBuffer(70,58);
		int nextReadCseq = 70+1;
		//r,w,n_rw(to start filling with data)
		overFlowAndTest(70,58,nextReadCseq,nextWriteCseq,25,1);
	}

	@Test
	public void testOverflowOn_w_r_nw_5() {
		//r - readCursor,w - writeCursor, nw - new(next)WriteCursor
		//(r,w)
		int nextWriteCseq=prefilBuffer(70,60);
		int nextReadCseq = 70+1;
		//r,w,n_rw(to start filling with data)
		overFlowAndTest(70,60,nextReadCseq,nextWriteCseq,25,5);
	}
	
	//with flip
	@Test
	public void testOverflowOn_w_r_nw_f() {
		//r - readCursor,w - writeCursor, nw - new(next)WriteCursor
		//(r,w)
		//
		int nextWriteCseq=prefilBuffer(95,90);
		int nextReadCseq = 95+1;
		//r,w,n_rw(to start filling with data)
		overFlowAndTest(95,90,nextReadCseq,nextWriteCseq,25,1);
	}
	@Test
	public void testOverflowOn_w_r_nw_f_5() {
		//r - readCursor,w - writeCursor, nw - new(next)WriteCursor
		//(r,w)
		//
		int nextWriteCseq=prefilBuffer(90,70);
		int nextReadCseq = 90+1;
		//r,w,n_rw(to start filling with data)
		overFlowAndTest(90,70,nextReadCseq,nextWriteCseq,25,5);
	}
	
	@Test
	public void testOverflowOn_r_nw_w() {
		//r - readCursor,w - writeCursor, nw - new(next)WriteCursor
		//(r,w)
	//
		int nextWriteCseq=prefilBuffer(JitterBuffer._QUEUE_SIZE_+10,90);
		int nextReadCseq = JitterBuffer._QUEUE_SIZE_+10+90+1;
		//r,w,n_rw(to start filling with data)
		overFlowAndTest(JitterBuffer._QUEUE_SIZE_+10,90,nextReadCseq,nextWriteCseq,25,1);
	}
	
	@Test
	public void testOverflowOn_r_nw_w_5() {
		//r - readCursor,w - writeCursor, nw - new(next)WriteCursor
		//(r,w)
	//
		int nextWriteCseq=prefilBuffer(JitterBuffer._QUEUE_SIZE_+10,90);
		int nextReadCseq = JitterBuffer._QUEUE_SIZE_+10+90+1;
		//r,w,n_rw(to start filling with data)
		overFlowAndTest(JitterBuffer._QUEUE_SIZE_+10,90,nextReadCseq,nextWriteCseq,25,5);
	}
	
	private int prefilBuffer(int targetReadPointer, int targetWritePointer) {
		
		int currentCseq = 1;
		if(targetReadPointer>targetWritePointer && targetReadPointer< 100)
		{
			//write pointer will always flip.
		
			int currentReadCseq = 1;
			//lets fill it.
			for(int i=0;i<JitterBuffer._QUEUE_SIZE_;i++)
			{
				//we write full buffer
				this.jitterBuffer.write(createBuffer(currentCseq++));
			}
			for (int i = 0; i < targetReadPointer; i++) {
				long tStamp = 20 * (currentReadCseq-1);
				//discard it. we dont care now.
				RtpPacket p = this.jitterBuffer.read(tStamp);
				currentReadCseq++;
				

			}
			for(int i=0;i<targetWritePointer;i++)
			{
				//we write full buffer
				this.jitterBuffer.write(createBuffer(currentCseq++));
			}
		}else
		{
			//here we have to flip twice :/
			//lets prepare buffer
			 currentCseq=this.prefilBuffer(99, targetWritePointer);
			 targetReadPointer-=99;
			//writePointer is in possition, now we need to flip "r" to proper position
			// = cause we are on 99
			 
			for(int i=0;i<=targetReadPointer;i++)
			{
				
					//we write full buffer
					this.jitterBuffer.write(createBuffer(currentCseq++));
				
			}
		}
		return currentCseq;
	}

	private void overFlowAndTest(int readCursor, int writeCursor, int nextReadCSeq,int nextWriteCseq, int writeCSeqShift, int numberOfPacketsToWrite) {
		//y, its complicated... Its hard to make proper checks on flipping buffer.
		//Before call, ensure that mextWriteCseq-- called Nth times wont produce duplicate!!!
		int localNextWriteCSeq = nextWriteCseq +writeCSeqShift;
		int localNumberPacketsToWrite = numberOfPacketsToWrite;
		//its a hack, we overflow only once...
		int localNextReadCSeq = (localNextWriteCSeq+1)-100;
		
		
		for(;localNumberPacketsToWrite>0;localNumberPacketsToWrite--)
		{
			this.jitterBuffer.write(createBuffer(localNextWriteCSeq));
			localNextWriteCSeq-=2;
		}
		
		localNumberPacketsToWrite = numberOfPacketsToWrite;
		
		//now lets test all reads :), remember, last packet wont be available for read in current impl.
		//initial tStamp
		int tStamp = localNextReadCSeq*20;
		//now this is really tricky...
		//determining number of packets may be hard, lets just read, until we reach null or desired CSeq 
		//- that is (nextWriteCseq+writeCSeqShift)-2;
		int desiredCSeq= (nextWriteCseq+writeCSeqShift)-2;
		boolean reasonToLive = true;
		//refresh
		localNextWriteCSeq = nextWriteCseq +writeCSeqShift;
		while(reasonToLive)
		{

			RtpPacket p = this.jitterBuffer.read(tStamp);
			RtpPacket patternPacket = createPatternPacket(localNextReadCSeq);
			createPatternPacket(localNextReadCSeq);
			
			//FIXME: add something for duration?
			if(p.getDuration()!=20)
			{
				patternPacket.setDuration(p.getDuration());
			}
			makeAssertionTestRtpPacket(patternPacket, p);
			tStamp+=p.getDuration();
			if(desiredCSeq == p.getSeqNumber())
			{

				return;
			}else
			{
				if(localNextReadCSeq >= nextWriteCseq-1)
				{
					localNumberPacketsToWrite--;
					//compute cseq ?
					localNextReadCSeq=(localNextWriteCSeq-2*localNumberPacketsToWrite);
					
					if(localNumberPacketsToWrite==0)
					{
						return;
					}
				}else
				{
					localNextReadCSeq++;
				}
			}
		}
		
	}
	
	private RtpPacket createPatternPacket(int cseq) {
		
		RtpPacket patternPacket = createBuffer(cseq);
		patternPacket.setDuration(20);
		patternPacket.setTime(cseq  * 20);

		return patternPacket;
	}

	private void makeAssertionTestRtpPacket(RtpPacket patternPacket, RtpPacket p) {
		assertTrue("Buffer packet is null!", p != null);
		assertEquals("Duration is incorrect!", patternPacket.getDuration(), p
				.getDuration());
		assertEquals("Time is incorrect!",
				patternPacket.getTime() /* (i+2)*20 */, p.getTime());
		assertEquals("Timestamp is incorrect!",
				patternPacket.getTimestamp()/* (i+2)*160 */, p.getTimestamp());
	}

//	private void printPacketData(RtpPacket p, int index) {
//		if (p != null) {
//			System.err.println("Packet[" + index + "], Duration: "
//					+ p.getDuration() + ", Seq: " + p.getSeqNumber()
//					+ ", Time: " + p.getTime() + ", Timestamp: "
//					+ p.getTimestamp());
//		} else {
//			System.err.println("packet[" + index + "]");
//		}
//	}


}
