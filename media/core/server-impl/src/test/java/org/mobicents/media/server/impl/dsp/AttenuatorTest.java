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
package org.mobicents.media.server.impl.dsp;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mobicents.media.Buffer;
import org.mobicents.media.Format;
import org.mobicents.media.Server;
import org.mobicents.media.format.AudioFormat;
import org.mobicents.media.server.impl.AbstractSink;
import org.mobicents.media.server.impl.resource.test.SineGenerator;
import org.mobicents.media.server.spi.NotificationListener;
import org.mobicents.media.server.spi.events.FailureEvent;
import org.mobicents.media.server.spi.events.NotifyEvent;

/**
 * 
 * @author amit bhayani
 *
 */
public class AttenuatorTest implements NotificationListener {

    private final static AudioFormat LINEAR_AUDIO = new AudioFormat(
            AudioFormat.LINEAR, 8000, 16, 1, AudioFormat.LITTLE_ENDIAN,
            AudioFormat.SIGNED);
    private final static short A = Short.MAX_VALUE;
    private final static int f = 50;
    private Server server;
    private SineGenerator sineGen;
    private Sink det;
    private AttenuatorImpl attenuator;
    private short[] data = new short[8000 * 15];
    private int len;
    private Semaphore semaphore = new Semaphore(0);
    private volatile boolean failed;
    private String message;

    public AttenuatorTest() {
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
        sineGen = new SineGenerator("test.sine");
        sineGen.setAmplitude(A);
        sineGen.setFrequency(f);

        attenuator = new AttenuatorImpl("test.attenuator");
        //-3 decible is half the signal strength
        attenuator.setVolume(-3);

        det = new Sink();

        sineGen.addListener(this);
        det.addListener(this);
    }

    @After
    public void tearDown() {
        server.stop();
    }

    @Test
    @SuppressWarnings("static-access")
    public void testGenerator() throws InterruptedException {
        sineGen.connect(attenuator.getInput());

        det.connect(attenuator.getOutput());

        det.start();
        sineGen.start();
        attenuator.start();

        semaphore.tryAcquire(10, TimeUnit.SECONDS);
        assertFalse(message, failed);

        sineGen.stop();
        det.stop();

        //this.print();

        assertTrue("Sine not recognized", check());
    }

    private boolean check() {
        boolean res = true;
        double dt = 1 / LINEAR_AUDIO.getSampleRate();
        short E = (short) (Short.MAX_VALUE / 100);
        for (int i = 0; i < len; i++) {
            short s = (short) (A * Math.sin(2 * Math.PI * f * i * dt));
            if (Math.abs((s * 0.5) - data[i]) > E) {
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
            return new Format[]{LINEAR_AUDIO};
        }

        public boolean isAcceptable(Format format) {
            return true;
        }
    }

    public void update(NotifyEvent event) {
        switch (event.getEventID()) {
            case NotifyEvent.START_FAILED:
            case NotifyEvent.TX_FAILED:
                failed = true;
                message = ((FailureEvent) event).getException().getMessage();
                semaphore.release();
                break;
        }
    }
}
