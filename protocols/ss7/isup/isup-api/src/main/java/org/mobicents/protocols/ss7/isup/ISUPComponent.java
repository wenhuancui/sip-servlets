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

package org.mobicents.protocols.ss7.isup;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

/**
 * Start time:13:34:05 2009-03-29<br>
 * Project: mobicents-isup-stack<br>
 * This is super interface for all components that can be parsed/encoded by ISUP
 * stack. It provides two essential methods that abtract decoding/encoding.
 * 
 * @author <a href="mailto:baranowb@gmail.com"> Bartosz Baranowski
 *         </a>
 */
public interface ISUPComponent extends Serializable {

	// FIXME: how is that indicated?
	public static final int _PROTOCOL_VERSION = 2;

	/**
	 * Decodes this element from passed byte[] array. This array must contain
	 * only element data. however in case of constructor elements it may contain
	 * more information elements that consist of tag, length and contents
	 * elements, this has to be handled accordingly in this method.
	 * 
	 * @param b
	 * @return
	 */
	int decodeElement(byte[] b) throws ParameterRangeInvalidException;


	/**
	 * Encodes elements as byte[].it contains body. (tag, length and Contents.
	 * See B.4/Q.763 - page 119)
	 * 
	 * @return byte[] with encoded element.
	 * @throws IOException
	 */
	byte[] encodeElement() throws IOException;

	/**
	 * Encodes elements as byte[]. It contains body. (tag, length and Contents.
	 * See B.4/Q.763 - page 119)
	 * 
	 * @return number of bytes encoded
	 * @throws IOException
	 */
	int encodeElement(ByteArrayOutputStream bos) throws IOException;

}
