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
import static org.junit.Assert.*;
import org.mobicents.media.server.impl.clock.TimerImpl;
import org.mobicents.media.server.spi.clock.Timer;

/**
 *
 * @author kulikov
 */
public class TransmissionTester2Test {

    private Timer timer;
    private TransmissionTester2 tester;    
    
    public TransmissionTester2Test() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        timer = new TimerImpl();
        timer.start();
        tester = new TransmissionTester2(timer);
    }

    @After
    public void tearDown() {
        timer.stop();
    }

    /**
     * Test of getGenerator method, of class TransmissionTester.
     */
    @Test
    public void testPassed() {
        tester.connect(tester.getDetector());
        tester.start();
        assertTrue("Test not passed", tester.isPassed());
    }

    @Test
    public void testFailure() {
        tester.start();
        assertFalse("Test passed", tester.isPassed());
    }


}