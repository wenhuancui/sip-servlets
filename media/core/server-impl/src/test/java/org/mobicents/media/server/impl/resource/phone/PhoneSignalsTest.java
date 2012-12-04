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

package org.mobicents.media.server.impl.resource.phone;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
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
public class PhoneSignalsTest implements NotificationListener {

    private Server server;
    
    private PhoneSignalGenerator gen;
    private PhoneSignalDetector det;
    
    private Semaphore semaphore = new Semaphore(0);
    private boolean evtDetected;
    
    public PhoneSignalsTest() {
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
        short A = Short.MAX_VALUE /2;
        
        int[] F = new int[]{100, 200};
        int[] T = new int[]{1,1};
        
        gen = new PhoneSignalGenerator("phone.gen");
        gen.setAmplitude((short)320);
        gen.setFrequency(F);
        gen.setPeriods(T);
        
        det = new PhoneSignalDetector("phone.detector");
        det.setFrequency(F);
        det.setPeriods(T);
        det.setVolume(-30);
        det.setEventID(50);
        det.addListener(this);
        gen.connect(det);
    }

    @After
    public void tearDown() {
        server.stop();
    }

    /**
     * Test of setPeriods method, of class PhoneSignalDetector.
     */
    @Test
    public void testSetPeriods() throws Exception {
        det.start();
        gen.start();
        
        semaphore.tryAcquire(10, TimeUnit.SECONDS);
        
        assertTrue("Event not detected", evtDetected);
    }


    public void update(NotifyEvent event) {
        if (event.getEventID() == 50) {
            evtDetected = true;
            semaphore.release();
        }
    }

}