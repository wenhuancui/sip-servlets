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
package org.mobicents.media.server.impl.resource.zap;

import java.io.File;
import java.util.Map;

import org.mobicents.protocols.ss7.mtp.Mtp1;
import org.mobicents.protocols.ss7.mtp.Mtp2;

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
        
    public Channel() {
    }
    
    public void setLink(Mtp2 link) {
	this.link = link;
    }
    
    public Mtp2 getLink() {
	return link;
    }
    
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
    public int read(byte[] buffer) {
	return readData(fd, buffer);
    }
    
    public native int readData(int fd, byte[] buffer);
    /**
     * Writes specified data to the pipe.
     *
     * @param buffer the buffer with data to write
     * @param len the length of the buffer.
     */
    public void write(byte[] buffer, int len) {
	writeData(fd, buffer, len);
    }
    
    public native void writeData(int fd, byte[] buffer, int len);
    
    public void close() {
	closeChannel(fd);
    }
    
    /**
     * Closes this pipe.
     */
    public native void closeChannel(int fd);
    
    public String toString() {
	return Integer.toString(channelID);
    }
}