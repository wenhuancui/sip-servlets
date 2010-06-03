/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mobicents.media.server;

import static org.junit.Assert.*;

import java.util.Hashtable;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mobicents.media.Format;
import org.mobicents.media.server.impl.clock.TimerImpl;
import org.mobicents.media.server.impl.resource.test.TesterSinkFactory;
import org.mobicents.media.server.impl.resource.test.TesterSourceFactory;
import org.mobicents.media.server.impl.resource.test.TransmissionTester;
import org.mobicents.media.server.impl.rtp.RtpClock;
import org.mobicents.media.server.impl.rtp.RtpFactory;
import org.mobicents.media.server.impl.rtp.clock.AudioClock;
import org.mobicents.media.server.impl.rtp.sdp.AVProfile;
import org.mobicents.media.server.resource.ChannelFactory;
import org.mobicents.media.server.spi.Connection;
import org.mobicents.media.server.spi.ConnectionMode;
import org.mobicents.media.server.spi.Timer;
import org.mobicents.media.server.spi.dsp.Codec;

/**
 *
 * @author kulikov
 */
public class RtpConnectionTest {

    private Timer timer;
    private EndpointImpl sender;
    private EndpointImpl receiver;
    private int localPort1 = 9201;
    private int localPort2 = 9202;
    private ChannelFactory channelFactory;
    private RtpFactory rtpFactory1,  rtpFactory2;

    private TransmissionTester tester;
    private TesterSourceFactory sourceFactory;
    private TesterSinkFactory sinkFactory;
    private RtpClock clock = new AudioClock();
    
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
        
        tester = new TransmissionTester(timer);
        
        sourceFactory = new TesterSourceFactory(tester);
        sinkFactory = new TesterSinkFactory(tester);
        
        Hashtable<Integer, Format> rtpMap = new Hashtable();
        rtpMap.put(1, Codec.LINEAR_AUDIO);

        AVProfile profile = new AVProfile();
        profile.setProfile(rtpMap);
        
        Hashtable localPorts1 = new Hashtable();
        localPorts1.put("audio", localPort1);
        
        rtpFactory1 = new RtpFactory();
        rtpFactory1.setBindAddress("127.0.0.1");
        rtpFactory1.setLocalPorts(localPorts1);
        rtpFactory1.setTimer(timer);
        rtpFactory1.setAVProfile(profile);
        rtpFactory1.setPeriod(20);
        rtpFactory1.start();

        Hashtable localPorts2 = new Hashtable();
        localPorts2.put("audio", localPort2);
        
        rtpFactory2 = new RtpFactory();
        rtpFactory2.setBindAddress("127.0.0.1");
        rtpFactory2.setLocalPorts(localPorts2);
        rtpFactory2.setTimer(timer);
        rtpFactory2.setAVProfile(profile);
        rtpFactory2.setPeriod(20);
        rtpFactory2.start();


        channelFactory = new ChannelFactory();
        channelFactory.start();

        Hashtable rxFactories = new Hashtable();
        rxFactories.put("audio", channelFactory);

        Hashtable txFactories = new Hashtable();
        txFactories.put("audio", channelFactory);
        
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setRxChannelFactory(rxFactories);
        connectionFactory.setTxChannelFactory(txFactories);
        
        sender = new EndpointImpl("test/announcement/sender");
        sender.setTimer(timer);

        Hashtable sources = new Hashtable();
        sources.put("audio", sourceFactory);
        
        Hashtable sinks = new Hashtable();
        sinks.put("audio", sinkFactory);
        
        sender.setRtpFactory(rtpFactory1);
        sender.setSourceFactory(sources);
        sender.setConnectionFactory(connectionFactory);

        sender.start();

        receiver = new EndpointImpl("test/announcement/receiver");
        receiver.setTimer(timer);

        receiver.setRtpFactory(rtpFactory2);
        receiver.setSinkFactory(sinks);
        receiver.setConnectionFactory(connectionFactory);

        receiver.start();
    }

    @After
    public void tearDown() {
        timer.stop();
    }

    /**
     * Test of getLocalName method, of class EndpointImpl.
     */
    @Test
    public void testTransmission() throws Exception {
        Connection rxConnection = receiver.createConnection(ConnectionMode.RECV_ONLY);
        Connection txConnection = sender.createConnection(ConnectionMode.SEND_ONLY);

        txConnection.setRemoteDescriptor(rxConnection.getLocalDescriptor());
        String sdp = txConnection.getLocalDescriptor();
        rxConnection.setRemoteDescriptor(sdp);

        tester.start();
        assertTrue(tester.getMessage(), tester.isPassed());
    }

}