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

package org.mobicents.media.server.impl.resource.test;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mobicents.media.Server;
import static org.junit.Assert.*;
import org.mobicents.media.server.spi.NotificationListener;
import org.mobicents.media.server.spi.events.NotifyEvent;

/**
 *
 * @author kulikov
 */
public class MeanderTest implements NotificationListener {

    public final static int TEST_DURATION = 10;
    
    public final static short A = 100;
    public final static double T = 0.1;
        
    private MeanderGenerator gen;
    private MeanderDetector det;
    
    private int outOfSeq;
    private int evtCount;
    private boolean fmtMissmatch = false;
    
    private Server server;
    
    public MeanderTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        server = new Server();
        server.start();
        gen = new MeanderGenerator("test-gen");
        gen.setAmplitude(A);
        gen.setPeriod(T);
        
        det = new MeanderDetector("test-det");
        det.setAmplitude(A);
        det.setPeriod(T);
        det.addListener(this); 
        
    }

    @After
    public void tearDown() {
        server.stop();
    }

    /**
     * Test of isAcceptable method, of class MeanderDetector.
     */
    @Test
    @SuppressWarnings("static-access")
    public void testGenerationDetection() throws Exception {
        gen.connect(det);
        det.start();
        gen.start();
        
        Thread.currentThread().sleep(TEST_DURATION * 1000);
        assertEquals(0, outOfSeq); 
        
        int count = (int)(TEST_DURATION /T/2);
        int limit = (int)(1/T/2) + 1 + 2 ;
        
        int diff = Math.abs(count - evtCount);
        
        assertTrue("Signal not detected, diff="+diff+", limit="+limit, diff <= limit);
        assertFalse("Format missmatch detected", this.fmtMissmatch);
    }

    public void update(NotifyEvent event) {
        if (event.getEventID() == MeanderEvent.EVENT_MEANDER) {
            evtCount++;
        } else if (event.getEventID() == MeanderEvent.EVENT_OUT_OF_SEQUENCE){
            outOfSeq++;
        } else if (event.getEventID() == MeanderEvent.EVENT_FORMAT_MISSMATCH) {
            fmtMissmatch = true;
        }
    }

}