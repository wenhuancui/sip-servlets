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

package org.mobicents.media.server.impl.resource.prelay;


import org.mobicents.media.server.impl.rtp.*;
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


import org.mobicents.media.server.impl.dsp.DspFactory;
import org.mobicents.media.server.impl.dsp.Processor;
import org.mobicents.media.server.impl.dsp.audio.g711.ulaw.DecoderFactory;
import org.mobicents.media.server.impl.dsp.audio.g711.ulaw.EncoderFactory;
import org.mobicents.media.server.impl.resource.Proxy;
import org.mobicents.media.server.impl.resource.cnf.AudioMixer;
import org.mobicents.media.server.impl.resource.cnf.Splitter;
import org.mobicents.media.server.impl.resource.dtmf.DetectorImpl;
import org.mobicents.media.server.impl.resource.dtmf.GeneratorImpl;
import org.mobicents.media.server.impl.resource.test.TransmissionTester;
import org.mobicents.media.server.impl.resource.test.TransmissionTester2;
import org.mobicents.media.server.impl.rtp.clock.AudioClock;
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
public class DtmfTest1 implements NotificationListener {

    private final static int HEART_BEAT = 20;
    private final static int jitter = 60;
    private RtpFactory rtpFactory1 = null;
    private RtpFactory rtpFactory2 = null;
    private RtpSocketImpl serverSocket;
    private RtpSocketImpl clientSocket;
    private InetAddress localAddress;
    private int localPort1 = 9201;
    private int localPort2 = 9202;
    
    private TransmissionTester tester;
    private TransmissionTester2 tester2;
    
    private GeneratorImpl generator;
    private DetectorImpl detector;
        
    private RtpClock clock = new AudioClock();
    private int tone = 0;
    
    private DspFactory dspFactory;
    
    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
/*        timer = new TimerImpl();
        timer.setHeartBeat(HEART_BEAT);
        timer.start();
        
        ArrayList<CodecFactory> codecs1 = new ArrayList();
        codecs1.add(new EncoderFactory());
        codecs1.add(new DecoderFactory());
        
        dspFactory = new DspFactory();
        dspFactory.setName("dsp");
        
        dspFactory.setCodecFactories(codecs1);
        
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

        rtpFactory2 = new RtpFactory();
        rtpFactory2.setJitter(jitter);
        rtpFactory2.setBindAddress("localhost");
        rtpFactory2.setTimer(timer);
        rtpFactory2.setAVProfile(profile);
        rtpFactory2.setCodecs(factories);

        localAddress = InetAddress.getByName("localhost"); 
 */            
    }

    @After
    public void tearDown() {
        rtpFactory1.stop();
        rtpFactory2.stop();
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
/*        generator.setToneDuration(1000);
        serverSocket = rtpFactory1.getRTPSocket(MediaType.AUDIO);
        serverSocket.setFormat(0, AVProfile.PCMU);
        if (rfc2833) {
            serverSocket.setDtmfPayload(101);
        }
        serverSocket.bind();
        
        clientSocket = rtpFactory2.getRTPSocket(MediaType.AUDIO);
        clientSocket.setJitter(0);
        clientSocket.setFormat(0, AVProfile.PCMU);
        
        if (rfc2833) {
            clientSocket.setDtmfPayload(101);
        }
        clientSocket.bind();
        
        serverSocket.setPeer(localAddress, clientSocket.getLocalPort());
        clientSocket.setPeer(localAddress, serverSocket.getLocalPort());

        Proxy proxy = new Proxy("proxy");
        Processor dsp = (Processor) dspFactory.newInstance(null);
        Splitter splitter = new Splitter("splitter");

        proxy.getOutput().connect(dsp.getInput());
        dsp.getOutput().connect(splitter);
        
        
        Proxy proxy1 = new Proxy("internal");

        AudioMixer mixer = new AudioMixer("mixer", timer);
        
        splitter.connect(proxy1);
               
        proxy1.connect(mixer);
        mixer.getOutput().connect(detector);
        
        proxy1.start();
        mixer.start();
        
        serverSocket.getReceiveStream().connect(proxy.getInput());
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
 */ 
    }
    
    public void update(NotifyEvent event) {
        tone = event.getEventID();
    }
}
