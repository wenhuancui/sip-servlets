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
package org.mobicents.media.server.resource;

import java.util.ArrayList;
import java.util.concurrent.ScheduledFuture;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mobicents.media.Buffer;
import org.mobicents.media.Component;
import org.mobicents.media.ComponentFactory;
import org.mobicents.media.Format;
import org.mobicents.media.MediaSink;
import org.mobicents.media.MediaSource;
import org.mobicents.media.Server;
import org.mobicents.media.server.VirtualEndpointImpl;
import org.mobicents.media.server.impl.AbstractSink;
import org.mobicents.media.server.impl.AbstractSource;
import org.mobicents.media.server.impl.resource.Proxy;
import org.mobicents.media.server.spi.Endpoint;
import org.mobicents.media.server.spi.MediaType;
import org.mobicents.media.server.spi.Valve;

/**
 *
 * @author kulikov
 */
public class ChanneWithPipesTest {

    private Server server;
    
    public final Format FORMAT = new Format("test");
    private Endpoint endpoint;
    private TestSink sink = new TestSink("test-sink");
    private TestSource source = new TestSource("test-source");
    private ChannelFactory channelFactory = new ChannelFactory();
    private ArrayList<Buffer> list = new ArrayList();
    private TestGatewayFactory gateway;
    
    public ChanneWithPipesTest() {
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
        gateway = new TestGatewayFactory("gateway");
        endpoint = new VirtualEndpointImpl("test");
        list.clear();

        sink = new TestSink("test-sink");
        source = new TestSource("test-source");
        
        channelFactory = new ChannelFactory();

        ArrayList components = new ArrayList();
        components.add(gateway);
        
        ArrayList pipes = new ArrayList();

        PipeFactory p1 = new PipeFactory();
        p1.setInlet(null);
        p1.setOutlet("gateway");
        p1.setValve(Valve.OPEN);
        
        PipeFactory p2 = new PipeFactory();
        p2.setInlet("gateway");
        p2.setOutlet(null);
        p2.setValve(Valve.OPEN);
        
        pipes.add(p1);
        pipes.add(p2);
        
        channelFactory.setComponents(components);
        channelFactory.setPipes(pipes);
        channelFactory.start();
    }

    @After
    public void tearDown() {
        server.stop();
        try {
            Thread.currentThread().sleep(200);
        } catch (Exception e) {
            
        }
    }

    @Test
    public void testConnect1() throws Exception {
        Channel channel = channelFactory.newInstance(endpoint, MediaType.AUDIO);

        channel.connect(sink);
        channel.connect(source);
        
        assertEquals(true, sink.isConnected());
        assertEquals(true, source.isConnected());

        channel.start();
        
        source.start();   
        source.stop();
        assertFalse(list.isEmpty());

        channel.disconnect(sink);
        channel.disconnect(source);

        assertEquals(false, sink.isConnected());
        assertEquals(false, source.isConnected());
    }

    @Test
    public void testConnect2() throws Exception {
        Channel channel = channelFactory.newInstance(endpoint, MediaType.AUDIO);

        channel.connect(source);
        
        channel.connect(sink);
        
        assertEquals(true, sink.isConnected());
        assertEquals(true, source.isConnected());

        channel.start();
        source.start();        
        source.stop();
        assertFalse(list.isEmpty());
        
        channel.disconnect(sink);
        channel.disconnect(source);

        assertEquals(false, sink.isConnected());
        assertEquals(false, source.isConnected());
    }

    @Test
    public void testConnect3() throws Exception {
        Channel channel = channelFactory.newInstance(endpoint, MediaType.AUDIO);

        channel.connect(source);        
        channel.connect(sink);

        assertEquals(true, sink.isConnected());
        assertEquals(true, source.isConnected());

        channel.start();
        source.start();        
        source.stop();
        assertFalse(list.isEmpty());
        
        channel.disconnect(source);
        channel.disconnect(sink);

        assertEquals(false, sink.isConnected());
        assertEquals(false, source.isConnected());
    }

    @Test
    public void testConnect4() throws Exception {
        Channel channel = channelFactory.newInstance(endpoint, MediaType.AUDIO);

        channel.connect(sink);
        channel.connect(source);

        assertEquals(true, sink.isConnected());
        assertEquals(true, source.isConnected());

        channel.start();
        source.start();        
        source.stop();
        assertFalse(list.isEmpty());
        
        channel.disconnect(source);
        channel.disconnect(sink);

        assertEquals(false, sink.isConnected());
        assertEquals(false, source.isConnected());
    }
    
    @Test
    public void testChannelConnect1() throws Exception {
        Channel channel1 = channelFactory.newInstance(endpoint, MediaType.AUDIO);
        Channel channel2 = channelFactory.newInstance(endpoint, MediaType.AUDIO);

        channel1.connect(source);
        channel2.connect(sink);

        channel1.connect(channel2);

        assertEquals(true, sink.isConnected());
        assertEquals(true, source.isConnected());

        channel1.start();
        channel2.start();
        
        source.start();        
        source.stop();
        assertFalse(list.isEmpty());
        
        
        channel1.disconnect(channel2);
        
        list.clear();
        channel1.disconnect(source);
        channel2.disconnect(sink);

        assertEquals(false, sink.isConnected());
        assertEquals(false, source.isConnected());
    }

    @Test
    public void testChannelConnect2() throws Exception {
        Channel channel1 = channelFactory.newInstance(endpoint, MediaType.AUDIO);
        Channel channel2 = channelFactory.newInstance(endpoint, MediaType.AUDIO);

        channel1.connect(channel2);

        channel2.connect(sink);
        channel1.connect(source);


        assertEquals(true, sink.isConnected());
        assertEquals(true, source.isConnected());

        channel1.start();
        channel2.start();
        source.start();        
        source.stop();
        assertFalse(list.isEmpty());
        
        channel1.disconnect(channel2);
        
        channel1.disconnect(source);
        channel2.disconnect(sink);

        assertEquals(false, sink.isConnected());
        assertEquals(false, source.isConnected());
    }

    @Test
    public void testChannelConnect3() throws Exception {
        Channel channel1 = channelFactory.newInstance(endpoint, MediaType.AUDIO);
        Channel channel2 = channelFactory.newInstance(endpoint, MediaType.AUDIO);

        channel1.connect(channel2);

        channel2.connect(sink);
        channel1.connect(source);

        assertEquals(true, sink.isConnected());
        assertEquals(true, source.isConnected());

        channel1.start();
        channel2.start();
        
        source.start();        
        source.stop();
        assertFalse(list.isEmpty());
        
        
        channel1.disconnect(source);
        channel2.disconnect(sink);


        assertEquals(false, sink.isConnected());
        assertEquals(false, source.isConnected());
    }

    @Test
    public void testChannelConnect4() throws Exception {
        Channel channel1 = channelFactory.newInstance(endpoint, MediaType.AUDIO);
        Channel channel2 = channelFactory.newInstance(endpoint, MediaType.AUDIO);

        channel1.connect(channel2);

        channel2.connect(sink);
        channel1.connect(source);


        assertEquals(true, sink.isConnected());
        assertEquals(true, source.isConnected());

        channel1.start();
        channel2.start();
        source.start();        
        source.stop();
        assertFalse(list.isEmpty());
        
        
        channel1.disconnect(channel2);

        list.clear();
        
        channel1.disconnect(source);
        channel2.disconnect(sink);

        assertEquals(false, sink.isConnected());
        assertEquals(false, source.isConnected());
    }

    @Test
    public void testInputFormats() throws Exception {
        Channel channel = channelFactory.newInstance(endpoint, MediaType.AUDIO);

        Format[] f = channel.getInputFormats();
        assertEquals(1, f.length);
                
        channel.connect(sink);
        
        f = channel.getInputFormats();
        assertEquals(1, f.length);
        assertEquals(true, f[0].matches(FORMAT));
        
        channel.disconnect(sink);

        assertEquals(false, sink.isConnected());
        
        f = channel.getInputFormats();
        assertEquals(1, f.length);
    }

//    @Test
    public void testOutputFormats() throws Exception {
        Channel channel = channelFactory.newInstance(endpoint, MediaType.AUDIO);

        Format[] f = channel.getOutputFormats();
        assertEquals(1, f.length);
                
        channel.connect(source);
        
        f = channel.getOutputFormats();
        assertEquals(1, f.length);
        assertEquals(true, f[0].matches(FORMAT));
        
        channel.disconnect(source);

        assertEquals(false, source.isConnected());
        
        f = channel.getOutputFormats();
        assertEquals(1, f.length);
    }
    

    private class TestGatewayFactory implements ComponentFactory {
        
        private String name;
        
        public TestGatewayFactory(String name) {
            this.name = name;
        }
        
        public Component newInstance(Endpoint endpoint) {
            return new Proxy(name);
        }
        
    }
    
    private class TestSource extends AbstractSource  {

        private ScheduledFuture task;
        private int count;

        public TestSource(String name) {
            super(name);
        }

        @Override
        public void start() {
            super.start();
            try {
                Thread.currentThread().sleep(1000);
            } catch (Exception e) {
            }
        }

        public MediaSink getOtherParty() {
            return otherParty;
        }

        public Format[] getFormats() {
            return new Format[]{FORMAT};
        }

        @Override
        public void evolve(Buffer buffer, long timestamp) {
            buffer.setFormat(FORMAT);
            buffer.setTimeStamp(count * 20);
            buffer.setSequenceNumber(count++);
            buffer.setData(new byte[160]);
            buffer.setOffset(0);
            buffer.setLength(160);
        }
    }

    private class TestSink extends AbstractSink {

        public TestSink(String name) {
            super(name);
        }

        public Format[] getFormats() {
            return new Format[]{FORMAT};
        }

        public boolean isAcceptable(Format format) {
            return true;
        }


        public MediaSource getOtherParty() {
            return otherParty;
        }

        @Override
        public void onMediaTransfer(Buffer buffer) {
            list.add(buffer);
        }
    }

    
}