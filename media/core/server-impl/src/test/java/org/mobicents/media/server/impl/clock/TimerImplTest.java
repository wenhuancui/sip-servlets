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

package org.mobicents.media.server.impl.clock;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mobicents.media.Buffer;
import org.mobicents.media.Format;
import org.mobicents.media.server.impl.AbstractSink;
import org.mobicents.media.server.impl.AbstractSource;
import org.mobicents.media.server.spi.clock.Task;
import org.mobicents.media.server.spi.dsp.Codec;

/**
 * 
 * @author kulikov
 */
public class TimerImplTest {

	public TimerImplTest() {
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
	}

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}

	// @Test
	public void testTimerStart() throws InterruptedException {
		TimerImpl timer = new TimerImpl();
		timer.start();

		Thread.currentThread().sleep(3000);

		timer.stop();
	}

	/**
	 * Test of getHeartBeat method, of class TimerImpl.
	 */
	@Test
	public void testTimer() throws Exception {
		TimerImpl timer = new TimerImpl();
		timer.start();

		TestSource t1 = new TestSource("1", 20);
		TestSource t2 = new TestSource("2", 20);

		TestSink s1 = new TestSink("1");
		TestSink s2 = new TestSink("2");

		t1.connect(s1);
		t2.connect(s2);

		t1.setSyncSource(timer);
		t2.setSyncSource(timer);

		Semaphore semaphore = new Semaphore(0);

		semaphore.tryAcquire(2, TimeUnit.SECONDS);

		t1.start();
		t2.start();

		semaphore.tryAcquire(5, TimeUnit.SECONDS);

		t1.stop();
		t2.stop();

		int c1 = t1.getCountor();
		int c2 = t2.getCountor();

		assertEquals(true, c1 > 0);
		assertEquals(true, c2 > 0);

		semaphore.tryAcquire(5, TimeUnit.SECONDS);
		assertEquals(true, (t1.getCountor() - c1) <= 1);
		assertEquals(true, (t2.getCountor() - c2) <= 1);

		t1.disconnect(s1);
		t2.disconnect(s2);

		timer.stop();
	}
	
	@Test
	public void testTaskZeroDuartion() throws Exception {

		Semaphore semaphore = new Semaphore(0);
		
		TimerImpl timer = new TimerImpl();
		timer.start();
		
		TaskImpl task = new TaskImpl();
		
		timer.sync(task);
		
		semaphore.tryAcquire(6, TimeUnit.SECONDS);
		
		System.out.println("here ==> "+task.getCounter());
		
		assertEquals(true, Math.abs(task.getCounter() - 14) <= 2);
		
		
	}

	/**
	 * Since the duration is 1 sec the Counter will be incremented max 5 times
	 * 
	 * @throws Exception
	 */
	@Test
	public void testTimer1000() throws Exception {
		TimerImpl timer = new TimerImpl();
		timer.start();

		TestSource t1 = new TestSource("1", 1010);
		TestSource t2 = new TestSource("2", 1010);

		TestSink s1 = new TestSink("1");
		TestSink s2 = new TestSink("2");

		t1.connect(s1);
		t2.connect(s2);

		t1.setSyncSource(timer);
		t2.setSyncSource(timer);

		Semaphore semaphore = new Semaphore(0);

		semaphore.tryAcquire(2, TimeUnit.SECONDS);

		t1.start();
		t2.start();

		System.out.println("started");

		semaphore.tryAcquire(5, TimeUnit.SECONDS);

		t1.stop();
		t2.stop();

		int c1 = t1.getCountor();
		int c2 = t2.getCountor();

		assertEquals(true, c1 > 0);
		assertEquals(true, c2 > 0);

		System.out.println("c1 = " + c1);
		System.out.println("c2 = " + c2);

		semaphore.tryAcquire(5, TimeUnit.SECONDS);
		assertEquals(true, ((t1.getCountor() - 4) <= 1)
				&& ((t1.getCountor() - 4) >= 0));
		assertEquals(true, ((t2.getCountor() - 4) <= 1)
				&& ((t2.getCountor() - 4) >= 0));

		t1.disconnect(s1);
		t2.disconnect(s2);

		timer.stop();
	}

	/**
	 * Since the duration is 2.5 sec the Counter will be incremented max 3 times
	 * while semaphore is trying to acquire lock for 8 secs
	 * 
	 * @throws Exception
	 */
	@Test
	public void testTimer2500() throws Exception {
		TimerImpl timer = new TimerImpl();
		timer.start();

		TestSource t1 = new TestSource("1", 2500);
		TestSource t2 = new TestSource("2", 2500);

		TestSink s1 = new TestSink("1");
		TestSink s2 = new TestSink("2");

		t1.connect(s1);
		t2.connect(s2);

		t1.setSyncSource(timer);
		t2.setSyncSource(timer);

		Semaphore semaphore = new Semaphore(0);

		semaphore.tryAcquire(2, TimeUnit.SECONDS);

		t1.start();
		t2.start();

		System.out.println("started");

		semaphore.tryAcquire(8, TimeUnit.SECONDS);

		t1.stop();
		t2.stop();

		int c1 = t1.getCountor();
		int c2 = t2.getCountor();

		assertEquals(true, c1 > 0);
		assertEquals(true, c2 > 0);

		System.out.println("c1 = " + c1);
		System.out.println("c2 = " + c2);

		semaphore.tryAcquire(5, TimeUnit.SECONDS);
		assertEquals(true, ((t1.getCountor() - 2) <= 2)
				&& ((t1.getCountor() - 2) >= 0));
		assertEquals(true, ((t2.getCountor() - 2) <= 2)
				&& ((t2.getCountor() - 2) >= 0));

		t1.disconnect(s1);
		t2.disconnect(s2);

		timer.stop();
	}

	private class TestSource extends AbstractSource {
		private int countor;
		private int duration;

		public TestSource(String name, int duration) {
			super(name);
			this.duration = duration;
		}

		public int getCountor() {
			return countor;
		}

		@Override
		public void evolve(Buffer buffer, long timestamp) {
			buffer.setData(new byte[320]);
			buffer.setLength(320);
			buffer.setDuration(this.duration);
			buffer.setTimeStamp(timestamp);
			countor++;
		}

		public Format[] getFormats() {
			return new Format[] { Codec.LINEAR_AUDIO };
		}

	}

	private class TaskImpl implements Task {

		boolean active = true;

		int duration = 1000;
		int reps = 4;
		int counter = 0;

		public void cancel() {
			this.active = false;
		}

		public boolean isActive() {
			return active;
		}
		
		public int getCounter(){
			return this.counter;
		}

		public int perform() {
			System.out.println("reps = "+ reps+ " counter = "+ counter+ " duration = "+ duration);
			reps--;
			counter++;
			if (reps == 0) {
				this.duration = 0;
			}
			return duration;

		}

	}

	private class TestSink extends AbstractSink {

		public TestSink(String name) {
			super(name);
		}

		@Override
		public void onMediaTransfer(Buffer buffer) throws IOException {
		}

		public Format[] getFormats() {
			return new Format[] { Codec.LINEAR_AUDIO };
		}

	}

}