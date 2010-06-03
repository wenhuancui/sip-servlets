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
import org.mobicents.media.server.impl.resource.dtmf.DetectorFactory;
import org.mobicents.media.server.impl.resource.dtmf.DetectorImpl;
import org.mobicents.media.server.impl.resource.dtmf.GeneratorFactory;
import org.mobicents.media.server.impl.resource.dtmf.GeneratorImpl;
import org.mobicents.media.server.impl.rtp.RtpClock;
import org.mobicents.media.server.impl.rtp.RtpFactory;
import org.mobicents.media.server.impl.rtp.clock.AudioClock;
import org.mobicents.media.server.impl.rtp.sdp.AVProfile;
import org.mobicents.media.server.resource.ChannelFactory;
import org.mobicents.media.server.spi.Connection;
import org.mobicents.media.server.spi.ConnectionMode;
import org.mobicents.media.server.spi.NotificationListener;
import org.mobicents.media.server.spi.clock.Timer;
import org.mobicents.media.server.spi.dsp.Codec;
import org.mobicents.media.server.spi.events.NotifyEvent;

/**
 *
 * @author kulikov
 */
public class RtpConnectionRFC2833DtmfTest implements NotificationListener {

    private Timer timer;
    private EndpointImpl sender;
    private EndpointImpl receiver;
    private int localPort1 = 9201;
    private int localPort2 = 9202;
    private ChannelFactory channelFactory;
    private RtpFactory rtpFactory1,  rtpFactory2;

    private GeneratorFactory sourceFactory;
    private DetectorFactory sinkFactory;
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
        timer.start();
        
        
        sourceFactory = new GeneratorFactory();
        sourceFactory.setName("dtmf-gen");
        
        sinkFactory = new DetectorFactory();
        sinkFactory.setName("dtmf-det");
        
        Hashtable<Integer, Format> rtpMap = new Hashtable();
        rtpMap.put(1, Codec.LINEAR_AUDIO);
        rtpMap.put(101, AVProfile.DTMF);
        
        AVProfile profile = new AVProfile();
        profile.setProfile(rtpMap);
        
        rtpFactory1 = new RtpFactory();
        rtpFactory1.setBindAddress("127.0.0.1");
        rtpFactory1.setTimer(timer);
        rtpFactory1.setAVProfile(profile);
        rtpFactory1.setPeriod(20);
        rtpFactory1.start();

        rtpFactory2 = new RtpFactory();
        rtpFactory2.setBindAddress("127.0.0.1");
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
        DetectorImpl det = (DetectorImpl)receiver.getComponent("dtmf-det");
        det.addListener(this);
        det.start();
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
        Connection rxConnection = receiver.createConnection();
        rxConnection.setMode(ConnectionMode.RECV_ONLY);
        Connection txConnection = sender.createConnection();
        txConnection.setMode(ConnectionMode.SEND_ONLY);

        txConnection.setRemoteDescriptor(rxConnection.getLocalDescriptor());
        String sdp = txConnection.getLocalDescriptor();
        rxConnection.setRemoteDescriptor(sdp);

        GeneratorImpl gen = (GeneratorImpl)sender.getComponent("dtmf-gen");
        
        gen.setDigit("1");
        gen.setToneDuration(1000);
        gen.start();
        
        Thread.currentThread().sleep(2000);
        assertEquals(1, tone);
    }

    public void update(NotifyEvent event) {
        tone = event.getEventID();
    }

}