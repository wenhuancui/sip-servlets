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
