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

/**
 * 
 */
package org.mobicents.protocols.asn;

/**
 * This class holds some vital BER statics
 * 
 * @author baranowb
 * @author abhayani
 */
public interface BERStatics {

	public static final int REAL_BB_SIGN_POSITIVE = 0x00;
	public static final int REAL_BB_SIGN_NEGATIVE = 0x01;
	public static final int REAL_BB_SIGN_MASK = 0x40;
	/**
	 * Mask for base:
	 * <ul>
	 * <li><b>00</b> base 2</li>
	 * <li><b>01</b> base 8</li>
	 * <li><b>11</b> base 16</li>
	 * </ul>
	 */
	public static final int REAL_BB_BASE_MASK = 0x30;
	/**
	 * Mask for scale:
	 * <ul>
	 * <li><b>00</b> 0</li>
	 * <li><b>01</b> 1</li>
	 * <li><b>10</b> 2</li>
	 * <li><b>11</b> 3</li>
	 * </ul>
	 */
	public static final int REAL_BB_SCALE_MASK = 0xC;
	/**
	 * Mask for encoding exponent (length):
	 * <ul>
	 * <li><b>00</b> on the following octet</li>
	 * <li><b>01</b> on the 2 following octets</li>
	 * <li><b>10</b> on the 3 following octets</li>
	 * <li><b>11</b> encoding of the length of the 2's-complement encoding of
	 * exponent on the following octet, and 2's-complement encoding of exponent
	 * on the other octets</li>
	 * </ul>
	 */
	public static final int REAL_BB_EE_MASK = 0x3;
	/**
	 * Value for real encoding in NR1 format
	 */
	public static final int REAL_NR1 = 0x1;
	/**
	 * Value for real encoding in NR2 format
	 */
	public static final int REAL_NR2 = 0x2;
	/**
	 * Value for real encoding in NR3 format
	 */
	public static final int REAL_NR3 = 0x3;
	/**
	 * Name of encoding scheme for java utils in case of IA5
	 */
	public static final String STRING_IA5_ENCODING = "US-ASCII";
	/**
	 * Name of encoding scheme for java utils in case of UTF8
	 */
	public static final String STRING_UTF8_ENCODING = "UTF-8";

}
