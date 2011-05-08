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
import org.mobicents.protocols.ss7.isup.message.parameter.TransimissionMediumRequierementPrime;

/**
 * Start time:18:41:12 2009-04-03<br>
 * Project: mobicents-isup-stack<br>
 * 
 * @author <a href="mailto:baranowb@gmail.com"> Bartosz Baranowski
 *         </a>
 */
public class TransimissionMediumRequierementPrimeImpl extends AbstractParameter implements TransimissionMediumRequierementPrime{
	

	
	public TransimissionMediumRequierementPrimeImpl() {
		super();
		
	}

	public TransimissionMediumRequierementPrimeImpl(int transimissionMediumRequirement) {
		super();
		this.transimissionMediumRequirement = transimissionMediumRequirement;
	}

	public TransimissionMediumRequierementPrimeImpl(byte[] b) throws ParameterRangeInvalidException {
		super();
		decodeElement(b);
	}

	// Defualt indicate speech
	private int transimissionMediumRequirement;

	// FIXME: again wrapper class but hell there is a lot of statics....
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.isup.ISUPComponent#decodeElement(byte[])
	 */
	public int decodeElement(byte[] b) throws ParameterRangeInvalidException {
		if (b == null || b.length != 1) {
			throw new ParameterRangeInvalidException("byte[] must  not be null and length must  be 1");
		}

		this.transimissionMediumRequirement = b[0];

		return 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.isup.ISUPComponent#encodeElement()
	 */
	public byte[] encodeElement() throws IOException {
		return new byte[] { (byte) this.transimissionMediumRequirement };
	}

	public int getTransimissionMediumRequirement() {
		return transimissionMediumRequirement;
	}

	public void setTransimissionMediumRequirement(int transimissionMediumRequirement) {
		this.transimissionMediumRequirement = transimissionMediumRequirement;
	}
	
	public int getCode() {

		return _PARAMETER_CODE;
	}

}
