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

import java.io.IOException;

import org.mobicents.protocols.ss7.isup.ParameterRangeInvalidException;
import org.mobicents.protocols.ss7.isup.message.parameter.LoopPreventionIndicators;

/**
 * Start time:11:31:36 2009-04-05<br>
 * Project: mobicents-isup-stack<br>
 * 
 * @author <a href="mailto:baranowb@gmail.com"> Bartosz Baranowski
 *         </a>
 */
public class LoopPreventionIndicatorsImpl extends AbstractParameter implements LoopPreventionIndicators{


	private static final int _TURN_ON = 1;
	private static final int _TURN_OFF = 0;

	
	private boolean response ;
	private int responseIndicator;

	public LoopPreventionIndicatorsImpl() {
		super();
		
	}
	
	public LoopPreventionIndicatorsImpl(boolean response, int responseIndicator) {
		super();
		this.response = response;
		this.responseIndicator = responseIndicator;
	}

	public LoopPreventionIndicatorsImpl(byte[] b) throws ParameterRangeInvalidException {
		super();
		decodeElement(b);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.isup.ISUPComponent#decodeElement(byte[])
	 */
	public int decodeElement(byte[] b) throws ParameterRangeInvalidException {
		if (b == null || b.length != 1) {
			throw new ParameterRangeInvalidException("byte[] must  not be null and length must  be 1");
		}

		this.response = (b[0] & 0x01) == _TURN_ON;

		if (response) {
			this.responseIndicator = (b[0] >> 1) & 0x03;
		}
		return 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.isup.ISUPComponent#encodeElement()
	 */
	public byte[] encodeElement() throws IOException {
		int v = this.response ? _TURN_ON : _TURN_OFF;
		if (this.response) {
			v |= (this.responseIndicator & 0x03) << 1;
		}
		return new byte[] { (byte) v };
	}

	public boolean isResponse() {
		return response;
	}

	public void setResponse(boolean response) {
		this.response = response;
	}

	public int getResponseIndicator() {
		return responseIndicator;
	}

	public void setResponseIndicator(int responseIndicator) {
		this.responseIndicator = responseIndicator;
	}

	public int getCode() {

		return _PARAMETER_CODE;
	}
}
