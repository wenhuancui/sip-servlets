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

package org.mobicents.media.server.impl.resource.zap;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.mobicents.protocols.ss7.mtp.Mtp1;
import org.mobicents.protocols.ss7.mtp.Mtp2;
import org.mobicents.protocols.stream.api.SelectorKey;
import org.mobicents.protocols.stream.api.SelectorProvider;
import org.mobicents.protocols.stream.api.StreamSelector;

public class Channel implements Mtp1 {

    private final static String MMS_HOME = "MMS_HOME";
    private final static String LIB_NAME = "zap-native-linux.so";
    
    static {
	try {
	    Map<String, String> env = System.getenv();
	    if (env.get(MMS_HOME) != null) {
		String path = env.get(MMS_HOME) + File.separator + "native" + File.separator + LIB_NAME;
		System.load(path);
		System.out.println("Loaded library " + path);
	    } else {
		System.out.println("Can not load library");
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    private int span;    
    private int channelID;
    private int code;
    private String linkName;
    
    protected int fd;

    private Mtp2 link;
    
    protected SelectorKey selectorKey;
    
    public Channel() {
    }
    
//    public void setLink(Mtp2 link) {
//	this.link = link;
//    }
    
//    public Mtp2 getLink() {
//	return link;
//    }
    
    public int getSpan() {
	return span;
    }
    	
    public void setSpan(int span) {
	this.span = span;
    }
    
    public int getChannelID() {
	return channelID;
    }
    
    public void setChannelID(int channelID) {
	this.channelID = channelID;
    }
    
    public void setCode(int code) {
	this.code = code;
    }
    
    public int getCode() {
	return code;
    }
    
    public void setLinkName(String linkName) {
	this.linkName = linkName;
    }
    
    public String getLinkName() {
	return linkName;
    }
    
    public void open() {
	int zapid = 31 * (span - 1) + channelID;
	fd = openChannel(zapid);
    }
    
    /**
     * Opens this channel and prepares it for reading.
     *
     *
     * @param path the path to the device.
     */
    public native int openChannel(int id);
    
    /**
     * Reads data from this pipe.
     *
     * @param buffer the byte buffer to read data.
     * @return the number of bytes actualy read.
     */
    public int read(byte[] buffer) throws IOException {
	return readData(fd, buffer);
    }
    
    public native int readData(int fd, byte[] buffer);
    /**
     * Writes specified data to the pipe.
     *
     * @param buffer the buffer with data to write
     * @param len the length of the buffer.
     */
    public int write(byte[] buffer) throws IOException {
	writeData(fd, buffer, buffer.length);
        return 0;
    }
    
    public native void writeData(int fd, byte[] buffer, int len);
    
    /**
     * Registers pipe for polling.
     *
     *@param fd the file descriptor.
     */
    public native void doRegister(int fd);
    
    /**
     * Unregisters pipe from polling.
     *
     * @param fd the file descriptor.
     */ 
    public native void doUnregister(int fd);
    
    public void close() {
	closeChannel(fd);
    }
    
    /**
     * Closes this pipe.
     */
    public native void closeChannel(int fd);
    
    @Override
    public String toString() {
	return Integer.toString(channelID);
    }

    public void setLink(Mtp2 link) {
        this.link = link;
    }

    public Mtp2 getLink() {
        return this.link;
    }

    protected void doRegister(StreamSelector selector) {
        doRegister(fd);
    }

    protected void doUnregister(StreamSelector selector) {
        doUnregister(fd);
    }
    

    public boolean isReadable() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isWriteable() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public SelectorProvider provider() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public SelectorKey register(StreamSelector selector) throws IOException {
        return ((Selector)selector).register(this);
    }

    public void write(byte[] data, int len) throws IOException {
        this.writeData(fd, data, len);
    }

}