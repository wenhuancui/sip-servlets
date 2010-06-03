/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mobicents.media.server.impl.resource;

import org.mobicents.media.server.impl.*;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mobicents.media.Buffer;
import org.mobicents.media.Format;
import org.mobicents.media.server.impl.clock.TimerImpl;
import org.mobicents.media.server.impl.rtp.sdp.AVProfile;
import org.mobicents.media.server.spi.Timer;

/**
 *
 * @author kulikov
 */
public class ProxyConnectionsTest {

    private Timer timer = new TimerImpl();
    
    private TestSource src = new TestSource("source");
    private TestSink sink = new TestSink("sink");
    
    private Proxy proxy = new Proxy("test-proxy");
    
    public ProxyConnectionsTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        timer = new TimerImpl();
        src.setSyncSource(timer);
        timer.start();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of connect method, of class AbstractSink.
     */
    @Test
    public void testProxyNegotiation() {
        src.connect(proxy);
        sink.connect(proxy);
        
        boolean res = src.getFormat() != null && src.getFormat().matches(AVProfile.PCMA);
        assertTrue("Format mismatch",res);
    }


    @Test
    public void testProxyNegotiation2() {
        sink.connect(proxy);
        src.connect(proxy);
        
        boolean res = src.getFormat() != null && src.getFormat().matches(AVProfile.PCMA);
        assertTrue("Format mismatch",res);
    }

    @Test
    public void testProxyNegotiation3() {
        proxy.connect(src);
        proxy.connect(sink);
        
        boolean res = src.getFormat() != null && src.getFormat().matches(AVProfile.PCMA);
        assertTrue("Format mismatch",res);
    }

    @Test
    public void testProxyNegotiation4() {
        proxy.connect(sink);
        proxy.connect(src);
        
        boolean res = src.getFormat() != null && src.getFormat().matches(AVProfile.PCMA);
        assertTrue("Format mismatch",res);
    }
    
    @Test
    public void testDisconnect() {
        src.connect(proxy);
        sink.connect(proxy);
        
        src.disconnect(proxy);
        sink.disconnect(proxy);
        
        assertTrue("Format still assigned",src.getFormat() == null);
        assertTrue("Format still assigned",sink.getFormat() == null);
    }

    @Test
    public void testDisconnect1() {
        sink.connect(proxy);
        src.connect(proxy);
        
        src.disconnect(proxy);
        sink.disconnect(proxy);
        
        assertTrue("Format still assigned",src.getFormat() == null);
        assertTrue("Format still assigned",sink.getFormat() == null);
    }

    @Test
    public void testDisconnect2() {
        sink.connect(proxy);
        src.connect(proxy);
        
        src.disconnect(proxy);
        sink.disconnect(proxy);
        
        assertTrue("Format still assigned",src.getFormat() == null);
        assertTrue("Format still assigned",sink.getFormat() == null);
    }

    @Test
    public void testDisconnect3() {
        sink.connect(proxy);
        src.connect(proxy);
        
        //src.disconnect(proxy);
        sink.disconnect(proxy);
        
        assertTrue("Format still assigned",src.getFormat() == null);
//        assertTrue("Format still assigned",sink.getFormat() == null);
    }
    
    private class TestSource extends AbstractSource {

        public TestSource(String name) {
            super(name);
        }

        public Format[] getFormats() {
            return new Format[]{AVProfile.PCMA, AVProfile.PCMU, AVProfile.L16_MONO};
        }

        public String getOtherPartyName() {
            return this.otherParty.getName();
        }

        @Override
        public void evolve(Buffer buffer, long timestamp, long sequenceNumber) {
        }
    }

    private class TestSink extends AbstractSink {

        public TestSink(String name) {
            super(name);
        }

        public Format[] getFormats() {
            return new Format[]{AVProfile.PCMA};
        }

        public boolean isAcceptable(Format format) {
            return true;
        }

        public String getOtherPartyName() {
            return this.otherParty.getName();
        }

        @Override
        public void onMediaTransfer(Buffer buffer) {
        }
    }

}