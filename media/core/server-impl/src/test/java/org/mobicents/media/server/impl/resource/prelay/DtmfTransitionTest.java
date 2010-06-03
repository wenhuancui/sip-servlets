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
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mobicents.media.Format;
import org.mobicents.media.server.ConnectionFactory;
import org.mobicents.media.server.EndpointImpl;
import org.mobicents.media.server.impl.clock.TimerImpl;
import org.mobicents.media.server.impl.dsp.DspFactory;
import org.mobicents.media.server.impl.dsp.audio.g711.ulaw.DecoderFactory;
import org.mobicents.media.server.impl.dsp.audio.g711.ulaw.EncoderFactory;
import org.mobicents.media.server.impl.resource.audio.AudioNoiseGeneratorFactory;
import org.mobicents.media.server.impl.resource.cnf.AudioMixerFactory;
import org.mobicents.media.server.impl.resource.cnf.SplitterFactory;
import org.mobicents.media.server.impl.resource.dtmf.DetectorFactory;
import org.mobicents.media.server.impl.resource.dtmf.DtmfEvent;
import org.mobicents.media.server.impl.resource.dtmf.GeneratorFactory;
import org.mobicents.media.server.impl.rtp.RtpFactory;
import org.mobicents.media.server.impl.rtp.sdp.AVProfile;
import org.mobicents.media.server.resource.ChannelFactory;
import org.mobicents.media.server.resource.PipeFactory;
import org.mobicents.media.server.spi.Connection;
import org.mobicents.media.server.spi.ConnectionMode;
import org.mobicents.media.server.spi.MediaType;
import org.mobicents.media.server.spi.NotificationListener;
import org.mobicents.media.server.spi.Valve;
import org.mobicents.media.server.spi.clock.Timer;
import org.mobicents.media.server.spi.dsp.CodecFactory;
import org.mobicents.media.server.spi.events.NotifyEvent;
import org.mobicents.media.server.spi.resource.DtmfDetector;
import org.mobicents.media.server.spi.resource.DtmfGenerator;
import static org.junit.Assert.*;

/**
 * 
 * @author kulikov
 */
public class DtmfTransitionTest implements NotificationListener {

    private Timer timer;
    
    private EndpointImpl user, ivr, relay;
//    private EndpointImpl testerEndpoint,  echoEndpoint;
    
//    private EndpointImpl packetRelayEnp;
    
//    private BridgeFactory packetRelayFactory;
//    private ChannelFactory channelFactory;
//    private DspFactory dspFactory;
    private RtpFactory rtpFactory;
    private int tone;
//    private TransmissionTester2 tester;
//    private Proxy proxy;
    
//    private ConnectionFactory connectionFactory;
    
    public DtmfTransitionTest() {
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
        
        setupRTP();
        
        user = setupIVR("/test/ua");
        ivr = setupIVR("/test/ivr");
        setupPacketRelay();
    }

    public EndpointImpl setupIVR(String name) throws Exception {
        // preparing g711: ALaw encoder, decoder
        AudioMixerFactory mixerFactory = new AudioMixerFactory();
        mixerFactory.setName("mixer");
        
        SplitterFactory splitterFactory = new SplitterFactory();
        splitterFactory.setName("splitter");
        
        GeneratorFactory generatorFactory = new GeneratorFactory();
        generatorFactory.setName("dtmf.gen");
        
        DetectorFactory detectorFactory = new DetectorFactory();
        detectorFactory.setName("dtmf.det");
        
        // creating component list
        ArrayList txComponents = new ArrayList();
        txComponents.add(mixerFactory);
        txComponents.add(generatorFactory);

        // define pipes
        PipeFactory p1 = new PipeFactory();
        p1.setInlet(null);
        p1.setOutlet("mixer");
        p1.setValve(Valve.OPEN);
        
        PipeFactory p2 = new PipeFactory();
        p2.setInlet("dtmf.gen");
        p2.setOutlet("mixer");
        p2.setValve(Valve.CLOSE);
        
        PipeFactory p3 = new PipeFactory();
        p3.setInlet("mixer");
        p3.setOutlet(null);
        p3.setValve(Valve.OPEN);
        
        ArrayList pipes = new ArrayList();
        pipes.add(p1);
        pipes.add(p2);
        pipes.add(p3);
        
        ChannelFactory txChannelFactory = new ChannelFactory();
        txChannelFactory.start();
        
        txChannelFactory.setComponents(txComponents);
        txChannelFactory.setPipes(pipes);

        //RX
        // creating component list
        ArrayList rxComponents = new ArrayList();
        rxComponents.add(splitterFactory);
        rxComponents.add(detectorFactory);

        // define pipes
        PipeFactory p5 = new PipeFactory();
        p5.setInlet(null);
        p5.setOutlet("splitter");
        p5.setValve(Valve.OPEN);
        
        PipeFactory p7 = new PipeFactory();
        p7.setInlet("splitter");
        p7.setOutlet("dtmf.det");
        p7.setValve(Valve.CLOSE);
        
        PipeFactory p8 = new PipeFactory();
        p8.setInlet("splitter");
        p8.setOutlet(null);
        p8.setValve(Valve.OPEN);
        
        ArrayList rxpipes = new ArrayList();
        rxpipes.add(p5);
        rxpipes.add(p7);
        rxpipes.add(p8);
        
        ChannelFactory rxChannelFactory = new ChannelFactory();
        rxChannelFactory.start();
        
        rxChannelFactory.setComponents(rxComponents);
        rxChannelFactory.setPipes(rxpipes);
        
        Hashtable rxFactories = new Hashtable();
        rxFactories.put("audio", rxChannelFactory);

        Hashtable txFactories = new Hashtable();
        txFactories.put("audio", txChannelFactory);
        
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setRxChannelFactory(rxFactories);
        connectionFactory.setTxChannelFactory(txFactories);
        
        //sink and source
        AudioNoiseGeneratorFactory noiseGeneratorFactory = new AudioNoiseGeneratorFactory();
        HashMap sources = new HashMap();
        sources.put("audio", noiseGeneratorFactory);
        
        EndpointImpl endpoint = new EndpointImpl(name);
        endpoint.setTimer(timer);
        endpoint.setConnectionFactory(connectionFactory);
        endpoint.setRtpFactory(rtpFactory);
        endpoint.setSourceFactory(sources);
        endpoint.start();
        
        return endpoint;
    }
    
    @After
    public void tearDown() {
        rtpFactory.stop();
        timer.stop();
    }

    private void setupRTP() throws Exception {
        Hashtable<Integer, Format> rtpMap = new Hashtable();
        rtpMap.put(3, AVProfile.PCMU);
        rtpMap.put(101, AVProfile.DTMF);

        AVProfile profile = new AVProfile();
        profile.setProfile(rtpMap);
        
        // preparing g711: ALaw encoder, decoder
        EncoderFactory encoderFactory = new EncoderFactory();
        DecoderFactory decoderFactory = new DecoderFactory();

        // group codecs into list
        ArrayList<CodecFactory> codecs = new ArrayList();
        codecs.add(encoderFactory);
        codecs.add(decoderFactory);

        // creating dsp factory with g711 encoder/decoder
        Hashtable<MediaType, List<CodecFactory>> codecFactories = new Hashtable();
        codecFactories.put(MediaType.AUDIO, codecs);
        
        
        rtpFactory = new RtpFactory();
        rtpFactory.setCodecs(codecFactories);
        
        rtpFactory.setBindAddress("localhost");
        rtpFactory.setTimer(timer);
        rtpFactory.setAVProfile(profile);
        rtpFactory.setPeriod(20);
        rtpFactory.start();
    }
    
    private void setupPacketRelay() throws Exception {
        ChannelFactory channelFactory = new ChannelFactory();
        channelFactory.start();
        
        Hashtable rxFactories1 = new Hashtable();
        rxFactories1.put("audio", channelFactory);

        Hashtable txFactories1 = new Hashtable();
        txFactories1.put("audio", channelFactory);
        
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setRxChannelFactory(rxFactories1);
        connectionFactory.setTxChannelFactory(txFactories1);
        
        // configuring Packet relay endpoint
        ArrayList<CodecFactory> codecs = new ArrayList();
        codecs.add(new EncoderFactory());
        codecs.add(new DecoderFactory());
        
        DspFactory dspFactory = new DspFactory();
        dspFactory.setName("dsp");
        
        dspFactory.setCodecFactories(codecs);
        
        BridgeFactory packetRelayFactory = new BridgeFactory();
        packetRelayFactory.setName("Packet-Relay");
        packetRelayFactory.setDspFactory(dspFactory);
        
        relay = new EndpointImpl("/pr/test/cnf");
        relay.setGroupFactory(packetRelayFactory);

        relay.setTimer(timer);
        relay.setConnectionFactory(connectionFactory);
        relay.setRtpFactory(rtpFactory);

        // strating packet relay endpoint
        relay.start();
    }
    
    /**
     * Test of getSink method, of class Bridge.
     */
    @Test
    public void testDtmfTransmission() throws Exception {
        Connection uaConnection = user.createConnection();
        uaConnection.setMode(ConnectionMode.SEND_RECV);
        
        Connection connection1 = relay.createConnection();
        connection1.setMode(ConnectionMode.SEND_RECV);

        connection1.setRemoteDescriptor(uaConnection.getLocalDescriptor());
        uaConnection.setRemoteDescriptor(connection1.getLocalDescriptor());
        
        Connection ivrConnection = ivr.createLocalConnection();
        ivrConnection.setMode(ConnectionMode.SEND_RECV);
        
        Connection connection2 = relay.createLocalConnection();
        connection2.setMode(ConnectionMode.SEND_RECV);
        
        connection2.setOtherParty(ivrConnection);
        ivrConnection.setOtherParty(connection2);

        DtmfDetector det = (DtmfDetector)ivrConnection.getComponent(MediaType.AUDIO, DtmfDetector.class);
        det.addListener(this);
        det.start();
        
        DtmfGenerator gen = (DtmfGenerator)uaConnection.getComponent(MediaType.AUDIO, DtmfGenerator.class);
        gen.setDigit("5");
        gen.setToneDuration(80);
        gen.start();
        
        Thread.currentThread().sleep(3000);
        
        assertEquals(DtmfEvent.DTMF_5, tone);        
    }

    public void update(NotifyEvent event) {
        tone = event.getEventID();
    }
}