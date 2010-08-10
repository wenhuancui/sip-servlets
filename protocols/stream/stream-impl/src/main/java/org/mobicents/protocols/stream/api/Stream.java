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
