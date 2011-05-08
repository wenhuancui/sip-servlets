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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mobicents.media.server.impl;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mobicents.media.Buffer;
import org.mobicents.media.Format;
import org.mobicents.media.Server;
import org.mobicents.media.server.spi.rtp.AVProfile;

/**
 *
 * @author kulikov
 */
public class AbstractSourceTest {

    private Server server;
    private TestSource source = new TestSource("test-source");
    
    public AbstractSourceTest() {
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
    }

    @After
    public void tearDown() {
        server.stop();
    }

    /**
     * Test of getSyncSource method, of class AbstractSource.
     */
    @Test
    @SuppressWarnings("static-access")
    public void testRestart() throws Exception {
        System.out.println("Starting ");
        source.start();
        System.out.println("Started ");
        Thread.currentThread().sleep(3000);
        
        source.stop();
        System.out.println("Stopped ");
//        Thread.currentThread().sleep(1500);
        source.start();
        
        Thread.currentThread().sleep(3000);
    }


    public class TestSource extends AbstractSource {

        public TestSource(String name) {
            super(name);
        }
        
        @Override
        public void evolve(Buffer buffer, long timestamp) {
            buffer.setLength(320);
            buffer.setDuration(500);
            buffer.setFlags(Buffer.FLAG_LIVE_DATA);
            System.out.println(System.currentTimeMillis() + " Evolve");
        }

        public Format[] getFormats() {
            return new Format[] {AVProfile.L16_MONO};
        }
        
    }
}