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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mobicents.protocols.ss7.indicator.GlobalTitleIndicator;
import org.mobicents.protocols.ss7.indicator.NatureOfAddress;
import org.mobicents.protocols.ss7.sccp.parameter.GT0001;
import org.mobicents.protocols.ss7.sccp.parameter.GlobalTitle;
import org.mobicents.protocols.ss7.sccp.parameter.SccpAddress;

/**
 *
 * @author kulikov
 */
public class RuleTest {
    private final static String RULE = "1; # #NATIONAL#9023629581# ; # #INTERNATIONAL#79023629581# ;linkset#14083#14155#0\n";
    private final static String RULE1 = "1; # #NATIONAL#9023629581# ; # #INTERNATIONAL#79023629581# ;\n";
    
    public RuleTest() {
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

    @Test
    public void testMatches() {
        Rule rule = Rule.getInstance(RULE);
        SccpAddress address = new SccpAddress(GlobalTitle.getInstance(NatureOfAddress.NATIONAL, "9023629581"), 0);
        assertTrue(rule.matches(address));
    }
    
    @Test
    public void testTranslation() {
        SccpAddress a1 = new SccpAddress(GlobalTitle.getInstance(NatureOfAddress.NATIONAL, "9023629581"), 0);
        Rule rule = Rule.getInstance(RULE);
        
        SccpAddress a2 = rule.translate(a1);
        assertEquals(a2.getGlobalTitle().getIndicator(), GlobalTitleIndicator.GLOBAL_TITLE_INCLUDES_NATURE_OF_ADDRESS_INDICATOR_ONLY);
        assertEquals(NatureOfAddress.INTERNATIONAL, ((GT0001)a2.getGlobalTitle()).getNoA());
        assertEquals("79023629581", a2.getGlobalTitle().getDigits());
    }
    /**
     * Test of getInstance method, of class Rule.
     */
    @Test
    public void testGetInstance() {
        Rule rule = Rule.getInstance(RULE);
        assertEquals(1, rule.getNo());
        assertEquals(null, rule.getPattern().getNumberingPlan());
        assertEquals(NatureOfAddress.NATIONAL, rule.getPattern().getNatureOfAddress());
        assertEquals("9023629581", rule.getPattern().getDigits());
        
        assertEquals(null, rule.getTranslation().getNumberingPlan());
        assertEquals(NatureOfAddress.INTERNATIONAL, rule.getTranslation().getNatureOfAddress());
        assertEquals("79023629581", rule.getTranslation().getDigits());
        
        assertEquals("linkset", rule.getMTPInfo().getName());
        assertEquals(14083, rule.getMTPInfo().getOpc());
        assertEquals(14155, rule.getMTPInfo().getDpc());
        assertEquals(0, rule.getMTPInfo().getSls());
    }

    @Test
    public void testGetInstanceWithOptions() {
        Rule rule = Rule.getInstance(RULE1);
        assertEquals(1, rule.getNo());
        assertEquals(null, rule.getPattern().getNumberingPlan());
        assertEquals(NatureOfAddress.NATIONAL, rule.getPattern().getNatureOfAddress());
        assertEquals("9023629581", rule.getPattern().getDigits());
        
        assertEquals(null, rule.getTranslation().getNumberingPlan());
        assertEquals(NatureOfAddress.INTERNATIONAL, rule.getTranslation().getNatureOfAddress());
        assertEquals("79023629581", rule.getTranslation().getDigits());
        
        assertEquals(null, rule.getMTPInfo());
    }
    
    /**
     * Test of toString method, of class Rule.
     */
    @Test
    public void testToString() {
        AddressInformation ai = new AddressInformation(-1, null, NatureOfAddress.NATIONAL, "9023629581", -1);
        AddressInformation tr = new AddressInformation(-1, null, NatureOfAddress.INTERNATIONAL, "79023629581", -1);
        MTPInfo mtpInfo = new MTPInfo("linkset", 14083, 14155, 0);
        
        Rule rule = new Rule(1, ai, tr, mtpInfo);
        assertEquals(RULE, rule.toString());
    }

}