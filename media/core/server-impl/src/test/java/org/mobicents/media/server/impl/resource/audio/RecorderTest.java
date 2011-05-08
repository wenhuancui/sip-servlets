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

package org.mobicents.media.server.impl.resource.audio;

import org.mobicents.media.server.impl.resource.mediaplayer.audio.AudioPlayerImpl;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.net.URL;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mobicents.media.Server;
import org.mobicents.media.server.spi.NotificationListener;
import org.mobicents.media.server.spi.events.FailureEvent;
import org.mobicents.media.server.spi.events.NotifyEvent;

/**
 * 
 * @author amit bhayani
 * 
 */
public class RecorderTest {

    private Server server;
    
    private Semaphore semaphore;
    private AudioPlayerImpl player;
    private RecorderImpl recorder;
    private boolean completed = false;
    private boolean stopped = false;
    private boolean failed = false;

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
        
        failed = false;
        stopped = false;
        player = new AudioPlayerImpl("test", null, null);
        player.addListener(new PlayerListener());
        
        recorder = new RecorderImpl("test");
        recorder.addListener(new RecorderListener());

        semaphore = new Semaphore(0);
    }

    @After
    public void tearDown() {
        server.stop();
    }
    
    private void testRecording(String src, String dst) throws Exception {
        URL url = RecorderTest.class.getClassLoader().getResource(src);
        String path = url.getPath();
        
        String recordDir = path.substring(0, path.lastIndexOf('/'));
        recorder.setRecordDir(recordDir);
        recorder.setRecordFile(dst);
        
        player.connect(recorder);
        player.setURL(url.toExternalForm());
        
        recorder.start();
        player.start();
        
        semaphore.tryAcquire(60, TimeUnit.SECONDS);        
        
        assertTrue("It is expected that audio player finishes playback", completed);
        completed = false;
        
        recorder.stop();
        player.stop();
        
        //give a bit of time to complete recording
        semaphore.tryAcquire(5, TimeUnit.SECONDS);
        assertTrue("Player have to send STOPPED event", stopped);
        assertFalse("Recorder failed", failed);
        assertTrue("Recorder have to send COMPLETED event", completed);
    }
    
    @Test
    public void test_Wav_L16_8000() throws Exception {
        testRecording("org/mobicents/media/server/impl/fox-full.wav", "recorder-test/fox-full-recorded.wav");
    }

    @Test
    public void test_8000_MONO_ALAW() throws Exception {
        testRecording("org/mobicents/media/server/impl/addf8-Alaw-GW.wav", "addf8-Alaw-GW-recorded.wav");
    }

//    @Test
    public void test_8000_MONO_ULAW() throws Exception {
        testRecording("org/mobicents/media/server/impl/8kulaw.wav", "8kulaw-recorded.wav");
    }

//    @Test
    public void test_SPEEX() throws Exception {
        testRecording("org/mobicents/media/server/impl/sin8m.spx", "speex-recording.spx");
    }

    private class PlayerListener implements NotificationListener {

        public void update(NotifyEvent event) {
            switch(event.getEventID()) {
                case NotifyEvent.COMPLETED :
                    completed = true;
                    semaphore.release();
                    break;
                case NotifyEvent.START_FAILED :
                case NotifyEvent.TX_FAILED :
                    failed = true;
                    semaphore.release();
                    break;
            }
        }
        
    }
    
    private class RecorderListener implements NotificationListener {

        public void update(NotifyEvent event) {
            switch (event.getEventID()) {
                case NotifyEvent.STOPPED:
                    stopped = true;
                    break;
                case NotifyEvent.START_FAILED :
                case NotifyEvent.RX_FAILED :
                    failed = true;
                    semaphore.release();
                    ((FailureEvent)event).getException().printStackTrace();
                    break;
                case NotifyEvent.COMPLETED :
                    completed = true;
                    semaphore.release();
                    break;
            }
        }
    }

}
