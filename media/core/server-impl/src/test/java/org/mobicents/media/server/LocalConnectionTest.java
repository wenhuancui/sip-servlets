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

package org.mobicents.media.server;


import java.util.Hashtable;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import org.mobicents.media.server.impl.resource.test.TesterSinkFactory;
import org.mobicents.media.server.impl.resource.test.TesterSourceFactory;
import org.mobicents.media.server.impl.resource.test.TransmissionTester;
import org.mobicents.media.server.resource.ChannelFactory;
import org.mobicents.media.server.spi.Connection;
import org.mobicents.media.server.spi.ConnectionMode;

/**
 *
 * @author kulikov
 */
public class LocalConnectionTest {

    private EndpointFactoryImpl sender;
    private EndpointFactoryImpl receiver;
    
    private TransmissionTester tester;
    
    private TesterSinkFactory sinkFactory;
    private TesterSourceFactory sourceFactory;
    
    private ChannelFactory channelFactory;
    
    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
/*        timer = new TimerImpl();
        timer.start();
        tester = new TransmissionTester(timer);
        
        sourceFactory = new TesterSourceFactory(tester);
        sinkFactory = new TesterSinkFactory(tester);
        
        channelFactory = new ChannelFactory();
        channelFactory.start();
        
        Hashtable rxFactories = new Hashtable();
        rxFactories.put("audio", channelFactory);

        Hashtable txFactories = new Hashtable();
        txFactories.put("audio", channelFactory);
        
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setRxChannelFactory(rxFactories);
        connectionFactory.setTxChannelFactory(txFactories);
        
        Hashtable sources = new Hashtable();
        sources.put("audio", sourceFactory);
        
        Hashtable sinks = new Hashtable();
        sinks.put("audio", sinkFactory);
        
        sender = new EndpointFactoryImpl("test/announcement/sender");
        sender.setTimer(timer);
        
        sender.setSourceFactory(sources);
        sender.setConnectionFactory(connectionFactory);
        
        sender.start();
        
        receiver = new EndpointFactoryImpl("test/announcement/receiver");
        receiver.setTimer(timer);
        
        receiver.setSinkFactory(sinks);
        receiver.setConnectionFactory(connectionFactory);
        receiver.start(); 
 */        
    }

    @After
    public void tearDown() {
//        timer.stop();
    }

    /**
     * Test of getLocalName method, of class EndpointImpl.
     */
    @Test
    public void testTransmission() throws Exception {
/*        Connection rxConnection = receiver.createLocalConnection();
        rxConnection.setMode(ConnectionMode.RECV_ONLY);
        Connection txConnection = sender.createLocalConnection();
        txConnection.setMode(ConnectionMode.SEND_ONLY);
        
        txConnection.setOtherParty(rxConnection);
        
        tester.start();
        
        receiver.deleteConnection(rxConnection.getId());
        sender.deleteConnection(txConnection.getId());

        assertTrue(tester.getMessage(), tester.isPassed());
 */ 
    }


}