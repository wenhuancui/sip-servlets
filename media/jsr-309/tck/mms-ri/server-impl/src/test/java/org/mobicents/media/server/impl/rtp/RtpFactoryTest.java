package org.mobicents.media.server.impl.rtp;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mobicents.media.Format;
import org.mobicents.media.format.AudioFormat;
import org.mobicents.media.server.impl.clock.TimerImpl;
import org.mobicents.media.server.impl.rtp.sdp.AVProfile;
import org.mobicents.media.server.spi.Timer;

/**
 * 
 * @author amit bhayani
 * 
 */
public class RtpFactoryTest {

    private static final int HEART_BEAT = 20;
    private static final AudioFormat PCMA = new AudioFormat(AudioFormat.ALAW, 8000, 8, 1);
    private static final AudioFormat PCMU = new AudioFormat(AudioFormat.ULAW, 8000, 8, 1);
    private static Map<Integer, Format> formatMap = new HashMap<Integer, Format>();
    

    static {
        formatMap.put(0, PCMU);
        formatMap.put(8, PCMA);
    }
    private RtpFactory factory = null;

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        Hashtable payloads = new Hashtable();
        payloads.put(8, AVProfile.PCMA);
        payloads.put(0, AVProfile.PCMU);
        
        AVProfile profile = new AVProfile();
        profile.setProfile(payloads);
        factory = new RtpFactory();
        factory.setAVProfile(profile);

        Timer timer = new TimerImpl();
        timer.setHeartBeat(HEART_BEAT);

        Hashtable<String, Integer> ports = new Hashtable();
        ports.put("audio", 9201);
        
        factory.setTimer(timer);
        factory.setJitter(80);
        factory.setBindAddress("127.0.0.1");
        factory.setLocalPorts(ports);
        factory.start();
    }

    @After
    public void tearDown() {
        // Dont close the Factory as it will stop the RtpSocket.readerThread and
        // RtpSocketTest will be screwed
        // factory.stop();
    }

    @Test
    public void getRTPSocketTest() throws Exception {
        RtpSocket rtpSocket = factory.getRTPSocket("audio");
        int port = rtpSocket.getLocalPort();
        assertEquals(9201, port);
        assertEquals(80, rtpSocket.getJitter());
    }
}
