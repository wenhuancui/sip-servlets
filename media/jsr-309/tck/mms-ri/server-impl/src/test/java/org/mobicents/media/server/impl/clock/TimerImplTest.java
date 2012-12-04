/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mobicents.media.server.impl.clock;

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
import org.mobicents.media.server.spi.dsp.Codec;
import static org.junit.Assert.*;

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

    /**
     * Test of getHeartBeat method, of class TimerImpl.
     */
    @Test
    public void testTimer() throws Exception {
        TimerImpl timer = new TimerImpl();
        TestSource t1 = new TestSource("1");
        TestSource t2 = new TestSource("2");
        
        TestSink s1 = new TestSink("1");
        TestSink s2 = new TestSink("2");
        
        t1.connect(s1);
        t2.connect(s2);
        
        t1.setSyncSource(timer);
        t2.setSyncSource(timer);
        
        Semaphore semaphore = new Semaphore(0);
        
        timer.start();
        semaphore.tryAcquire(2, TimeUnit.SECONDS);
        
        timer.sync(t1);
        timer.sync(t2);

        
        semaphore.tryAcquire(5, TimeUnit.SECONDS);
        
        timer.unsync(t1);
        timer.unsync(t2);
        
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


    private class TestSource extends AbstractSource {
        private int countor;
        
        public TestSource(String name) {
            super(name);
        }
        
        public int getCountor() {
            return countor;
        }
        
        @Override
        public void evolve(Buffer buffer, long timestamp, long sequenceNumber) {
            buffer.setData(new byte[320]);
            buffer.setLength(320);
            buffer.setDuration(20);
            buffer.setTimeStamp(timestamp);
            buffer.setSequenceNumber(sequenceNumber);
            countor++;
        }

        public Format[] getFormats() {
            return new Format[] {Codec.LINEAR_AUDIO};
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
            return new Format[] {Codec.LINEAR_AUDIO};
        }

        public boolean isAcceptable(Format format) {
            return true;
        }
        
    }

}