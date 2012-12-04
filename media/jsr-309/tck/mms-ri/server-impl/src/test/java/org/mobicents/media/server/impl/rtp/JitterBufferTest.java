package org.mobicents.media.server.impl.rtp;

import java.util.Collection;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mobicents.media.Buffer;
import org.mobicents.media.Frame;
import org.mobicents.media.server.impl.rtp.clock.AudioClock;
import org.mobicents.media.server.impl.rtp.sdp.AVProfile;

/**
 * 
 * @author amit bhayani
 * 
 */
public class JitterBufferTest {

    private int period = 20;
    private int jitter = 40;
    
    private JitterBuffer jitterBuffer;
    private RtpClock clock;
    
    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        jitterBuffer = new JitterBuffer(jitter, period);
        clock = new AudioClock();
        jitterBuffer.setClock(clock);
        jitterBuffer.setFormat(AVProfile.PCMU);
    }

    @After
    public void tearDown() {
    }

    private RtpPacket createBuffer(int seq) {
        return new RtpPacket((byte)0, seq, seq * 160, 1, new byte[160]);
    }

    @Test
    public void testAccuracy() {
        jitterBuffer.write(createBuffer(1));
        
        RtpPacket p = null;
        p = jitterBuffer.read(0);
        assertEquals("Jitter Buffer not full yet", null, p);

        jitterBuffer.write(createBuffer(2));
        p = jitterBuffer.read(20);
        assertEquals("Jitter Buffer not full yet", null, p);

        jitterBuffer.write(createBuffer(3));
        p = jitterBuffer.read(40);
        assertEquals("Jitter Buffer not full yet", null, p);
        
        jitterBuffer.write(createBuffer(4));
        p = jitterBuffer.read(60);
        assertTrue("Jitter Buffer not full yet", p != null);
        
        jitterBuffer.write(createBuffer(5));
        p = jitterBuffer.read(80);
        assertTrue("Jitter Buffer should be full", p != null);        
    }
    
    private void check(RtpPacket packet, int seq) {
        assertTrue("Failed to match binary representation.", packet != null);
        assertTrue("Expected seq = " + seq, packet.getSeqNumber() == seq);
        assertTrue("Expected timestamp=" + (seq*160), packet.getTimestamp() == (seq*160));
    }
}
