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
import org.mobicents.protocols.ss7.isup.message.parameter.RedirectionNumberRestriction;

/**
 * Start time:16:55:01 2009-04-02<br>
 * Project: mobicents-isup-stack<br>
 * 
 * @author <a href="mailto:baranowb@gmail.com"> Bartosz Baranowski </a>
 * 
 */
public class RedirectionNumberRestrictionImpl extends AbstractParameter implements RedirectionNumberRestriction {

	private int presentationRestrictedIndicator;

	public RedirectionNumberRestrictionImpl(int presentationRestrictedIndicator) {
		super();
		this.presentationRestrictedIndicator = presentationRestrictedIndicator;
	}

	public RedirectionNumberRestrictionImpl() {
		super();
		
	}

	public RedirectionNumberRestrictionImpl(byte[] b) throws ParameterRangeInvalidException {
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

		this.presentationRestrictedIndicator = (byte) (b[0] & 0x03);
		return 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.isup.ISUPComponent#encodeElement()
	 */
	public byte[] encodeElement() throws IOException {
		return new byte[] { (byte) (this.presentationRestrictedIndicator & 0x03) };
	}

	public int getPresentationRestrictedIndicator() {
		return presentationRestrictedIndicator;
	}

	public void setPresentationRestrictedIndicator(int presentationRestrictedIndicator) {
		this.presentationRestrictedIndicator = presentationRestrictedIndicator;
	}

	public int getCode() {

		return _PARAMETER_CODE;
	}
}
