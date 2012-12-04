package org.mobicents.media.server.impl.rtp;


import java.net.InetAddress;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
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
//        detector.start();
        
        tester = new TransmissionTester(timer);
        tester2 = new TransmissionTester2(timer);

        ArrayList<CodecFactory> codecs = new ArrayList();
        codecs.add(new org.mobicents.media.server.impl.dsp.audio.g711.alaw.DecoderFactory());
        codecs.add(new org.mobicents.media.server.impl.dsp.audio.g711.alaw.EncoderFactory());
        
        Hashtable<String, List<CodecFactory>> factories  = new Hashtable();
        factories.put("audio", codecs);
        
        Hashtable rtpMap = new Hashtable();        
        rtpMap.put(1, Codec.LINEAR_AUDIO);

        AVProfile profile = new AVProfile();
        profile.setProfile(rtpMap);
        
        Hashtable localPorts1 = new Hashtable();
        localPorts1.put("audio", localPort1);
        
        rtpFactory1 = new RtpFactory();
        rtpFactory1.setJitter(jitter);
        rtpFactory1.setBindAddress("localhost");
        rtpFactory1.setLocalPorts(localPorts1);
        rtpFactory1.setTimer(timer);
        rtpFactory1.setAVProfile(profile);
        rtpFactory1.setCodecs(factories);
        rtpFactory1.setPeriod(20);
        rtpFactory1.start();

        Hashtable localPorts2 = new Hashtable();
        localPorts2.put("audio", localPort2);
        
        rtpFactory2 = new RtpFactory();
        rtpFactory2.setJitter(jitter);
        rtpFactory2.setBindAddress("localhost");
        rtpFactory2.setLocalPorts(localPorts2);
        rtpFactory2.setTimer(timer);
        rtpFactory2.setAVProfile(profile);
        rtpFactory2.setCodecs(factories);
        rtpFactory2.setPeriod(20);
        rtpFactory2.start();

        try {
            localAddress = InetAddress.getByName("localhost");
        } catch (Exception e) {
        }


    }

    @After
    public void tearDown() {
        rtpFactory1.stop();
        rtpFactory2.stop();
        timer.stop();
    }

//    @Test
    public void testReceiveStreamConnection() throws Exception {
        serverSocket = rtpFactory1.getRTPSocket("audio");
        try {
            serverSocket.getReceiveStream().connect(tester.getDetector());
            fail("Format is not assigned for server socket");
        } catch (Exception e) {
        }
    }    

//    @Test
    public void testSendStreamConnection() throws Exception {
        serverSocket = rtpFactory1.getRTPSocket("audio");
        try {
            serverSocket.getSendStream().connect(tester.getGenerator());
            fail("Format is not assigned for server socket");
        } catch (Exception e) {
        }
    }    
    
 //   @Test
    public void testTransmission() throws Exception {
        serverSocket = rtpFactory1.getRTPSocket("audio");
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
    }

 //   @Test
    public void testTransmission2() throws Exception {
        serverSocket = rtpFactory1.getRTPSocket("audio");
        serverSocket.setFormat(1, AVProfile.PCMA);
        
        clientSocket = rtpFactory2.getRTPSocket("audio");
        clientSocket.setFormat(1, AVProfile.PCMA);
        
        serverSocket.setPeer(localAddress, localPort2);
        clientSocket.setPeer(localAddress, localPort1);

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
    public void testRfc2833Dtmf() throws Exception {
        generator.setToneDuration(1000);
        serverSocket = rtpFactory1.getRTPSocket("audio");
        serverSocket.setFormat(1, Codec.LINEAR_AUDIO);
        serverSocket.setDtmfPayload(101);
        
        clientSocket = rtpFactory2.getRTPSocket("audio");
        clientSocket.setJitter(0);
        clientSocket.setFormat(1,  Codec.LINEAR_AUDIO);
        clientSocket.setDtmfPayload(101);
        
        serverSocket.setPeer(localAddress, localPort2);
        clientSocket.setPeer(localAddress, localPort1);

        serverSocket.getReceiveStream().connect(detector);

        clientSocket.getSendStream().connect(generator);
        
        clientSocket.getSendStream().start();
        serverSocket.getReceiveStream().start();
        
        detector.start();
        generator.start();
        
        Thread.currentThread().sleep(2000);
        
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
