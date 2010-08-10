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
