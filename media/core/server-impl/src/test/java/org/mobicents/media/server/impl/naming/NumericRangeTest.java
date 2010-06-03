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
package org.mobicents.media.server.impl.naming;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author kulikov
 */
public class NumericRangeTest {

    private final static String token = "[1..100]";
    private NumericRange range;
    
    public NumericRangeTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        range = new NumericRange(token);
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of hasMore method, of class NumericRange.
     */
    @Test
    public void testHasMore() {
        for (int i = 1; i <= 100; i++) {
            assertEquals(true, range.hasMore());
        }
    }

    /**
     * Test of next method, of class NumericRange.
     */
    @Test
    public void testNext() {
        for (int i = 1; i <= 100; i++) {
            assertEquals(Integer.toString(i), range.next());
        }
    }

}