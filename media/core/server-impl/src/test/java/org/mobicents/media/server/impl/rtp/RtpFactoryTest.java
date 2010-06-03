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
package org.mobicents.media.server.impl.rtp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mobicents.media.Format;
import org.mobicents.media.format.AudioFormat;
import org.mobicents.media.server.impl.clock.TimerImpl;
import org.mobicents.media.server.impl.rtp.sdp.AVProfile;
import org.mobicents.media.server.spi.MediaType;
import org.mobicents.media.server.spi.clock.Timer;

/**
 * 
 * @author amit bhayani
 * 
 */
public class RtpFactoryTest {

    private static final int HEART_BEAT = 20;
    private static final AudioFormat PCMA = new AudioFormat(AudioFormat.ALAW, 8000, 8, 1);
    private static final AudioFormat PCMU = new AudioFormat(AudioFormat.ULAW, 8000, 8, 1);
    private static Map<Integer, Format> formatMap = new HashMap<Integer, Format>();
    

    static {
        formatMap.put(0, PCMU);
        formatMap.put(8, PCMA);
    }
    private RtpFactory factory = null;

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        Hashtable payloads = new Hashtable();
        payloads.put(8, AVProfile.PCMA);
        payloads.put(0, AVProfile.PCMU);
        
        AVProfile profile = new AVProfile();
        profile.setProfile(payloads);
        factory = new RtpFactory();
        factory.setAVProfile(profile);

        Timer timer = new TimerImpl();
        timer.setHeartBeat(HEART_BEAT);

        timer.start();
        
        factory.setTimer(timer);
        factory.setJitter(80);
        factory.setBindAddress("127.0.0.1");
        factory.start();
    }

    @After
    public void tearDown() {
        // Dont close the Factory as it will stop the RtpSocket.readerThread and
        // RtpSocketTest will be screwed
        factory.stop();
    }

    @Test
    public void getRTPSocketTest() throws Exception {
        RtpSocket rtpSocket = factory.getRTPSocket(MediaType.AUDIO);
        rtpSocket.bind();
        boolean started = rtpSocket.getLocalPort() > 0;
        assertTrue("Socket is null", rtpSocket != null);
        assertTrue("Socket is unknown", started);
    }
}
