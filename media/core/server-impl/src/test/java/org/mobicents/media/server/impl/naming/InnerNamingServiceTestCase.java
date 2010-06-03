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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.jboss.test.kernel.junit.MicrocontainerTest;
import org.mobicents.media.server.spi.Endpoint;
import org.mobicents.media.server.spi.ResourceUnavailableException;

/**
 * 
 * @author kulikov
 */
public class InnerNamingServiceTestCase extends MicrocontainerTest {

    Logger logger = Logger.getLogger(InnerNamingServiceTestCase.class);
    private InnerNamingService namingService;
    private final String name = "/media/aap/[1..10]";
    private final String name2 = "/media/aap/[1..10]/[1..2]";

    public InnerNamingServiceTestCase(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        namingService = new InnerNamingService();
        namingService.start();
    }

    /**
     * Test of getNames method, of class InnerNamingService.
     */
    public void testGetNames() {
        NameParser parser = new NameParser();
        Iterator<NameToken> tokens = parser.parse(name).iterator();

        ArrayList<String> prefixes = new ArrayList();
        prefixes.add("");

        Collection<String> names = namingService.getNames(prefixes, tokens.next(), tokens);

        Iterator<String> it = names.iterator();
        for (int i = 1; i <= 10; i++) {
            assertEquals("/media/aap/" + i, it.next());
        }
    }

    public void testGetNames2() {
        NameParser parser = new NameParser();
        Iterator<NameToken> tokens = parser.parse(name2).iterator();

        ArrayList<String> prefixes = new ArrayList();
        prefixes.add("");

        Collection<String> names = namingService.getNames(prefixes, tokens.next(), tokens);

        Iterator<String> it = names.iterator();
        for (int i = 1; i <= 2; i++) {
            for (int j = 1; j <= 10; j++) {
                assertEquals("/media/aap/" + j + "/" + i, it.next());
            }
        }
    }

    public void testFind() {

        namingService = (InnerNamingService) getBean("MediaServer");
        try {
            Endpoint endPt = namingService.find("/mobicents/media/aap/1", true);
            assertNotNull(endPt);

            try {
                endPt = namingService.find("/mobicents/media/aap/1", false);
                fail("ResourceUnavailableException should have been thrown");
            } catch (ResourceUnavailableException e) {
                logger.debug("Expected Error", e);
            }

        } catch (ResourceUnavailableException e) {
            e.printStackTrace();
            fail("testFind failed");
        }

    }

    public void testFindAny() {

        namingService = (InnerNamingService) getBean("MediaServer");
        try {
            Endpoint endPt = namingService.lookup("/mobicents/media/aap/$", false);
            assertNotNull(endPt);


            Endpoint endPt1 = namingService.lookup("/mobicents/media/aap/$", false);
            assertNotNull(endPt1);


            assertNotSame(endPt.getLocalName(), endPt1.getLocalName());

        } catch (ResourceUnavailableException e) {
            e.printStackTrace();
            fail("testFind failed");
        }

    }
}