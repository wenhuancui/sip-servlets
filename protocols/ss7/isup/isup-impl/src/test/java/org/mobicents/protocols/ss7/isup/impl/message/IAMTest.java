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

package org.mobicents.protocols.ss7.isup.impl.message;

import org.mobicents.protocols.ss7.isup.message.ISUPMessage;
import org.mobicents.protocols.ss7.isup.message.InitialAddressMessage;

/**
 * Start time:15:07:07 2009-07-17<br>
 * Project: mobicents-isup-stack<br>
 * 
 * @author <a href="mailto:baranowb@gmail.com"> Bartosz Baranowski </a>
 */
public class IAMTest extends MessageHarness{


	
	public void testTwo_Parameters() throws Exception
	{
		byte[] message = getDefaultBody();

		//InitialAddressMessageImpl iam=new InitialAddressMessageImpl(this,message);
		InitialAddressMessage iam=super.messageFactory.createIAM(0);
		iam.decodeElement(message);
		assertNotNull(iam.getNatureOfConnectionIndicators());
		assertNotNull(iam.getForwardCallIndicators());
		assertNotNull(iam.getCallingPartCategory());
		assertNotNull(iam.getTransmissionMediumRequirement());
		assertNotNull(iam.getCalledPartyNumber());
		assertNotNull(iam.getCallingPartyNumber());

	}
	@Override
	protected byte[] getDefaultBody() {
		//FIXME: for now we strip MTP part
		byte[] message={

				0x0C
				,(byte) 0x0B
				,0x01,
				0x10,
				0x00,
				0x01,
				0x0A,
				0x03,
				0x02,
				0x0A,
				0x08,
				0x03,
				0x10,
				(byte) 0x83,
				0x60,
				0x38,
				0x04,
				0x10,
				0x65,
				0x0A,
				0x07,
				0x03,
				0x13,
				0x09,
				0x32,
				0x36,
				0x11,
				0x37,
				0x00

		};


		return message;
	}
	@Override
	protected ISUPMessage getDefaultMessage() {
		return super.messageFactory.createIAM(0);
	}
}
