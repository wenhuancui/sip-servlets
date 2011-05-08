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

package org.mobicents.protocols.stream.impl;

import org.mobicents.protocols.stream.api.SelectorKey;
import org.mobicents.protocols.stream.api.Stream;
import org.mobicents.protocols.stream.api.StreamSelector;

public class SelectorKeyImpl implements SelectorKey {

    private boolean readable;
    private boolean writeable;
    private Stream stream;
    private StreamSelector streamSelector;
    private boolean valid = true;

    public SelectorKeyImpl(StreamSelector streamSelector, Stream stream) {
        super();
        this.stream = stream;
        this.streamSelector = streamSelector;
    }

    /**
     * @return the readable
     */
    public boolean isReadable() {
        return readable;
    }

    /**
     * @param readable the readable to set
     */
    void setReadable(boolean readable) {
        this.readable = readable;
    }

    /**
     * @return the writeable
     */
    public boolean isWriteable() {
        return writeable;
    }

    /**
     * @param writeable the writeable to set
     */
    void setWriteable(boolean writeable) {
        this.writeable = writeable;
    }

    /**
     * @return the stream
     */
    public Stream getStream() {
        return stream;
    }

    /**
     * @param stream the stream to set
     */
    void setStream(Stream stream) {
        this.stream = stream;
    }

    /**
     * @return the streamSelector
     */
    public StreamSelector getStreamSelector() {
        return streamSelector;
    }

    /**
     * @param streamSelector the streamSelector to set
     */
    void setStreamSelector(StreamSelector streamSelector) {
        this.streamSelector = streamSelector;
    }

    /**
     * @return the valid
     */
    public boolean isValid() {
        return valid;
    }

    void invalidate() {

        this.valid = false;
        this.streamSelector = null;
        this.stream = null;

    }

    public void cancel() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void attach(Object obj) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object attachment() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
