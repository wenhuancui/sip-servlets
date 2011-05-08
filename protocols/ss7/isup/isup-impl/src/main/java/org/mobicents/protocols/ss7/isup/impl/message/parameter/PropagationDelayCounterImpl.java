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
import org.mobicents.protocols.ss7.isup.message.parameter.PropagationDelayCounter;

/**
 * Start time:14:20:15 2009-04-02<br>
 * Project: mobicents-isup-stack<br>
 * 
 * @author <a href="mailto:baranowb@gmail.com"> Bartosz Baranowski </a>
 */
public class PropagationDelayCounterImpl extends AbstractParameter implements PropagationDelayCounter {

	private int propagationDelay;

	public PropagationDelayCounterImpl() {
		super();
		
	}

	public PropagationDelayCounterImpl(byte[] b) throws ParameterRangeInvalidException {
		super();
		decodeElement(b);
	}

	public PropagationDelayCounterImpl(int propagationDelay) {
		super();
		this.propagationDelay = propagationDelay;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.isup.ISUPComponent#decodeElement(byte[])
	 */
	public int decodeElement(byte[] b) throws ParameterRangeInvalidException {
		// This one is other way around, as Eduardo might say.
		if (b == null || b.length != 2) {
			throw new ParameterRangeInvalidException("byte[] must  not be null and length must be 2");
		}

		this.propagationDelay = b[0] << 8;
		this.propagationDelay |= b[1];
		return b.length;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.isup.ISUPComponent#encodeElement()
	 */
	public byte[] encodeElement() throws IOException {

		byte b0 = (byte) (this.propagationDelay >> 8);
		byte b1 = (byte) this.propagationDelay;
		return new byte[] { b0, b1 };
	}

	public int getPropagationDelay() {
		return propagationDelay;
	}

	public void setPropagationDelay(int propagationDelay) {
		this.propagationDelay = propagationDelay;
	}

	public int getCode() {

		return _PARAMETER_CODE;
	}
}
