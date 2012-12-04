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

package org.mobicents.media.server.impl.dsp;

import java.io.IOException;
import java.util.ArrayList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mobicents.media.Buffer;
import org.mobicents.media.Format;
import org.mobicents.media.Server;
import org.mobicents.media.format.AudioFormat;
import org.mobicents.media.server.impl.AbstractSink;
import org.mobicents.media.server.impl.AbstractSource;
import org.mobicents.media.server.impl.resource.Proxy;
import org.mobicents.media.server.impl.resource.test.TransmissionTester2;
import org.mobicents.media.server.spi.dsp.Codec;
import org.mobicents.media.server.spi.dsp.CodecFactory;
import org.mobicents.media.server.spi.rtp.AVProfile;

/**
 * Test for transcoding with different codecs.
 * Tester's generator is a sine wave signal, which is compressed and then 
 * decompressed back. Spectral analyzer checks the returned signal.
 * 
 * @author Oleg Kulikov
 */
public class ProcessorTest {

    private final static AudioFormat PCMA = new AudioFormat(AudioFormat.ALAW, 8000, 8, 1);
    private final static AudioFormat PCMU = new AudioFormat(AudioFormat.ULAW, 8000, 8, 1);
    private final static AudioFormat G729 = new AudioFormat(AudioFormat.G729, 8000, 8, 1);
    private final static AudioFormat LINEAR_AUDIO = new AudioFormat(
            AudioFormat.LINEAR, 8000, 16, 1,
            AudioFormat.LITTLE_ENDIAN, AudioFormat.SIGNED);
    private final static AudioFormat GSM = new AudioFormat(AudioFormat.GSM, 8000, 8, 1);
    
    private Server server;
    
    private Processor dsp1;
    private Processor dsp2;
    
    private CodecFactory pcmaEncoderFactory = new org.mobicents.media.server.impl.dsp.audio.g711.alaw.EncoderFactory();
    private CodecFactory pcmaDecoderFactory = new org.mobicents.media.server.impl.dsp.audio.g711.alaw.DecoderFactory();
    private CodecFactory pcmuEncoderFactory = new org.mobicents.media.server.impl.dsp.audio.g711.ulaw.EncoderFactory();
    private CodecFactory pcmuDecoderFactory = new org.mobicents.media.server.impl.dsp.audio.g711.ulaw.DecoderFactory();
    private CodecFactory gsmEncoderFactory = new org.mobicents.media.server.impl.dsp.audio.gsm.EncoderFactory();
    private CodecFactory gsmDecoderFactory = new org.mobicents.media.server.impl.dsp.audio.gsm.DecoderFactory();
    private CodecFactory speexEncoderFactory = new org.mobicents.media.server.impl.dsp.audio.speex.EncoderFactory();
    private CodecFactory speexDecoderFactory = new org.mobicents.media.server.impl.dsp.audio.speex.DecoderFactory();
    private CodecFactory g729EncoderFactory = new org.mobicents.media.server.impl.dsp.audio.g729.EncoderFactory();
    private CodecFactory g729DecoderFactory = new org.mobicents.media.server.impl.dsp.audio.g729.DecoderFactory();
    
    private DspFactory dspFactory = new DspFactory();
    private TransmissionTester2 tester;
    private Proxy proxy;

    public ProcessorTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        server = new Server();
        server.start();
        tester = new TransmissionTester2();
        
        proxy = new Proxy("test");
        
        ArrayList<CodecFactory> codecFactories = new ArrayList();
        codecFactories.add(pcmaEncoderFactory);
        codecFactories.add(pcmaDecoderFactory);
        codecFactories.add(pcmuEncoderFactory);
        codecFactories.add(pcmuDecoderFactory);
        codecFactories.add(gsmEncoderFactory);
        codecFactories.add(gsmDecoderFactory);
        codecFactories.add(speexEncoderFactory);
        codecFactories.add(speexDecoderFactory);
        codecFactories.add(g729EncoderFactory);
        codecFactories.add(g729DecoderFactory);

        dspFactory.setName("test");
        dspFactory.setCodecFactories(codecFactories);
        
        dsp1 = (Processor) dspFactory.newInstance(null);
        dsp2 = (Processor) dspFactory.newInstance(null);
                
        dsp1.getInput().addListener(tester);
        dsp1.getOutput().addListener(tester);
        
        dsp2.getInput().addListener(tester);
        dsp2.getOutput().addListener(tester);
        
        proxy.getInput().addListener(tester);
        proxy.getOutput().addListener(tester);
        
    }

    @After
    public void tearDown() {
        server.stop();
    }

    @Test    
    public void testInputFormats1() {
        //test#1. processor is not connected.  
        //expected input format ANY
        Format[] supported = dsp1.getInput().getFormats();
        assertEquals(1, supported.length);
        assertEquals(Format.ANY, supported[0]);        
    }

    @Test    
    public void testInputFormats2() {
        //test#2. processor is connected to component using format ANY
        //expected input format is ANY too.
        TestSink sink = new TestSink(new Format[]{Format.ANY});
        dsp1.getOutput().connect(sink);
        Format[] supported = dsp1.getInput().getFormats();
        assertEquals(1, supported.length);
        assertEquals(Format.ANY, supported[0]);
        
        dsp1.getOutput().disconnect(sink);
    }
    
    @Test    
    public void testInputFormats3() {
        //test#3 processor is connected to component using concrete set of formats
        TestSink sink = new TestSink(new Format[]{Codec.LINEAR_AUDIO});
        dsp1.getOutput().connect(sink);
        Format[] supported = dsp1.getInput().getFormats();
        
        assertEquals(6, supported.length);
        assertTrue(contains(supported, AVProfile.PCMA));
        assertTrue(contains(supported, AVProfile.PCMU));
        assertTrue(contains(supported, AVProfile.SPEEX));
        assertTrue(contains(supported, AVProfile.G729));
        assertTrue(contains(supported, AVProfile.GSM));
        assertTrue(contains(supported, sink.getFormats()[0]));
                
        dsp1.getOutput().disconnect(sink);
        
    }
    
    @Test    
    public void testOutputFormats() {
        //test#1. processor is not connected.  
        //expected output format ANY
        Format[] supported = dsp1.getOutput().getFormats();
        assertEquals(1, supported.length);
        assertEquals(Format.ANY, supported[0]);
    }

    @Test    
    public void testOutputFormats2() {
        //test#2. processor is connected to component using format ANY
        //expected output format is ANY too.
        TestSource source = new TestSource(new Format[]{Format.ANY});
        dsp1.getInput().connect(source);
        Format[] supported = dsp1.getOutput().getFormats();
        assertEquals(1, supported.length);
        assertEquals(Format.ANY, supported[0]);
        
        dsp1.getInput().disconnect(source);
    }
    
    @Test    
    public void testOutputFormats3() {
        //test#3 processor is connected to component using concrete set of formats
        TestSource source = new TestSource(new Format[]{Codec.LINEAR_AUDIO});
        dsp1.getInput().connect(source);
        Format[] supported = dsp1.getOutput().getFormats();
        
        assertEquals(6, supported.length);
        assertTrue(contains(supported, AVProfile.PCMA));
        assertTrue(contains(supported, AVProfile.PCMU));
        assertTrue(contains(supported, AVProfile.SPEEX));
        assertTrue(contains(supported, AVProfile.G729));
        assertTrue(contains(supported, AVProfile.GSM));
        assertTrue(contains(supported, source.getFormats()[0]));
                
        dsp1.getInput().disconnect(source);
    }
    
    @Test
    public void testCodecSelection() {
        //this test should not throws any exception
        TestSource source = new TestSource(new Format[]{Codec.LINEAR_AUDIO});
        TestSink sink = new TestSink(new Format[]{Codec.PCMA});
        
        dsp1.getInput().connect(source);
        dsp1.getOutput().connect(sink);
        
//        System.out.println("Codec=" + ((Processor)dsp1).getActiveCodec());
    }
    
    private boolean contains(Format[] list, Format item) {
        for (Format f:  list) {
            if (f.matches(item)) {
                return true;
            }
        }
        return false;
    }

    private void testTranscoding(Format fmt) {
        proxy.setFormat(new Format[] {fmt});
//        proxy.start();        

        tester.connect(dsp1.getInput());
        dsp1.getOutput().connect(proxy.getInput());
        dsp2.getInput().connect(proxy.getOutput());
        tester.connect(dsp2.getOutput());
        
        dsp1.start();
        dsp2.start();
        
        tester.start();
        proxy.stop();
        dsp1.stop();
        dsp2.stop();
        
//        printStat();
        assertTrue(tester.getMessage(), tester.isPassed());
    }
    @Test
    public void testLinear() throws Exception {
        testTranscoding(LINEAR_AUDIO);
    }

    @Test
    public void testPCMA() throws Exception {
        testTranscoding(PCMA);
    }
    
    @Test
    public void testPCMU() throws Exception {
        testTranscoding(PCMU);
    }

    @Test
    public void testGSM() throws Exception {
        testTranscoding(GSM);
    }

    @Test
    public void testSpeex() throws Exception {
        testTranscoding(AVProfile.SPEEX);
    }

    @Test
    public void testG729() throws Exception {
        testTranscoding(G729);
    }
    
    private void printStat() {
        System.out.println("Generator :" + tester.getGenerator().getPacketsTransmitted());
        System.out.println("Dsp1 input :" + dsp1.getInput().getPacketsReceived());
        System.out.println("Dsp1 output :" + dsp1.getOutput().getPacketsTransmitted());
        System.out.println("Proxy input :" + proxy.getInput().getPacketsReceived());
        System.out.println("Proxy output :" + proxy.getOutput().getPacketsTransmitted());
        System.out.println("Dsp2 input :" + dsp2.getInput().getPacketsReceived());
        System.out.println("Dsp2 output :" + dsp2.getOutput().getPacketsTransmitted());
        System.out.println("Detector :" + tester.getDetector().getPacketsReceived());
    }
    
    private class TestSource extends AbstractSource {

        private Format[] formats;
        
        public TestSource(Format[] formats) {
            super("test");
            this.formats = formats;
        }
        
        @Override
        public void evolve(Buffer buffer, long timestamp) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Format[] getFormats() {
            return formats;
        }
        
    }
    
    private class TestSink extends AbstractSink {

        private Format[] formats;
        
        public TestSink(Format[] formats) {
            super("test");
            this.formats = formats;
        }
        
        @Override
        public void onMediaTransfer(Buffer buffer) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Format[] getFormats() {
            return formats;
        }
        
    }
}
