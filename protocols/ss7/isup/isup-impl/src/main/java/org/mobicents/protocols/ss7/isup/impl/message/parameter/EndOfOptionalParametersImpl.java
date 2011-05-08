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

package org.mobicents.protocols.ss7.isup.impl.message.parameter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.mobicents.protocols.ss7.isup.ParameterRangeInvalidException;
import org.mobicents.protocols.ss7.isup.message.parameter.EndOfOptionalParameters;

/**
 * Start time:11:21:05 2009-03-31<br>
 * Project: mobicents-isup-stack<br>
 * This class represent element that encodes end of parameters
 * 
 * @author <a href="mailto:baranowb@gmail.com"> Bartosz Baranowski
 *         </a>
 */
public class EndOfOptionalParametersImpl extends AbstractParameter implements EndOfOptionalParameters{

	public EndOfOptionalParametersImpl() {
		super();
		
	}

	public EndOfOptionalParametersImpl(byte[] b) {
		super();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.isup.ISUPComponent#decodeElement(byte[])
	 */
	/**
	 * heeh, value is zero actually :D
	 */
	public static final int _PARAMETER_CODE = 0;

	public int decodeElement(byte[] b) throws ParameterRangeInvalidException {

		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.isup.ISUPComponent#encodeElement()
	 */
	public byte[] encodeElement() throws IOException {
		// TODO Auto-generated method stub
		return new byte[] { 0 };
	}

	@Override
	public int encodeElement(ByteArrayOutputStream bos) throws IOException {
		bos.write(0);
		return 1;
	}

	public int getCode() {

		return _PARAMETER_CODE;
	}
}
