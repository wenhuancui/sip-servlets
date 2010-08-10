/**
 * 
 */
package org.mobicents.protocols.stream.impl;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.mobicents.protocols.stream.api.SelectorKey;
import org.mobicents.protocols.stream.api.Stream;
import org.mobicents.protocols.stream.api.StreamSelector;

/**
 * 
 * Base class for stream selector
 * 
 * @author baranowb
 * 
 */
public class StreamSelectorImpl implements StreamSelector {

    protected Map<AbstractStream, SelectorKeyImpl> registeredStreams = new HashMap<AbstractStream, SelectorKeyImpl>();
    protected LinkedList<SelectorKey> ioReady = new LinkedList<SelectorKey>();
    protected Collection<SelectorKey> ioView = Collections.unmodifiableCollection(ioReady);
    protected int ops = 0;
    protected boolean closed = false;
    
    
    /**
     * Implements registartion.
     * 
     * @param stream
     * @param op
     * @return
     * @throws java.io.IOException
     */
    protected SelectorKey register(Stream stream, int op) throws IOException {
        return new SelectorKeyImpl(this, stream);
    }
    
    protected void unregister(Stream stream) {
        
    }
    
    /////////////////
    // OPS methods //
    /////////////////
	/*
     * (non-Javadoc)
     * 
     * @see org.mobicents.protocols.stream.api.StreamSelector#getOperations()
     */
    public int getOperations() {
        stateCheck();
        return ops;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mobicents.protocols.stream.api.StreamSelector#isReadOperation()
     */
    public boolean isReadOperation() {
        stateCheck();
        return (this.ops & OP_READ) > 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mobicents.protocols.stream.api.StreamSelector#isWriteOperation()
     */
    public boolean isWriteOperation() {
        stateCheck();
        return (this.ops & OP_WRITE) > 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mobicents.protocols.stream.api.StreamSelector#setOperation(int)
     */
    public void setOperation(int v) {
        stateCheck();
        this.ops = v & 0x03;

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.mobicents.protocols.stream.api.StreamSelector#getRegisteredStreams()
     */
    public Collection<Stream> getRegisteredStreams() {
        stateCheck();
        Collection c = this.registeredStreams.keySet();

        return Collections.unmodifiableCollection(c);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mobicents.protocols.stream.api.StreamSelector#isValid()
     */
    public boolean isClosed() {

        return this.closed;
    }

    public void close() {
        if (!isClosed()) {
            this.ioReady.clear();
            Iterator<AbstractStream> it = this.registeredStreams.keySet().iterator();
            while (it.hasNext()) {
                AbstractStream as = it.next();
                it.remove();
                as.doUnregister(this);
            }

            ops = 0;
            closed = true;
        }

    }

    /* (non-Javadoc)
     * @see org.mobicents.protocols.stream.api.StreamSelector#deregister(org.mobicents.protocols.stream.api.Stream)
     */
    public void deregister(Stream s) {
        stateCheck();

        if (this.registeredStreams.containsKey(s)) {
            AbstractStream as = (AbstractStream) s;
            as.doUnregister(this);
            SelectorKeyImpl key = this.registeredStreams.remove(s);
            if (this.ioReady.contains(key)) {
                this.ioReady.remove(key);
            }
            key.invalidate();
        } else {
            throw new IllegalArgumentException("Stream is not registered!");
        }


    }

    private void stateCheck() {
        if (isClosed()) {
            throw new IllegalStateException("Selector is already closed!");
        }

    }

    /* (non-Javadoc)
     * @see org.mobicents.protocols.stream.api.StreamSelector#register(org.mobicents.protocols.stream.api.Stream)
     */
    public void register(Stream s) {
        stateCheck();
        if (!this.registeredStreams.containsKey(s)) {
            AbstractStream as = (AbstractStream) s;
            as.doRegister(this);
            SelectorKeyImpl newKey = new SelectorKeyImpl(this, s);
            this.registeredStreams.put((AbstractStream) s, newKey);

        } else {
            throw new IllegalArgumentException("Stream has already been registered!");
        }

    }

    /* (non-Javadoc)
     * @see org.mobicents.protocols.stream.api.StreamSelector#selectNow()
     */
    public Collection<SelectorKey> selectNow() throws IOException {
        stateCheck();
        this.ioReady.clear();
        for (AbstractStream as : this.registeredStreams.keySet()) {
            try {
                as.impSelectNow();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return this.ioView;
    }

    ///////////////////////////
    // Local helper methods //
    //////////////////////////
    /**
     * Marks readines based on
     * 
     * @param v
     */
    void markOp(int v, AbstractStream as) {
        SelectorKeyImpl key = this.registeredStreams.get(as);
        if ((this.ops & v & OP_READ) > 0) {

            key.setReadable(true);
        } else {
        }

        if ((this.ops & v & OP_WRITE) > 0) {
            key.setWriteable(true);
        } else {
        }
        if ((key.isReadable() || key.isWriteable()) && !this.ioReady.contains(key)) {
            this.ioReady.add(key);
        }
    }

    public Collection<SelectorKey> selectNow(int operation, int timeout) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
