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

package org.mobicents.protocols.ss7.m3ua.impl.message;

import org.mobicents.protocols.ss7.m3ua.message.*;
import java.nio.ByteBuffer;
import java.util.HashMap;
import org.mobicents.protocols.ss7.m3ua.impl.message.parms.ParameterFactoryImpl;

/**
 *
 * @author kulikov
 */
public abstract class M3UAMessageImpl implements M3UAMessage {
    //header part
    private int messageClass;
    private int messageType;
    
    protected HashMap<Short, Parameter> parameters = new HashMap();
    
    private ParameterFactoryImpl factory = new ParameterFactoryImpl();
    
    public M3UAMessageImpl() {
    
    }
    
    protected M3UAMessageImpl(int messageClass, int messageType) {
        this.messageClass = messageClass;
        this.messageType = messageType;
    }
    
    protected abstract void encodeParams(ByteBuffer buffer);
    
    public void encode(ByteBuffer buffer) {
        buffer.position(8);
        
        encodeParams(buffer);
        
        int length = buffer.position();
        buffer.rewind();
        
        buffer.put((byte)1);
        buffer.put((byte)0);
        buffer.put((byte)messageClass);
        buffer.put((byte)messageType);
        buffer.putInt(length);
        
        buffer.position(length);
    }
    
    protected void decode(byte[] data) {
        int pos = 0;
        while (pos < data.length) {
            short tag = (short)((data[pos] & 0xff) << 8 | (data[pos+1] & 0xff));
            short len = (short)((data[pos+2] & 0xff) << 8 | (data[pos+3] & 0xff));
            
            byte[] value = new byte[len - 4];
            
            System.arraycopy(data, pos + 4, value, 0, value.length);            
            pos+= len;
            parameters.put(tag, factory.createParameter(tag, value));            
        }
    }

    public int getMessageClass() {
        return messageClass;
    }

    public int getMessageType() {
        return messageType;
    }
}
