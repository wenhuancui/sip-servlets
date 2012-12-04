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

package org.mobicents.protocols.stream.impl.tcp;

import java.nio.channels.SelectionKey;
import org.mobicents.protocols.stream.api.SelectorKey;
import org.mobicents.protocols.stream.api.Stream;
import org.mobicents.protocols.stream.api.StreamSelector;

/**
 *
 * @author kulikov
 */
public class TCPSelectorKey implements SelectorKey {
    
    private SelectionKey key;
    private Stream stream;
    private StreamSelector selector;
    
    protected TCPSelectorKey(StreamSelector selector, SelectionKey key, Stream stream) {
        this.selector = selector;
        this.key = key;
        this.stream = stream;
    }
    
    public boolean isValid() {
        return key.isValid();
    }

    public boolean isReadable() {
        return key.isReadable();
    }

    public boolean isWriteable() {
        return key.isWritable();
    }

    public Stream getStream() {
        return stream;
    }

    public StreamSelector getStreamSelector() {
        return selector;
    }

    public void cancel() {
        key.cancel();
    }

    public void attach(Object obj) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object attachment() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
 }
