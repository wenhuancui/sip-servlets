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
package org.mobicents.media.server.impl.resource.mediaplayer.audio;

import java.net.URL;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
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
import org.mobicents.media.server.impl.resource.mediaplayer.audio.tts.VoicesCache;
import org.mobicents.media.server.spi.NotificationListener;
import org.mobicents.media.server.spi.events.FailureEvent;
import org.mobicents.media.server.spi.events.NotifyEvent;
import org.mobicents.media.server.spi.resource.TTSEngine;
import org.mobicents.media.server.spi.rtp.AVProfile;

/**
 *
 * @author kulikov
 */
public class AudioPlayerImplTest {

    private Server server;
    
    private Semaphore semaphore;
    private volatile boolean started = false;
    private volatile boolean failed = false;
    private volatile boolean end_of_media = false;
    private volatile boolean isFormatCorrect = true;
    private volatile boolean isSizeCorrect = true;
    private volatile boolean isCorrectTimestamp = true;
    private volatile boolean isSeqCorrect = true;
    private AudioPlayerImpl player;
    private TestSink sink;
    private final static AudioFormat LINEAR_AUDIO = new AudioFormat(AudioFormat.LINEAR, 8000, 16, 1,
            AudioFormat.LITTLE_ENDIAN, AudioFormat.SIGNED);

    public AudioPlayerImplTest() {
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
        started = false;
        failed = false;
        end_of_media = false;
        isFormatCorrect = true;
        isSizeCorrect = true;
        isCorrectTimestamp = false;
        isSeqCorrect = false;

        player = new AudioPlayerImpl("test", null, new VoicesCache());
        player.addListener(new PlayerListener());

        sink = new TestSink("test-sink");

        semaphore = new Semaphore(0);
    }

    @After
    public void tearDown() {
        server.stop();
        player.disconnect(sink);
    }

    private void testPlayback(String file, Format fmt, int size) throws Exception {
        sink.setSupportedFormat(fmt);
        sink.setSize(size);
        player.connect(sink);

        URL url = AudioPlayerImplTest.class.getClassLoader().getResource(file);
        player.setURL(url.toExternalForm());

        sink.start();
        player.start();

        semaphore.tryAcquire(60, TimeUnit.SECONDS);

        assertEquals(false, failed);
        assertEquals(true, started);
        assertEquals(true, end_of_media);
        assertEquals(true, isFormatCorrect);
        assertEquals(true, isSizeCorrect);
        assertEquals(true, isSeqCorrect);
    }

    private void testSpeek(String text, Format fmt, int size) throws Exception {
        sink.setSupportedFormat(fmt);
        sink.setSize(size);
        player.connect(sink);

        ((TTSEngine) player).setText(text);

        sink.start();
        player.start();

        semaphore.tryAcquire(60, TimeUnit.SECONDS);

        assertEquals(false, failed);
        assertEquals(true, started);
        assertEquals(true, end_of_media);
        assertEquals(true, isFormatCorrect);
        assertEquals(true, isSizeCorrect);
        assertEquals(true, isSeqCorrect);
    }
    
    @Test
    public void test_WAV_8000_MONO_ALAW() throws Exception {
        testPlayback("org/mobicents/media/server/impl/addf8-Alaw-GW.wav", AVProfile.PCMA, 160);
    }

    @Test
    public void test_WAV_8000_MONO_ALAW_Decompression() throws Exception {
        testPlayback("org/mobicents/media/server/impl/addf8-Alaw-GW.wav", LINEAR_AUDIO, 320);
    }
    @Test
    public void test_WAV_8000_MONO_ULAW() throws Exception {
        testPlayback("org/mobicents/media/server/impl/8kulaw.wav", AVProfile.PCMU, 160);
    }

    @Test
    public void test_WAV_8000_MONO_ULAW_Decompression() throws Exception {
        testPlayback("org/mobicents/media/server/impl/8kulaw.wav", LINEAR_AUDIO, 320);
    }
    @Test
    public void test_Wav_L16_8000() throws Exception {
        testPlayback("org/mobicents/media/server/impl/dtmf-0.wav", LINEAR_AUDIO, 320);
    }

    @Test
    public void test_Wav_L16_8000_Compression_ULaw() throws Exception {
        testPlayback("org/mobicents/media/server/impl/dtmf-0.wav", AVProfile.PCMU, 160);
    }

    @Test
    public void test_Wav_L16_8000_Compression_ALaw() throws Exception {
        testPlayback("org/mobicents/media/server/impl/dtmf-0.wav", AVProfile.PCMA, 160);
    }

    @Test
    public void test_Wav_L16_8000_Compression_GSM() throws Exception {
        testPlayback("org/mobicents/media/server/impl/dtmf-0.wav", AVProfile.GSM, 33);
    }
    @Test
    public void test_WAV_L16_44100_MONO() throws Exception {
        testPlayback("org/mobicents/media/server/impl/gwn44m.wav", AVProfile.L16_MONO, 1764);
    }

    @Test
    public void test_WAV_L16_44100_STEREO() throws Exception {
        testPlayback("org/mobicents/media/server/impl/gwn44s.wav", AVProfile.L16_STEREO, 1764 * 2);
    }

    @Test
    public void test_GSM() throws Exception {
        testPlayback("org/mobicents/media/server/impl/cnfannouncement.gsm", AVProfile.GSM, 33);
    }

    @Test
    public void test_GSM_Decompression() throws Exception {
        testPlayback("org/mobicents/media/server/impl/cnfannouncement.gsm", LINEAR_AUDIO, 320);
    }

    @Test
    public void test_TTS() throws Exception {
        testPlayback("org/mobicents/media/server/impl/tts.txt", LINEAR_AUDIO, 320);
    }

    @Test
    public void test_TTS_Speek() throws Exception {
        for (int i = 0; i < 10; i++) {
            testSpeek("Hello world", LINEAR_AUDIO, 320);
        }
    }

    private class TestSink extends AbstractSink {

        private long lastTick = 0;
        private long lastSeqNo = 0;
        private Format fmt;
        private int size;

        private TestSink(String name) {
            super(name);
        }

        public void setSupportedFormat(Format fmt) {
            this.fmt = fmt;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public Format[] getFormats() {
            return new Format[]{fmt};
        }

        @Override
        public void onMediaTransfer(Buffer buffer) {
            if (!buffer.isEOM()) {
                isFormatCorrect &= buffer.getFormat().matches(fmt);
                isSizeCorrect = (buffer.getLength() == size);
                lastTick = buffer.getTimeStamp();

                if (lastSeqNo > 0) {
                    isSeqCorrect = (buffer.getSequenceNumber() - lastSeqNo) == 1;
                }
                lastSeqNo = buffer.getSequenceNumber();
            }
        }
    }

    private class PlayerListener implements NotificationListener {

        public void update(NotifyEvent event) {
            switch (event.getEventID()) {
                case AudioPlayerEvent.STARTED:
                    started = true;
                    break;
                case AudioPlayerEvent.COMPLETED:
                    end_of_media = true;
                    semaphore.release();
                    break;
                case AudioPlayerEvent.START_FAILED:
                    failed = true;
                    ((FailureEvent) event).getException().printStackTrace();
                    semaphore.release();
                    break;
            }
        }
    }
}