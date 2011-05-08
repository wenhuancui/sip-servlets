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
import java.util.logging.Logger;

import org.mobicents.protocols.ss7.isup.ISUPComponent;
import org.mobicents.protocols.ss7.isup.message.parameter.ISUPParameter;

/**
 * Start time:12:54:56 2009-03-30<br>
 * Project: mobicents-isup-stack<br>
 * Simple class to define common methods and fields for all.
 * @author <a href="mailto:baranowb@gmail.com"> Bartosz Baranowski
 *         </a>

 */
public abstract class AbstractParameter implements ISUPParameter,ISUPComponent {

	//protected byte[] tag = null;
	protected Logger logger  = Logger.getLogger(this.getClass().getName());
//	public byte[] getTag() {
//		return this.tag;
//	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.isup.ISUPComponent#encodeElement(java.io.ByteArrayOutputStream
	 * )
	 */
	public int encodeElement(ByteArrayOutputStream bos) throws IOException {
		//FIXME: this has to be removed, we should not create separate arrays?
		byte[] b = this.encodeElement();
		bos.write(b);
		return b.length;
	}

}
