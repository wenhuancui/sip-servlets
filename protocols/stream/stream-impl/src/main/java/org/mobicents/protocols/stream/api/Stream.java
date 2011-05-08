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

package org.mobicents.protocols.stream.api;

import java.io.IOException;

/**
 * Interface which hides impl details of stream.
 * 
 * @author baranowb
 *
 */
public interface Stream {

    /**
     * Registers this stream with the given selector, returning a selection key.
     * This method first verifies that this channel is open and that the given initial 
     * interest set is valid.
     * 
     * If this stream is already registered with the given selector then the selection key 
     * representing that registration is returned after setting its interest set to the 
     * given value.
     * 
     * @param selector 
     * @param op The selector with which this channel is to be registered
     * @return
     */
    public SelectorKey register(StreamSelector selector) throws IOException;

    public int read(byte[] b) throws IOException;

    public int write(byte[] d) throws IOException;

    /**
     * Closes this streamer implementation. After closing stream its selectors are invalidated!
     */
    public void close();

    /**
     * Returns the provider that created this stream.
     * 
     * @return
     */
    public SelectorProvider provider();
}
