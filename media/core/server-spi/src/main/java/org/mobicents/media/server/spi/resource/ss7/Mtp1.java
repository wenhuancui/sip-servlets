/*
 * JBoss, Home of Professional Open Source
 * Copyright XXXX, Red Hat Middleware LLC, and individual contributors as indicated
 * by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a full listing
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 */
package org.mobicents.media.server.spi.resource.ss7;

import java.io.IOException;

/**
 * @author baranowb
 * @author kulikov
 */
public interface Mtp1 {
    /**
     * Reads upto buffer.length bytes from layer 1.
     * 
     * @param buffer reader buffer
     * @return the number of actualy read bytes.
     */
    public int read(byte[] buffer) throws IOException;
    
    /**
     * Writes data to layer 1.
     * 
     * @param buffer the buffer containing data to write.
     * @param bytesRead 
     */
    public void write(byte[] buffer, int bytesRead) throws IOException;
    
    /**
     * Open message tranfer part layer 1.
     */
    public void open() throws IOException;
    
    /**
     * Close message tranfer part layer 1.
     */
    public void close();

    
}
