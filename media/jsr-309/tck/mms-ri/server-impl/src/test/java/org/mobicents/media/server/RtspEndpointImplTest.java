/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mobicents.media.server;

import java.net.URL;
import java.util.Hashtable;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mobicents.media.server.impl.clock.TimerImpl;
import org.mobicents.media.server.impl.resource.video.AVPlayerFactory;
import org.mobicents.media.server.impl.rtp.RtpClock;
import org.mobicents.media.server.impl.rtp.RtpFactory;
import org.mobicents.media.server.impl.rtp.clock.AudioClock;
import org.mobicents.media.server.spi.Connection;
import org.mobicents.media.server.spi.ConnectionMode;
import org.mobicents.media.server.spi.Timer;
import org.mobicents.media.server.spi.resource.Player;

/**
 *
 * @author kulikov
 */
public class RtspEndpointImplTest {

    private Timer timer;
    private RtspEndpointImpl sender;
    private RtpFactory rtpFactory;
    private RtpClock clock = new AudioClock();
    private AVPlayerFactory playerFactory;
    
    public RtspEndpointImplTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        timer = new TimerImpl();
        timer.start();
        
        Hashtable<String, Integer> ports = new Hashtable();
        ports.put("audio", 9201);
        ports.put("video", 9203);
        
        rtpFactory = new RtpFactory();
        rtpFactory.setBindAddress("127.0.0.1");
        rtpFactory.setLocalPorts(ports);
        rtpFactory.setTimer(timer);
        rtpFactory.setPeriod(20);
        rtpFactory.start();
        
        playerFactory = new AVPlayerFactory();
        playerFactory.setName("AVPlayer");
        
        sender = new RtspEndpointImpl("rtsp/test");
        sender.setTimer(timer);
        sender.setRtpFactory(rtpFactory);
        sender.setPlayer(playerFactory);
        sender.start();
    }

    @After
    public void tearDown() {
        sender.stop();
        timer.stop();
    }


    @Test
    public void testSDP() throws Exception {
        String file = "org/mobicents/media/server/impl/resource/video/sample_50kbit.3gp";
        URL url = RtspEndpointImplTest.class.getClassLoader().getResource(file);
        Player player = (Player) sender.getComponent("AVPlayer");        
        player.setURL(url.getPath());
        Connection connection = sender.createConnection(ConnectionMode.SEND_ONLY);
        System.out.println("SDP=" + connection.getLocalDescriptor());
    }
}