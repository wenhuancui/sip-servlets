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

package org.mobicents.protocols.ss7.sccp.impl.router;

import java.io.IOException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mobicents.protocols.ss7.indicator.NatureOfAddress;

import static org.junit.Assert.*;

/**
 *
 * @author kulikov
 */
public class RouterImplTest {
    private final static String RULE1 = "1; #ISDN_MOBILE#NATIONAL#9023629581# ; #ISDN_MOBILE#INTERNATIONAL#79023629581# ;linkset#14083#14155#0\n";
    private final static String RULE2 = "2; #ISDN_MOBILE#INTERNATIONAL#9023629581# ; #ISDN_MOBILE#NATIONAL#9023629581# ;linkset#14083#14155#0\n";
    
    private Rule rule1, rule2;
    
    public RouterImplTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws IOException {
        rule1 = Rule.getInstance(RULE1);
        rule2 = Rule.getInstance(RULE2);
        
        //cleans config file
        RouterImpl router = new RouterImpl("sccp-routing.txt");
        router.clean();
    }

    @After
    public void tearDown() {
    }

    
    /**
     * Test of add method, of class RouterImpl.
     */
    @Test
    public void testRouter() throws Exception {
        RouterImpl router = new RouterImpl("sccp-routing.txt");
        router.add(rule1);
        assertEquals(1, router.list().size());
        router.add(rule2);
        assertEquals(2, router.list().size());
        
        router.remove(1);
        Rule rule = router.list().iterator().next();
        assertEquals(NatureOfAddress.NATIONAL, rule.getPattern().getNatureOfAddress());
    }


}