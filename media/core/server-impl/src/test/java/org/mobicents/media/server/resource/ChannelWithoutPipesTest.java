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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mobicents.media.Buffer;
import org.mobicents.media.Format;
import org.mobicents.media.Server;
import org.mobicents.media.server.VirtualEndpointImpl;
import org.mobicents.media.server.impl.resource.test.TransmissionTester;
import org.mobicents.media.server.spi.Endpoint;
import org.mobicents.media.server.spi.MediaType;

/**
 *
 * @author kulikov
 */
public class ChannelWithoutPipesTest {

    private Server server;
    
    public final Format FORMAT = new Format("test");
    private Endpoint endpoint;
    private ChannelFactory channelFactory = new ChannelFactory();
    private ArrayList<Buffer> list = new ArrayList();
    private TransmissionTester tester;

    public ChannelWithoutPipesTest() {
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
        
        tester = new TransmissionTester();
        
        endpoint = new VirtualEndpointImpl("test");
        list.clear();

        channelFactory = new ChannelFactory();
        channelFactory.start();

        ArrayList components = new ArrayList();
        ArrayList pipes = new ArrayList();

        channelFactory.setComponents(components);
        channelFactory.setPipes(pipes);

    }

    @After
    public void tearDown() {
        server.stop();
    }

    @Test
    public void testConnect1() throws Exception {
        Channel channel = channelFactory.newInstance(endpoint, MediaType.AUDIO);

        channel.connect(tester.getDetector());
        channel.connect(tester.getGenerator());

        tester.start();
        assertTrue(tester.getMessage(), tester.isPassed());        
    }

    @Test
    public void testConnect2() throws Exception {
        Channel channel = channelFactory.newInstance(endpoint, MediaType.AUDIO);
    
        channel.connect(tester.getGenerator());
        channel.connect(tester.getDetector());
        
       

        tester.start();
        assertTrue(tester.getMessage(), tester.isPassed());        
    }

    @Test
    public void testInputFormats() throws Exception {
        Channel channel = channelFactory.newInstance(endpoint, MediaType.AUDIO);

        Format[] f = channel.getInputFormats();
        assertEquals(1, f.length);

        channel.connect(tester.getDetector());

        f = channel.getInputFormats();
        assertEquals(1, f.length);
        assertEquals(true, f[0].matches(tester.getDetector().getFormats()[0]));

        channel.disconnect(tester.getDetector());

        assertEquals(false, tester.getDetector().isConnected());

        f = channel.getInputFormats();
        assertEquals(1, f.length);
    }

    @Test
    public void testOutputFormats() throws Exception {
        Channel channel = channelFactory.newInstance(endpoint, MediaType.AUDIO);

        Format[] f = channel.getOutputFormats();
        assertEquals(1, f.length);

        channel.connect(tester.getGenerator());

        f = channel.getOutputFormats();
        assertEquals(1, f.length);
        assertEquals(true, f[0].matches(tester.getGenerator().getFormats()[0]));

        channel.disconnect(tester.getGenerator());

        assertEquals(false, tester.getGenerator().isConnected());

        f = channel.getOutputFormats();
        assertEquals(1, f.length);
    }

   @Test
    public void testTransmission() throws Exception {
        Channel channel = channelFactory.newInstance(endpoint, MediaType.AUDIO);

        channel.connect(tester.getGenerator());
        channel.connect(tester.getDetector());
        
        tester.start();
        assertTrue(tester.getMessage(), tester.isPassed());        
    }
    
    @Test
    public void testChannelConnect1() throws Exception {
        Channel channel1 = channelFactory.newInstance(endpoint, MediaType.AUDIO);
        Channel channel2 = channelFactory.newInstance(endpoint, MediaType.AUDIO);

        channel1.connect(tester.getGenerator());
        channel2.connect(tester.getDetector());

        channel1.connect(channel2);

        tester.start();
        assertTrue(tester.getMessage(), tester.isPassed());
    }

    @Test
    public void testChannelConnect2() throws Exception {
        Channel channel1 = channelFactory.newInstance(endpoint, MediaType.AUDIO);
        Channel channel2 = channelFactory.newInstance(endpoint, MediaType.AUDIO);

        channel1.connect(channel2);

        channel1.connect(tester.getGenerator());
        channel2.connect(tester.getDetector());

        tester.start();
        assertTrue(tester.getMessage(), tester.isPassed());
    }
  
}