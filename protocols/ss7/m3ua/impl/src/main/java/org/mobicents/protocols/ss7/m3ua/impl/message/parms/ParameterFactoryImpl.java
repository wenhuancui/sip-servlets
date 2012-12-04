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

package org.mobicents.protocols.ss7.m3ua.impl.message.parms;

import org.mobicents.protocols.ss7.m3ua.message.parm.*;
import org.mobicents.protocols.ss7.m3ua.impl.message.ParameterImpl;

/**
 *
 * @author kulikov
 */
public class ParameterFactoryImpl implements ParameterFactory {
    public ProtocolDataImpl createProtocolData(int opc, int dpc, int si, int ni, int mp, int sls, byte[] data) {
        return new ProtocolDataImpl(opc, dpc, si, ni, mp, sls, data);
    }
    
    public ProtocolData createProtocolData(int mp, byte[] msu) {
        ProtocolDataImpl p = new ProtocolDataImpl();
        p.load(msu);
        return p;
    }
    
    public ProtocolData createProtocolData(byte[] msu) {
        return new ProtocolDataImpl(msu);
    }
    
    public ParameterImpl createParameter(int tag, byte[] value) {
        ParameterImpl p = null;
        switch (tag) {
            case ParameterImpl.Protocol_Data : 
                p = new ProtocolDataImpl(value);
                break;
            default :
                p = new UnknownParameterImpl(tag, value.length, value);
                break;
        }
        return p;
    }

    
}
