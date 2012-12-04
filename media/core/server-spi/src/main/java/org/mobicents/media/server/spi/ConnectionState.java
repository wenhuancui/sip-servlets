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

package org.mobicents.media.server.spi;
/**
 *
 * @author amit bhayani
 */
public enum ConnectionState {

    NULL(0, "NULL"), IDLE(1, "IDLE"), HALF_OPEN(2, "HALF_OPEN"), OPEN(3, "OPEN"), CLOSED(4, "CLOSED");

    private int code;
    private String stateName;
    
    private ConnectionState(int code, String stateName) {
        this.stateName = stateName;
        this.code = code;
    }
    
    public static ConnectionState getInstance(String name) {
        if (name.equalsIgnoreCase("NULL")) {
            return NULL;
        } else if(name.equalsIgnoreCase("IDLE")){
            return IDLE;
        } else if(name.equalsIgnoreCase("HALF_OPEN")){
            return HALF_OPEN;
        } else if(name.equalsIgnoreCase("OPEN")){
            return OPEN;
        } else if(name.equalsIgnoreCase("CLOSED")){
            return CLOSED;
        } else{
        	throw new IllegalArgumentException("There is no media type for: "+name);
        }
    }    
    
    @Override
    public String toString() {
        return stateName;
    }
    
    public int getCode() {
        return code;
    }
}
