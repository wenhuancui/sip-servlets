/**
 * 
 */
package org.mobicents.protocols.stream.impl;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.mobicents.protocols.stream.api.SelectorKey;
import org.mobicents.protocols.stream.api.Stream;
import org.mobicents.protocols.stream.api.StreamSelector;

/**
 * Base class for stream impl.
 * @author baranowb
 *
 */
public abstract class AbstractStream implements Stream {

    protected List<StreamSelector> selectors = new LinkedList<StreamSelector>();

    public SelectorKey register(StreamSelector selector, int op) throws IOException {
        return ((StreamSelectorImpl)selector).register(this, op);
    }

    protected void doRegister(StreamSelector selector) {
        selectors.add(selector);
    }

    protected void doUnregister(StreamSelector selector) {
        selectors.remove(selector);
    }

    protected abstract void impSelectNow() throws IOException;

    protected void markOp(int op, AbstractStream abstractStream, StreamSelector sel) {
        ((StreamSelectorImpl) sel).markOp(op, abstractStream);

    }
}
