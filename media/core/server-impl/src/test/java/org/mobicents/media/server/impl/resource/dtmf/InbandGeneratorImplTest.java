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

package org.mobicents.media.server.impl.resource.dtmf;

import java.util.ArrayList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mobicents.media.Server;
import static org.junit.Assert.*;
import org.mobicents.media.Utils;
import org.mobicents.media.server.impl.resource.fft.SpectraAnalyzer;
import org.mobicents.media.server.impl.resource.fft.SpectrumEvent;
import org.mobicents.media.server.spi.NotificationListener;
import org.mobicents.media.server.spi.events.NotifyEvent;

/**
 *
 * @author kulikov
 */
public class InbandGeneratorImplTest implements NotificationListener {

    private Server server;
    
    private final static int FREQ_ERROR = 5;
    private int MAX_ERRORS = 1;
    
    private GeneratorImpl gen;
    private SpectraAnalyzer det;
    private ArrayList<double[]> s;

    public InbandGeneratorImplTest() {
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
        gen = new GeneratorImpl("Gen");
        gen.setToneDuration(2000);
        gen.setDigit("0");

        det = new SpectraAnalyzer("det");
        det.addListener(this);
        s = new ArrayList();
    }

    @After
    public void tearDown() {
        server.stop();
    }

    /**
     * Test of getDigit method, of class InbandGeneratorImpl.
     */
    @Test
    @SuppressWarnings("static-access")
    public void testDigit0() throws Exception {
        gen.connect(det);
        gen.start();
        det.start();
        
        Thread.currentThread().sleep(2000);
        gen.stop();
        det.stop();
        
        Thread.currentThread().sleep(1000);        
        assertTrue(verify(s, new int[]{941, 1336}));
    }

    private boolean verify(ArrayList<double[]> spectra, int[] F) {
        int errorCount = 0;
        if (spectra.size() == 0) {
            return false;
        }
        
        int i =0;
        for (double[] s : spectra) {            
            int[] ext = Utils.getFreq(s);
            if (ext.length == 0) {
                return false;
            }
            boolean r = Utils.checkFreq(ext, F, FREQ_ERROR);
            if (!r) {
                errorCount++;
            }
        }
        return (errorCount <= MAX_ERRORS);
    }
    
    public void update(NotifyEvent event) {
        switch (event.getEventID()) {
            case SpectrumEvent.SPECTRA :
                SpectrumEvent evt = (SpectrumEvent) event;
                s.add(evt.getSpectra());
                break;
        }
    }
}