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
package org.mobicents.media.server.impl.resource.prelay;

import java.util.ArrayList;
import java.util.Hashtable;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mobicents.media.Component;
import org.mobicents.media.ComponentFactory;
import org.mobicents.media.Format;
import org.mobicents.media.server.ConnectionFactory;
import org.mobicents.media.server.EndpointImpl;
import org.mobicents.media.server.impl.clock.TimerImpl;
import org.mobicents.media.server.impl.dsp.DspFactory;
import org.mobicents.media.server.impl.dsp.audio.g711.alaw.DecoderFactory;
import org.mobicents.media.server.impl.dsp.audio.g711.alaw.EncoderFactory;
import org.mobicents.media.server.impl.resource.Proxy;
import org.mobicents.media.server.impl.resource.ProxySinkFactory;
import org.mobicents.media.server.impl.resource.ProxySourceFactory;
import org.mobicents.media.server.impl.resource.test.TransmissionTester2;
import org.mobicents.media.server.impl.rtp.RtpFactory;
import org.mobicents.media.server.impl.rtp.sdp.AVProfile;
import org.mobicents.media.server.resource.ChannelFactory;
import org.mobicents.media.server.spi.Connection;
import org.mobicents.media.server.spi.ConnectionMode;
import org.mobicents.media.server.spi.Endpoint;
import org.mobicents.media.server.spi.clock.Timer;
import org.mobicents.media.server.spi.dsp.CodecFactory;
import static org.junit.Assert.*;

/**
 * 
 * @author kulikov
 */
public class TranscodingBridgeTest {

    private Timer timer;
    private EndpointImpl testerEndpoint,  echoEndpoint;
    private EndpointImpl packetRelayEnp;
    
    private BridgeFactory packetRelayFactory;
    private ChannelFactory channelFactory;
    private DspFactory dspFactory;
    private RtpFactory rtpFactory, rtpFactory2;

    private TransmissionTester2 tester;
    private Proxy proxy;
    
    private ConnectionFactory connectionFactory;
    
    public TranscodingBridgeTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        // creating timer
        timer = new TimerImpl();
        timer.start();
        tester = new TransmissionTester2(timer);
        
        setupRTP();
        
        // creating transparent channels
        channelFactory = new ChannelFactory();
        channelFactory.start();

        Hashtable rxFactories1 = new Hashtable();
        rxFactories1.put("audio", channelFactory);

        Hashtable txFactories1 = new Hashtable();
        txFactories1.put("audio", channelFactory);
        
        connectionFactory = new ConnectionFactory();
        connectionFactory.setRxChannelFactory(rxFactories1);
        connectionFactory.setTxChannelFactory(txFactories1);

        setupPacketRelay();
        setupTester();
        setupEcho();
    }

    @After
    public void tearDown() {
        rtpFactory.stop();
        rtpFactory2.stop();
        timer.stop();
    }

    private void setupRTP() throws Exception {
        Hashtable<Integer, Format> rtpMap = new Hashtable();
        rtpMap.put(1, AVProfile.PCMA);

        AVProfile profile = new AVProfile();
        profile.setProfile(rtpMap);
        
        rtpFactory = new RtpFactory();
        rtpFactory.setBindAddress("localhost");
        rtpFactory.setTimer(timer);
        rtpFactory.setAVProfile(profile);
        rtpFactory.setPeriod(20);
        rtpFactory.start();
    }
    
    private void setupPacketRelay() throws Exception {
        // configuring Packet relay endpoint
        ArrayList<CodecFactory> codecs = new ArrayList();
        codecs.add(new EncoderFactory());
        codecs.add(new DecoderFactory());
        dspFactory = new DspFactory();
        dspFactory.setName("dsp");
        
        dspFactory.setCodecFactories(codecs);
        
        packetRelayFactory = new BridgeFactory();
        packetRelayFactory.setName("Packet-Relay");
        packetRelayFactory.setDspFactory(dspFactory);
        
        packetRelayEnp = new EndpointImpl("/pr/test/cnf");
        packetRelayEnp.setGroupFactory(packetRelayFactory);

        packetRelayEnp.setTimer(timer);
        packetRelayEnp.setConnectionFactory(connectionFactory);
        packetRelayEnp.setRtpFactory(rtpFactory);

        // strating packet relay endpoint
        packetRelayEnp.start();
    }
    
    private void setupTester() throws Exception {
        tester = new TransmissionTester2(timer);

        TestSourceFactory genFactory = new TestSourceFactory(tester);
        TestSinkFactory detFactory = new TestSinkFactory(tester);

        // configuring sender
        testerEndpoint = new EndpointImpl("/pr/test/sender");
        testerEndpoint.setTimer(timer);
        testerEndpoint.setConnectionFactory(connectionFactory);
        
        Hashtable sources = new Hashtable();
        sources.put("audio", genFactory);
        
        Hashtable sinks = new Hashtable();
        sinks.put("audio", detFactory);
        
        testerEndpoint.setSourceFactory(sources);
        testerEndpoint.setSinkFactory(sinks);
        testerEndpoint.start();
    }
    
    private void setupEcho() throws Exception {
        proxy = new Proxy("proxy");
        proxy.setFormat(new Format[]{AVProfile.PCMA});
        
        proxy.getInput().start();
        proxy.getOutput().start();
        
        ProxySourceFactory sourceFactory = new ProxySourceFactory(proxy);
        ProxySinkFactory sinkFactory = new ProxySinkFactory(proxy);
    
        Hashtable<Integer, Format> rtpMap = new Hashtable();
        rtpMap.put(1, AVProfile.PCMA);
        
        AVProfile profile = new AVProfile();
        profile.setProfile(rtpMap);
        
        rtpFactory2 = new RtpFactory();
        rtpFactory2.setBindAddress("localhost");
        rtpFactory2.setTimer(timer);
        rtpFactory2.setAVProfile(profile);
        rtpFactory2.setPeriod(20);
        rtpFactory2.start();

        Hashtable<String, RtpFactory> rtpFactories2 = new Hashtable();
        rtpFactories2.put("audio", rtpFactory2);
        
        echoEndpoint = new EndpointImpl("/pr/test/echo");
        echoEndpoint.setTimer(timer);
        echoEndpoint.setConnectionFactory(connectionFactory);
        
        Hashtable sources = new Hashtable();
        sources.put("audio", sourceFactory);
        
        Hashtable sinks = new Hashtable();
        sinks.put("audio", sinkFactory);
        
        echoEndpoint.setSinkFactory(sinks);
        echoEndpoint.setSourceFactory(sources);
        
        echoEndpoint.setRtpFactory(rtpFactory2);
        echoEndpoint.start();
    }
    /**
     * Test of getSink method, of class Bridge.
     */
//    @Test
    public void testSimpleTransmission() throws Exception {
        Connection txConnection = testerEndpoint.createLocalConnection();
        txConnection.setMode(ConnectionMode.SEND_RECV);
        Connection rxConnection = echoEndpoint.createLocalConnection();
        rxConnection.setMode(ConnectionMode.SEND_RECV);

        Connection rxC = packetRelayEnp.createLocalConnection();
        rxC.setMode(ConnectionMode.SEND_RECV);
        Connection txC = packetRelayEnp.createLocalConnection();
        txC.setMode(ConnectionMode.SEND_RECV);

        rxC.setOtherParty(txConnection);
        txC.setOtherParty(rxConnection);

        tester.start();
        
        echoEndpoint.deleteAllConnections();
        testerEndpoint.deleteAllConnections();

        packetRelayEnp.deleteAllConnections();
        
        assertTrue(tester.getMessage(), tester.isPassed());
    }

    private void runRtpTransmission() throws Exception {
        Connection testerConnection = testerEndpoint.createLocalConnection();
        testerConnection.setMode(ConnectionMode.SEND_RECV);
        Connection echoConnection = echoEndpoint.createConnection();
        echoConnection.setMode(ConnectionMode.SEND_RECV);

        Connection connection1 = packetRelayEnp.createLocalConnection();
        connection1.setMode(ConnectionMode.SEND_RECV);
        connection1.setOtherParty(testerConnection);
        
        Connection connection2 = packetRelayEnp.createConnection();
        connection2.setMode(ConnectionMode.SEND_RECV);

        connection2.setRemoteDescriptor(echoConnection.getLocalDescriptor());
        echoConnection.setRemoteDescriptor(connection2.getLocalDescriptor());

        tester.start();
        
        testerEndpoint.deleteAllConnections();

        packetRelayEnp.deleteAllConnections();
        echoEndpoint.deleteAllConnections();
        assertTrue(tester.getMessage(), tester.isPassed());        
    }

    @Test
    public void testRtpTransmission() throws Exception {
        for (int i = 0; i < 1; i++) {
            runRtpTransmission();
        }
    }

    private class TestSourceFactory implements ComponentFactory {

        private TransmissionTester2 tester;
        
        public TestSourceFactory(TransmissionTester2 tester) {
            this.tester = tester;
        }
        public String getName() {
            return tester.getGenerator().getName();
        }

        public Component newInstance(Endpoint endpoint) {
            return tester.getGenerator();
        }
    }

    private class TestSinkFactory implements ComponentFactory {

        private TransmissionTester2 tester;
        
        public TestSinkFactory(TransmissionTester2 tester) {
            this.tester = tester;
        }

        public String getName() {
            return tester.getDetector().getName();
        }


        public Component newInstance(Endpoint endpoint) {
            return tester.getDetector();
        }
    }

}