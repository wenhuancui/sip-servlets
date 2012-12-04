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

package org.mobicents.media.server.impl.resource.test;

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
import org.mobicents.media.server.spi.NotificationListener;
import org.mobicents.media.server.spi.events.FailureEvent;
import org.mobicents.media.server.spi.events.NotifyEvent;

/**
 *
 * @author kulikov
 */
public class SineGeneratorTest implements NotificationListener {

    private final static AudioFormat LINEAR_AUDIO = new AudioFormat(
            AudioFormat.LINEAR, 8000, 16, 1,
            AudioFormat.LITTLE_ENDIAN,
            AudioFormat.SIGNED);
    
    private final static short A = Short.MAX_VALUE;
    private final static int f = 30;
    
    private SineGenerator gen;
    private Sink det;
    
    private short[] data = new short[8000 * 15];
    private int len;

    private Semaphore semaphore = new Semaphore(0);
    private volatile boolean failed;
    private String message;

    private Server server;
    
    public SineGeneratorTest() {
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
        gen = new SineGenerator("test.sine");
        gen.setAmplitude(A);
        gen.setFrequency(f);
        
        det = new Sink();
        
        gen.addListener(this);
        det.addListener(this);
    }

    @After
    public void tearDown() {
        server.stop();
    }

    /**
     * Test of setAmplitude method, of class SineGenerator.
     */
    @Test
    @SuppressWarnings("static-access")
    public void testGenerator() throws InterruptedException {
        det.connect(gen);
        det.start();
        gen.start();
        
        semaphore.tryAcquire(10, TimeUnit.SECONDS);
        assertFalse(message,failed);
        
        gen.stop();
        det.stop();
        
        assertTrue("Sine not recognized", check());
    }

    private boolean check() {
        boolean res = true;
        double dt = 1/LINEAR_AUDIO.getSampleRate();
        short E = (short)(Short.MAX_VALUE / 100);
        for (int i = 0; i < len; i++) {
            short s = (short) (A* Math.sin(2 * Math.PI * f * i * dt));
            if (Math.abs(s - data[i]) > E) {
                return false;
            }
        }
        return res;
    }
    
    private class Sink extends AbstractSink {

        public Sink() {
            super("test.sink");
        }

        @Override
        public void onMediaTransfer(Buffer buffer) {
                byte[] buff = buffer.getData();
                for (int i = 0; i < buff.length - 1; i += 2) {
                    short s = (short) ((buff[i] & 0xff) | (buff[i + 1] << 8));
                    data[len++] = s;
                }
        }

        public Format[] getFormats() {
            return new Format[] {LINEAR_AUDIO};
        }

        public boolean isAcceptable(Format format) {
            return true;
        }
    }

    public void update(NotifyEvent event) {
        switch (event.getEventID()) {
            case NotifyEvent.START_FAILED :
            case NotifyEvent.TX_FAILED :
                failed = true;
                message = ((FailureEvent) event).getException().getMessage();
                semaphore.release();
                break;
        }
    }
}