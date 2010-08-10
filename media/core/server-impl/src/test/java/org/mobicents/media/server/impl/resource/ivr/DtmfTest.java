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
package org.mobicents.media.server.impl.resource.ivr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mobicents.media.server.ConnectionFactory;
import org.mobicents.media.server.EndpointFactoryImpl;
import org.mobicents.media.server.impl.dsp.DspFactory;
import org.mobicents.media.server.impl.dsp.audio.g711.alaw.DecoderFactory;
import org.mobicents.media.server.impl.dsp.audio.g711.alaw.EncoderFactory;
import org.mobicents.media.server.impl.resource.Proxy;
import org.mobicents.media.server.impl.resource.audio.AudioNoiseGeneratorFactory;
import org.mobicents.media.server.impl.resource.cnf.AudioMixerFactory;
import org.mobicents.media.server.impl.resource.cnf.SplitterFactory;
import org.mobicents.media.server.impl.resource.dtmf.DetectorFactory;
import org.mobicents.media.server.impl.resource.dtmf.GeneratorFactory;
import org.mobicents.media.server.impl.rtp.RtpClock;
import org.mobicents.media.server.impl.rtp.RtpFactory;
import org.mobicents.media.server.impl.rtp.clock.AudioClock;
import org.mobicents.media.server.resource.ChannelFactory;
import org.mobicents.media.server.resource.PipeFactory;
import org.mobicents.media.server.spi.Connection;
import org.mobicents.media.server.spi.ConnectionMode;
import org.mobicents.media.server.spi.MediaType;
import org.mobicents.media.server.spi.NotificationListener;
import org.mobicents.media.server.spi.events.NotifyEvent;
import org.mobicents.media.server.spi.resource.DtmfDetector;
import org.mobicents.media.server.spi.resource.DtmfGenerator;
import static org.junit.Assert.*;

/**
 * 
 * @author kulikov
 */
public class DtmfTest implements NotificationListener {

    private int localPort1 = 9201;
    private int localPort2 = 9202;
    
    private EndpointFactoryImpl ivrEndpoint1, ivrEndpoint2;
    
    private RtpFactory rtpFactory1, rtpFactory2;
    private EncoderFactory encoderFactory;
    private DecoderFactory decoderFactory;
    private DspFactory dspFactory;

    private Proxy proxy;
    private AudioMixerFactory mixerFactory;
    private SplitterFactory splitterFactory;
    private DetectorFactory detectorFactory;
    private GeneratorFactory generatorFactory;
    private AudioNoiseGeneratorFactory noiseGeneratorFactory;
    
    private RtpClock clock = new AudioClock();
    private int digit = 0;
    
    public DtmfTest() {
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
//        timer = new TimerImpl();
//        timer.start();
        
        
//        setupIVR();
    }

    @After
    public void tearDown() {
//        rtpFactory1.stop();
//        rtpFactory2.stop();
//        timer.stop();
    }

    private void setupIVR() throws Exception {
/*        rtpFactory1 = new RtpFactory();
        rtpFactory1.setBindAddress("localhost");
        rtpFactory1.setTimer(timer);

        rtpFactory2 = new RtpFactory();
        rtpFactory2.setBindAddress("localhost");
        rtpFactory2.setTimer(timer);
        

        // preparing g711: ALaw encoder, decoder
        encoderFactory = new EncoderFactory();
        decoderFactory = new DecoderFactory();

        // group codecs into list
        ArrayList codecs = new ArrayList();
        codecs.add(encoderFactory);
        codecs.add(decoderFactory);

        // creating dsp factory with g711 encoder/decoder
        dspFactory = new DspFactory();
        dspFactory.setName("dsp");
        dspFactory.setCodecFactories(codecs);
        
        mixerFactory = new AudioMixerFactory();
        mixerFactory.setName("mixer");
        
        splitterFactory = new SplitterFactory();
        splitterFactory.setName("splitter");
        
        generatorFactory = new GeneratorFactory();
        generatorFactory.setName("dtmf.gen");
        
        detectorFactory = new DetectorFactory();
        detectorFactory.setName("dtmf.det");
        
        // creating component list
        ArrayList txComponents = new ArrayList();
        txComponents.add(dspFactory);
        txComponents.add(mixerFactory);
        txComponents.add(generatorFactory);

        // define pipes
        PipeFactory p1 = new PipeFactory();
        p1.setInlet(null);
        p1.setOutlet("mixer");

        PipeFactory p2 = new PipeFactory();
        p2.setInlet("dtmf.gen");
        p2.setOutlet("mixer");

        PipeFactory p3 = new PipeFactory();
        p3.setInlet("mixer");
        p3.setOutlet("dsp");

        PipeFactory p4 = new PipeFactory();
        p4.setInlet("dsp");
        p4.setOutlet(null);
        
        ArrayList pipes = new ArrayList();
        pipes.add(p1);
        pipes.add(p2);
        pipes.add(p3);
        pipes.add(p4);
        
        ChannelFactory txChannelFactory = new ChannelFactory();
        txChannelFactory.start();
        
        txChannelFactory.setComponents(txComponents);
        txChannelFactory.setPipes(pipes);

        //RX
        // creating component list
        ArrayList rxComponents = new ArrayList();
        rxComponents.add(dspFactory);
        rxComponents.add(splitterFactory);
        rxComponents.add(detectorFactory);

        // define pipes
        PipeFactory p5 = new PipeFactory();
        p5.setInlet(null);
        p5.setOutlet("dsp");

        PipeFactory p6 = new PipeFactory();
        p6.setInlet("dsp");
        p6.setOutlet("splitter");

        PipeFactory p7 = new PipeFactory();
        p7.setInlet("splitter");
        p7.setOutlet("dtmf.det");

        PipeFactory p8 = new PipeFactory();
        p8.setInlet("splitter");
        p8.setOutlet(null);
        
        ArrayList rxpipes = new ArrayList();
        rxpipes.add(p5);
        rxpipes.add(p6);
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
        noiseGeneratorFactory = new AudioNoiseGeneratorFactory();
        HashMap sources = new HashMap();
        sources.put("audio", noiseGeneratorFactory);
        
        ivrEndpoint1 = new EndpointFactoryImpl("/ivr/1");
        ivrEndpoint1.setTimer(timer);
        ivrEndpoint1.setConnectionFactory(connectionFactory);
        ivrEndpoint1.setRtpFactory(rtpFactory1);
        ivrEndpoint1.setSourceFactory(sources);
        ivrEndpoint1.start();
        
        ivrEndpoint2 = new EndpointFactoryImpl("/ivr/1");
        ivrEndpoint2.setTimer(timer);
        ivrEndpoint2.setConnectionFactory(connectionFactory);
        ivrEndpoint2.setRtpFactory(rtpFactory2);
        ivrEndpoint2.setSourceFactory(sources);
        ivrEndpoint2.start();
*/        
    }
    /**
     * Test of getSink method, of class Bridge.
     */
    @Test
    public void testSimpleTransmission() throws Exception {
/*        Connection connection1 = ivrEndpoint1.createConnection();
        connection1.setMode(ConnectionMode.SEND_RECV);
        Connection connection2 = ivrEndpoint2.createConnection();
        connection2.setMode(ConnectionMode.SEND_RECV);

        connection1.setRemoteDescriptor(connection2.getLocalDescriptor());
        connection2.setRemoteDescriptor(connection1.getLocalDescriptor());
        
        DtmfGenerator gen = (DtmfGenerator) connection1.getComponent(MediaType.AUDIO,DtmfGenerator.class);
        DtmfDetector det = (DtmfDetector) connection2.getComponent(MediaType.AUDIO,DtmfDetector.class);
        det.start();
        det.addListener(this);
        gen.setDigit("1");
        gen.setToneDuration(100);
        gen.start();
        
        Thread.currentThread().sleep(5000);
        assertEquals(1, digit);
        
        ivrEndpoint1.deleteAllConnections();
        ivrEndpoint2.deleteAllConnections();
 */ 
    }

    public void update(NotifyEvent event) {
        digit = event.getEventID();
    }



}