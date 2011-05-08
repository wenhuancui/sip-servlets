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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mobicents.protocols.ss7.indicator.NatureOfAddress;
import org.mobicents.protocols.ss7.indicator.NumberingPlan;

/**
 *
 * @author kulikov
 */
public class AddressInformationTest {
    private final static String ADDRESS_INFORMATION_1 = " #ISDN_MOBILE#NATIONAL#9023629581# ";
    
    public AddressInformationTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getInstance method, of class AddressInformation.
     */
    @Test
    public void testGetInstance() {
        AddressInformation ai = AddressInformation.getInstance(ADDRESS_INFORMATION_1);
        assertEquals(-1, ai.getTranslationType());
        assertEquals(NumberingPlan.ISDN_MOBILE, ai.getNumberingPlan());
        assertEquals(NatureOfAddress.NATIONAL, ai.getNatureOfAddress());
        assertEquals("9023629581", ai.getDigits());
        assertEquals(-1, ai.getSubsystem());
    }

    /**
     * Test of getTranslationType method, of class AddressInformation.
     */
    @Test
    public void testToString() {
        AddressInformation ai = new AddressInformation(-1, NumberingPlan.ISDN_MOBILE, 
                NatureOfAddress.NATIONAL, "9023629581", -1);
        assertEquals(ADDRESS_INFORMATION_1, ai.toString());
    }


}