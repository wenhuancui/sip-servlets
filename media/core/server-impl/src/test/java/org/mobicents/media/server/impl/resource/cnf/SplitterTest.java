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

package org.mobicents.media.server.impl.resource.cnf;

import org.mobicents.media.server.impl.resource.*;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mobicents.media.Server;
import static org.junit.Assert.*;
import org.mobicents.media.server.impl.resource.test.TransmissionTester;

/**
 *
 * @author kulikov
 */
public class SplitterTest {

    private Server server;
    private Splitter splitter;
    private TransmissionTester tester;
    
    public SplitterTest() {
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
        
        splitter = new Splitter("test");
        tester = new TransmissionTester();
    }

    @After
    public void tearDown() {
        server.stop();
    }

    /**
     * Test of start method, of class Proxy.
     */
    @Test
    public void testTransmission() {
        tester.connect(splitter.getInput());
        tester.connect(splitter);
        splitter.start();
        tester.start();
        assertTrue(tester.getMessage(), tester.isPassed());
        splitter.stop();
    }

}