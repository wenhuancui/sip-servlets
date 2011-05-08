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

package org.mobicents.media.server.impl.resource.cnf;

import java.util.ArrayList;
import java.util.Timer;
import java.util.concurrent.Semaphore;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mobicents.media.Utils;
import org.mobicents.media.server.EndpointFactoryImpl;
import org.mobicents.media.server.impl.resource.fft.AnalyzerFactory;
import org.mobicents.media.server.impl.resource.fft.SpectrumEvent;
import org.mobicents.media.server.impl.resource.test.SineGeneratorFactory;
import org.mobicents.media.server.resource.ChannelFactory;
import org.mobicents.media.server.spi.NotificationListener;
import org.mobicents.media.server.spi.events.NotifyEvent;

/**
 *
 * @author kulikov
 */
public class ConferenceBridgeTest {
    private final static int FREQ_ERROR = 5;
    private int MAX_ERRORS = 3;

    private final static int[] FREQ = new int[]{50, 150, 250};
    private Timer timer;
    private EndpointFactoryImpl e1,  e2,  e3;
    private EndpointFactoryImpl cnf;
    private SineGeneratorFactory g1,  g2,  g3;
    private AnalyzerFactory a1,  a2,  a3;
    private ArrayList<double[]> s1,  s2,  s3;
    private ChannelFactory channelFactory;
    private CnfBridgeFactory cnfBridgeFactory;
    
    private Semaphore semaphore;
    private boolean res;

    public ConferenceBridgeTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
/*        short A = (short)(Short.MAX_VALUE / 3);
        
        semaphore = new Semaphore(0);
        res = false;

        s1 = new ArrayList();
        s2 = new ArrayList();
        s3 = new ArrayList();

        timer = new TimerImpl();
        timer.start();
        
        cnfBridgeFactory = new CnfBridgeFactory();
        cnfBridgeFactory.setName("Conf");

        channelFactory = new ChannelFactory();
        channelFactory.start();

        a1 = new AnalyzerFactory();
        a1.setName("a1");

        a2 = new AnalyzerFactory();
        a2.setName("a2");

        a3 = new AnalyzerFactory();
        a3.setName("a3");

        g1 = new SineGeneratorFactory();
        g1.setName("g1");
        g1.setF(FREQ[0]);
        g1.setA(A);
        
        g2 = new SineGeneratorFactory();
        g2.setName("g2");
        g2.setF(FREQ[1]);
        g2.setA(A);

        g3 = new SineGeneratorFactory();
        g3.setName("g3");
        g3.setF(FREQ[2]);
        g3.setA(A);

        e1 = new EndpointFactoryImpl("/cnf/test/1");
        e2 = new EndpointFactoryImpl("/cnf/test/2");
        e3 = new EndpointFactoryImpl("/cnf/test/3");

        cnf = new EndpointFactoryImpl("/cnf/test/cnf");

        Hashtable rxFactories = new Hashtable();
        rxFactories.put("audio", channelFactory);

        Hashtable txFactories = new Hashtable();
        txFactories.put("audio", channelFactory);
        
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setRxChannelFactory(rxFactories);
        connectionFactory.setTxChannelFactory(txFactories);
        
        e1.setConnectionFactory(connectionFactory);
        e2.setConnectionFactory(connectionFactory);
        e3.setConnectionFactory(connectionFactory);


        Hashtable src1 = new Hashtable();
        src1.put("audio", g1);

        Hashtable src2 = new Hashtable();
        src2.put("audio", g2);
        
        Hashtable src3 = new Hashtable();
        src3.put("audio", g3);
        
        e1.setSourceFactory(src1);
        e2.setSourceFactory(src2);
        e3.setSourceFactory(src3);

        Hashtable dst1 = new Hashtable();
        dst1.put("audio", a1);

        Hashtable dst2 = new Hashtable();
        dst2.put("audio", a2);
        
        Hashtable dst3 = new Hashtable();
        dst3.put("audio", a3);
        
        e1.setSinkFactory(dst1);
        e2.setSinkFactory(dst2);
        e3.setSinkFactory(dst3);
        
        cnf.setGroupFactory(cnfBridgeFactory);
        cnf.setConnectionFactory(connectionFactory);

        e1.start();
        e2.start();
        e3.start();

        cnf.start();
        
        e1.getComponent("a1").addListener(new AnalyzerListener(s1));
        e2.getComponent("a2").addListener(new AnalyzerListener(s2));
        e3.getComponent("a3").addListener(new AnalyzerListener(s3));
*/
    }

    @After
    public void tearDown() {
//        timer.stop();
    }

    /**
     * Test of getSource method, of class ConferenceBridge.
     */
    @Test
    public void testTransmission() throws Exception {
/*        Connection c1 = e1.createLocalConnection();
        c1.setMode(ConnectionMode.SEND_RECV);
        Connection c2 = e2.createLocalConnection();
        c2.setMode(ConnectionMode.SEND_RECV);
        Connection c3 = e3.createLocalConnection();
        c3.setMode(ConnectionMode.SEND_RECV);

        Connection cc1 = cnf.createLocalConnection();
        cc1.setMode(ConnectionMode.SEND_RECV);
        Connection cc2 = cnf.createLocalConnection();
        cc2.setMode(ConnectionMode.SEND_RECV);
        Connection cc3 = cnf.createLocalConnection();
        cc3.setMode(ConnectionMode.SEND_RECV);

        c1.setOtherParty(cc1);
        c2.setOtherParty(cc2);
        c3.setOtherParty(cc3);

        
        MediaSource gen1 = (MediaSource) e1.getComponent("g1");
        gen1.start();
        
        MediaSink an1 = (MediaSink) e1.getComponent("a1");
        an1.start();

        MediaSource gen2 = (MediaSource) e2.getComponent("g2");
        gen2.start();

        MediaSink an2 = (MediaSink) e2.getComponent("a2");
        an2.start();
        
        MediaSource gen3 = (MediaSource) e3.getComponent("g3");
        gen3.start();

        MediaSink an3 = (MediaSink) e3.getComponent("a3");
        an3.start();
        
//        semaphore.tryAcquire(15, TimeUnit.SECONDS);
        Thread.currentThread().sleep(10000);
        
//        gen2.stop();
//        gen3.stop();
//        gen3.stop();

        e1.deleteAllConnections();
        e2.deleteAllConnections();
        e3.deleteAllConnections();
        
        cnf.deleteAllConnections();
        //We have to wait, cause threads may not end immediatly...
        try{
        	Thread.currentThread().sleep(1000);
    	}catch(Exception e)
    	{}
        
        System.out.println("1");
        res = verify(s1, new int[]{FREQ[1], FREQ[2]});
        assertEquals(true, res);
        
        System.out.println("2");
        res = verify(s2, new int[]{FREQ[0], FREQ[2]});
//        assertEquals(true, res);

        System.out.println("3");
        res = verify(s3, new int[]{FREQ[0], FREQ[1]});
        assertEquals(true, res);
*/        
    }

    private boolean verify(ArrayList<double[]> spectra, int[] F) {
        int errorCount = 0;
        if (spectra.size() == 0) {
            return false;
        }
        
        int i =0;
        for (double[] s : spectra) {            
            int[] ext = Utils.getFreq(s);
            boolean r = Utils.checkFreq(ext, F, FREQ_ERROR);
            if (!r) {
                errorCount++;
            }
        }
        return (errorCount <= MAX_ERRORS);
    }

    private class AnalyzerListener implements NotificationListener {

        private ArrayList<double[]> s;

        public AnalyzerListener(ArrayList<double[]> s) {
            this.s = s;
        }

        public void update(NotifyEvent event) {
            switch (event.getEventID()) {
                case SpectrumEvent.SPECTRA :
                    SpectrumEvent evt = (SpectrumEvent) event;
                    s.add(evt.getSpectra());
                    break;
                case NotifyEvent.START_FAILED :
                    break;
            }
        }
    }
}