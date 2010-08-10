/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mobicents.protocols.link;

import java.nio.channels.SelectionKey;
import org.mobicents.protocols.stream.api.SelectorKey;
import org.mobicents.protocols.stream.api.Stream;
import org.mobicents.protocols.stream.api.StreamSelector;

/**
 *
 * @author kulikov
 */
public class SelectorKeyImpl implements SelectorKey {

    protected SelectionKey key;
    private Stream stream;
    private StreamSelector selector;
    
    protected boolean isReadable;
    protected boolean isWritable;
    
    private Object attachment;
    
    protected SelectorKeyImpl(SelectionKey key, Stream stream, StreamSelector selector) {
        this.key = key;
        this.stream = stream;
        this.selector = selector;
    }
    
    public boolean isValid() {
        return key.isValid();
    }

    public boolean isReadable() {
        return isReadable;
    }

    public boolean isWriteable() {
        return isWritable;
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
        this.attachment = obj;
    }

    public Object attachment() {
        return this.attachment;
    }

}
