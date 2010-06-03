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
package org.mobicents.media.server.impl.rtp;


import java.net.InetAddress;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;


import org.mobicents.media.server.impl.clock.TimerImpl;
import org.mobicents.media.server.impl.resource.dtmf.DetectorImpl;
import org.mobicents.media.server.impl.resource.dtmf.GeneratorImpl;
import org.mobicents.media.server.impl.resource.test.TransmissionTester;
import org.mobicents.media.server.impl.resource.test.TransmissionTester2;
import org.mobicents.media.server.impl.rtp.clock.AudioClock;
import org.mobicents.media.server.impl.rtp.sdp.AVProfile;
import org.mobicents.media.server.spi.MediaType;
import org.mobicents.media.server.spi.NotificationListener;
import org.mobicents.media.server.spi.dsp.Codec;
import org.mobicents.media.server.spi.dsp.CodecFactory;
import org.mobicents.media.server.spi.events.NotifyEvent;

/**
 * 
 * @author amit bhayani
 * 
 */
public class RtpSocketTest implements NotificationListener {

    private final static int HEART_BEAT = 20;
    private final static int jitter = 60;
    private RtpFactory rtpFactory1 = null;
    private RtpFactory rtpFactory2 = null;
    private RtpSocket serverSocket;
    private RtpSocket clientSocket;
    private InetAddress localAddress;
    private int localPort1 = 9201;
    private int localPort2 = 9202;
    private TimerImpl timer = null;
    
    private TransmissionTester tester;
    private TransmissionTester2 tester2;
    
    private GeneratorImpl generator;
    private DetectorImpl detector;
        
    private RtpClock clock = new AudioClock();
    private int tone = 0;
    
    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        timer = new TimerImpl();
        timer.setHeartBeat(HEART_BEAT);
        timer.start();
        
        generator = new GeneratorImpl("dtmf-gen", timer);
        generator.setDigit("1");
        detector = new DetectorImpl("dtmf-det");
        detector.addListener(this);
        
        tester = new TransmissionTester(timer);
        tester2 = new TransmissionTester2(timer);

        ArrayList<CodecFactory> codecs = new ArrayList();
        codecs.add(new org.mobicents.media.server.impl.dsp.audio.g711.alaw.DecoderFactory());
        codecs.add(new org.mobicents.media.server.impl.dsp.audio.g711.alaw.EncoderFactory());
        
        Hashtable<MediaType, List<CodecFactory>> factories  = new Hashtable();
        factories.put(MediaType.AUDIO, codecs);
        
        Hashtable rtpMap = new Hashtable();        
        rtpMap.put(1, Codec.LINEAR_AUDIO);

        AVProfile profile = new AVProfile();
        profile.setProfile(rtpMap);
        
        rtpFactory1 = new RtpFactory();
        rtpFactory1.setJitter(jitter);
        rtpFactory1.setBindAddress("localhost");
        rtpFactory1.setTimer(timer);
        rtpFactory1.setAVProfile(profile);
        rtpFactory1.setCodecs(factories);
        rtpFactory1.setPeriod(20);
        rtpFactory1.start();

        rtpFactory2 = new RtpFactory();
        rtpFactory2.setJitter(jitter);
        rtpFactory2.setBindAddress("localhost");
        rtpFactory2.setTimer(timer);
        rtpFactory2.setAVProfile(profile);
        rtpFactory2.setCodecs(factories);
        rtpFactory2.setPeriod(20);
        rtpFactory2.start();

        localAddress = InetAddress.getByName("localhost");            
    }

    @After
    public void tearDown() {
        rtpFactory1.stop();
        rtpFactory2.stop();
        timer.stop();
    }

//    @Test
    public void testReceiveStreamConnection() throws Exception {
        serverSocket = rtpFactory1.getRTPSocket(MediaType.AUDIO);
        try {
            serverSocket.getReceiveStream().connect(tester.getDetector());
            fail("Format is not assigned for server socket");
        } catch (Exception e) {
        }
    }    

//    @Test
    public void testSendStreamConnection() throws Exception {
        serverSocket = rtpFactory1.getRTPSocket(MediaType.AUDIO);
        try {
            serverSocket.getSendStream().connect(tester.getGenerator());
            fail("Format is not assigned for server socket");
        } catch (Exception e) {
        }
    }    
    
    @Test
    public void testTransmission() throws Exception {
/*        serverSocket = rtpFactory1.getRTPSocket("audio");
        serverSocket.setFormat(1, Codec.LINEAR_AUDIO);
        
        clientSocket = rtpFactory2.getRTPSocket("audio");
        clientSocket.setFormat(1, Codec.LINEAR_AUDIO);
        
        serverSocket.setPeer(localAddress, localPort2);
        clientSocket.setPeer(localAddress, localPort1);

        serverSocket.getReceiveStream().connect(tester.getDetector());
        serverSocket.getReceiveStream().start();

        clientSocket.getSendStream().connect(tester.getGenerator());
        clientSocket.getSendStream().start();
        
        detector.start();
        tester.start();
        serverSocket.release();
        clientSocket.release();
        
        assertTrue(tester.getMessage(), tester.isPassed());
 */
    }

    @Test
    public void testTransmission2() throws Exception {
        serverSocket = rtpFactory1.getRTPSocket(MediaType.AUDIO);
        serverSocket.setFormat(1, AVProfile.PCMA);
        serverSocket.bind();
        
        clientSocket = rtpFactory2.getRTPSocket(MediaType.AUDIO);
        clientSocket.setFormat(1, AVProfile.PCMA);
        clientSocket.bind();
        
        serverSocket.setPeer(localAddress, clientSocket.getLocalPort());
        clientSocket.setPeer(localAddress, serverSocket.getLocalPort());

        serverSocket.getReceiveStream().connect(tester2.getDetector());

        clientSocket.getSendStream().connect(tester2.getGenerator());
        
        clientSocket.getSendStream().start();
        serverSocket.getReceiveStream().start();
        
        tester2.start();
        
        serverSocket.release();
        clientSocket.release();
        
        assertTrue(tester.getMessage(), tester2.isPassed());
    }

    @Test
    public void testRfc2833() throws Exception {
        testDtmf(true);
    }

    @Test
    public void testInbandDtmf() throws Exception {
        testDtmf(false);
    }
    
    public void testDtmf(boolean rfc2833) throws Exception {
        generator.setToneDuration(1000);
        serverSocket = rtpFactory1.getRTPSocket(MediaType.AUDIO);
        serverSocket.setFormat(1, Codec.LINEAR_AUDIO);
        if (rfc2833) {
            serverSocket.setDtmfPayload(101);
        }
        serverSocket.bind();
        
        clientSocket = rtpFactory2.getRTPSocket(MediaType.AUDIO);
        clientSocket.setJitter(0);
        clientSocket.setFormat(1, Codec.LINEAR_AUDIO);
        
        if (rfc2833) {
            clientSocket.setDtmfPayload(101);
        }
        clientSocket.bind();
        
        serverSocket.setPeer(localAddress, clientSocket.getLocalPort());
        clientSocket.setPeer(localAddress, serverSocket.getLocalPort());

        serverSocket.getReceiveStream().connect(detector);

        clientSocket.getSendStream().connect(generator);
        
        clientSocket.getSendStream().start();
        serverSocket.getReceiveStream().start();
        
        detector.start();
        generator.start();
        
        Thread.currentThread().sleep(2000);
        
        System.out.println(clientSocket.getBytesSent());
        System.out.println(serverSocket.getBytesReceived());
        serverSocket.release();
        clientSocket.release();
        
        assertEquals(1, tone);
    }
    
//    @Test
    public void testRelease() throws Exception {
        testTransmission();
        
        serverSocket.release();
        clientSocket.release();
        
        testTransmission();
    }

    public void update(NotifyEvent event) {
        tone = event.getEventID();
    }
}
