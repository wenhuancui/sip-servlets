/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mobicents.media.server.impl;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mobicents.media.Buffer;
import org.mobicents.media.Format;
import org.mobicents.media.server.impl.clock.TimerImpl;
import org.mobicents.media.server.impl.rtp.sdp.AVProfile;
import org.mobicents.media.server.spi.clock.Timer;

/**
 *
 * @author kulikov
 */
public class AbstractSourceTest {

    private Timer timer = new TimerImpl();
    private TestSource source = new TestSource("test-source");
    
    public AbstractSourceTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        source.setSyncSource(timer);
        timer.start();
    }

    @After
    public void tearDown() {
        timer.stop();
    }

    /**
     * Test of getSyncSource method, of class AbstractSource.
     */
    @Test
    @SuppressWarnings("static-access")
    public void testRestart() throws Exception {
        System.out.println("Starting ");
        source.start();
        System.out.println("Started ");
        Thread.currentThread().sleep(3000);
        
        source.stop();
        System.out.println("Stopped ");
//        Thread.currentThread().sleep(1500);
        source.start();
        
        Thread.currentThread().sleep(3000);
    }


    public class TestSource extends AbstractSource {

        public TestSource(String name) {
            super(name);
        }
        
        @Override
        public void evolve(Buffer buffer, long timestamp) {
            buffer.setLength(320);
            buffer.setDuration(500);
            buffer.setFlags(Buffer.FLAG_LIVE_DATA);
            System.out.println(System.currentTimeMillis() + " Evolve");
        }

        public Format[] getFormats() {
            return new Format[] {AVProfile.L16_MONO};
        }
        
    }
}