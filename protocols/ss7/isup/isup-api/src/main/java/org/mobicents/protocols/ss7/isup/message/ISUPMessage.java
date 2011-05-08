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

package org.mobicents.protocols.ss7.isup.message;

import org.mobicents.protocols.ss7.isup.ISUPComponent;
import org.mobicents.protocols.ss7.isup.ISUPTransaction;
import org.mobicents.protocols.ss7.isup.ParameterRangeInvalidException;
import org.mobicents.protocols.ss7.isup.message.parameter.CircuitIdentificationCode;
import org.mobicents.protocols.ss7.isup.message.parameter.ISUPParameter;
import org.mobicents.protocols.ss7.isup.message.parameter.MessageType;

/**
 * Start time:08:55:07 2009-08-30<br>
 * Project: mobicents-isup-stack<br>
 * 
 * @author <a href="mailto:baranowb@gmail.com">Bartosz Baranowski </a>
 */
public interface ISUPMessage extends ISUPComponent {

	/**
	 * Get mandatory field, CIC.
	 * @return
	 */
	public CircuitIdentificationCode getCircuitIdentificationCode();
	/**
	 * Set mandatory field, CIC.
	 * @return
	 */
	public void setCircuitIdentificationCode(CircuitIdentificationCode cic);

	/**
	 * Returns message code. See Q.763 Table 4. It simply return value of static
	 * constant, where value of parameter is value of MESSAGE_CODE
	 * 
	 * @return
	 */
	public MessageType getMessageType();
	
	/**
	 * Adds parameter to this message.
	 * @param param
	 * @throws ParameterRangeInvalidException - thrown if parameter is not part of message.
	 */
	public void addParameter(ISUPParameter param) throws ParameterRangeInvalidException;
	/**
	 * Returns parameter with passed code.
	 * @param parameterCode
	 * @return
	 * @throws ParameterRangeInvalidException - thrown if code does not match any parameter.
	 */
	public ISUPParameter getParameter(int parameterCode) throws ParameterRangeInvalidException;
	/**
	 * Removes parameter from this message.
	 * @param parameterCode
	 * @throws ParameterRangeInvalidException
	 */
	public void removeParameter(int parameterCode) throws ParameterRangeInvalidException;
	
	/**
	 * Return reference to transaction if it exists.
	 * @return
	 */
	public ISUPTransaction getTransaction();

	/**
	 * @return <ul>
	 *         <li><b>true</b> - if all requried parameters are set</li>
	 *         <li><b>false</b> - otherwise</li>
	 *         </ul>
	 */
	public boolean hasAllMandatoryParameters();

}
