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

package org.mobicents.protocols.ss7.m3ua.message.parm;

/**
 * Constructs parameters.
 * 
 * @author kulikov
 */
public interface ParameterFactory {
    /**
     * Constructs Protocol Data parameter.
     * 
     * @param opc the origination point code
     * @param dpc the destination point code
     * @param si the service indicator
     * @param ni the network indicator
     * @param mp the message priority indicator
     * @param sls the signaling link selection
     * @param data message payload
     * @return Protocol data parameter
     */
    public ProtocolData createProtocolData(int opc, int dpc, int si, int ni, int mp, int sls, byte[] data);
    
    /**
     * Constructs Protocol Data parameter.
     * 
     * @param mp message priority
     * @param msu the message signaling unit
     * @return Protocol data parameter
     */
    public ProtocolData createProtocolData(int mp, byte[] msu);
    
}
