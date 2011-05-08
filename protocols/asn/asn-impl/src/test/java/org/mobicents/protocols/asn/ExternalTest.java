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

package org.mobicents.protocols.asn;

import java.io.ByteArrayInputStream;
import java.util.Arrays;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 * @author amit bhayani
 * 
 */
public class ExternalTest extends TestCase {

	@BeforeClass
	public static void setUpClass() throws Exception {
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {

	}

	@After
	public void tearDown() {
	}

	@Test
	public void testDecode() throws Exception {

		// This raw data is from wireshark trace of TCAP - MAP
		byte[] data = new byte[] { 0x28, 0x23, 0x06, 0x07, 0x04, 0x00,
				0x00, 0x01, 0x01, 0x01, 0x01, (byte) 0xa0, 0x18, (byte) 0xa0,
				(byte) 0x80, (byte) 0x80, 0x09, (byte) 0x96, 0x02, 0x24,
				(byte) 0x80, 0x03, 0x00, (byte) 0x80, 0x00, (byte) 0xf2,
				(byte) 0x81, 0x07, (byte) 0x91, 0x13, 0x26, (byte) 0x98,
				(byte) 0x86, 0x03, (byte) 0xf0, 0x00, 0x00 };

		AsnInputStream asin = new AsnInputStream(new ByteArrayInputStream(data));
		
		int tag = asin.readTag();
		
		assertTrue(External._TAG_IMPLICIT_SEQUENCE == tag);

		External external = new External();
		external.decode(asin);

		assertTrue(external.isOid());
		assertTrue(Arrays.equals(new long[] { 0, 4, 0, 0, 1, 1, 1, 1 },
				external.getOidValue()));

		assertFalse(external.isInteger());

		assertTrue(external.isAsn());
		assertTrue(Arrays.equals(new byte[] { (byte) 0xa0, (byte) 0x80,
				(byte) 0x80, 0x09, (byte) 0x96, 0x02, 0x24, (byte) 0x80, 0x03,
				0x00, (byte) 0x80, 0x00, (byte) 0xf2, (byte) 0x81, 0x07,
				(byte) 0x91, 0x13, 0x26, (byte) 0x98, (byte) 0x86, 0x03,
				(byte) 0xf0, 0x00, 0x00 }, external.getEncodeType()));

	}

	@Test
	public void testEncode() throws Exception {
		
		
		// This raw data is from wireshark trace of TCAP - MAP
		byte[] data = new byte[] { 0x28, 0x23, 0x06, 0x07, 0x04, 0x00,
				0x00, 0x01, 0x01, 0x01, 0x01, (byte) 0xa0, 0x18, (byte) 0xa0,
				(byte) 0x80, (byte) 0x80, 0x09, (byte) 0x96, 0x02, 0x24,
				(byte) 0x80, 0x03, 0x00, (byte) 0x80, 0x00, (byte) 0xf2,
				(byte) 0x81, 0x07, (byte) 0x91, 0x13, 0x26, (byte) 0x98,
				(byte) 0x86, 0x03, (byte) 0xf0, 0x00, 0x00 };


		External external = new External();
		external.setOid(true);
		external.setOidValue(new long[] { 0, 4, 0, 0, 1, 1, 1, 1 });

		external.setAsn(true);
		external.setEncodeType(new byte[] { (byte) 0xa0, (byte) 0x80,
				(byte) 0x80, 0x09, (byte) 0x96, 0x02, 0x24, (byte) 0x80, 0x03,
				0x00, (byte) 0x80, 0x00, (byte) 0xf2, (byte) 0x81, 0x07,
				(byte) 0x91, 0x13, 0x26, (byte) 0x98, (byte) 0x86, 0x03,
				(byte) 0xf0, 0x00, 0x00 });
		
		AsnOutputStream asnOs = new AsnOutputStream();
		
		external.encode(asnOs);
		
		byte[] encodedData = asnOs.toByteArray();
		
		System.out.println(dump(encodedData, encodedData.length, false));
		
		assertTrue(Arrays.equals(data, encodedData));
		
	}
	
	
	
	public final static String dump(byte[] buff, int size, boolean asBits) {
		String s = "";
		for (int i = 0; i < size; i++) {
			String ss = null;
			if(!asBits)
			{
				ss = Integer.toHexString(buff[i] & 0xff);
			}
			else
			{
				ss = Integer.toBinaryString(buff[i] & 0xff); 
			}
			ss = fillInZeroPrefix(ss,asBits);
			s += " " + ss;
		}
		return s;
	}
	
	public final static String fillInZeroPrefix(String ss, boolean asBits) {
		if (asBits) {
			if (ss.length() < 8) {
				for (int j = ss.length(); j < 8; j++) {
					ss = "0" + ss;
				}
			}
		} else {
			// hex
			if (ss.length() < 2) {

				ss = "0" + ss;
			}
		}

		return ss;
	}

}
