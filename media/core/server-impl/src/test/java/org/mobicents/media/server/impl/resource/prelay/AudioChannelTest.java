/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mobicents.media.server.impl.resource.prelay;

import java.io.IOException;
import java.util.ArrayList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mobicents.media.Buffer;
import org.mobicents.media.Format;
import org.mobicents.media.Server;
import org.mobicents.media.server.impl.AbstractSink;
import org.mobicents.media.server.impl.AbstractSource;
import org.mobicents.media.server.impl.dsp.DspFactory;
import org.mobicents.media.server.impl.dsp.audio.g711.alaw.DecoderFactory;
import org.mobicents.media.server.impl.dsp.audio.g711.alaw.EncoderFactory;
import org.mobicents.media.server.spi.dsp.Codec;
import org.mobicents.media.server.spi.dsp.CodecFactory;

/**
 *
 * @author kulikov
 */
public class AudioChannelTest {

    private Server server;
    private AudioChannel channel;
    
    public AudioChannelTest() {
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
        // preparing g711: ALaw encoder, decoder
        EncoderFactory encoderFactory = new EncoderFactory();
        DecoderFactory decoderFactory = new DecoderFactory();

        // group codecs into list
        ArrayList<CodecFactory> codecs = new ArrayList();
        codecs.add(encoderFactory);
        codecs.add(decoderFactory);
        
        DspFactory dspFactory = new DspFactory();
        dspFactory.setName("dsp");
        
        dspFactory.setCodecFactories(codecs);
        
        channel = new AudioChannel(dspFactory);
    }

    @After
    public void tearDown() {
        server.stop();
    }


    /**
     * Test of stop method, of class AudioChannel.
     */
    @Test
    public void testStop() throws Exception {
        TestSource source = new TestSource("source");
        
        TestSink sink = new TestSink("sink");
        
        channel.getInput().connect(source);
        channel.getOutput().connect(sink);
                
        source.start();
        sink.start();
        
        Thread.currentThread().sleep(1000);
        
        source.stop();
        sink.stop();
        
        System.out.println("STOPPED");
        Thread.currentThread().sleep(1000);
    }

    public class TestSource extends AbstractSource {

        public TestSource(String name) {
            super(name);
        }
        
        @Override
        public void evolve(Buffer buffer, long timestamp) {
            buffer.setData(new byte[320]);
            buffer.setLength(320);
            buffer.setDuration(20);
        }

        public Format[] getFormats() {
            return new Format[] {Codec.LINEAR_AUDIO};
        }
        
    }
    
    public class TestSink extends AbstractSink {

        public TestSink(String name) {
            super(name);
        }
        
        @Override
        public void onMediaTransfer(Buffer buffer) throws IOException {
            System.out.println("Receive " + buffer);
        }

        public Format[] getFormats() {
            return new Format[] {Codec.LINEAR_AUDIO};
        }
        
    }
}