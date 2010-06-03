/*
 * Mobicents, Communications Middleware
 * 
 * Copyright (c) 2008, Red Hat Middleware LLC or third-party
 * contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Middleware LLC.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 *
 * Boston, MA  02110-1301  USA
 */
package org.mobicents.media.server.impl.resource.zap;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.apache.log4j.Logger;
import org.mobicents.media.server.impl.resource.ss7.Mtp1;
import org.mobicents.media.server.impl.resource.ss7.Mtp2;
import org.mobicents.media.server.impl.resource.ss7.Mtp3;

/**
 * 
 * @author kulikov
 * @author baranowb
 */
public class ZapLink implements Mtp1 {

	private String linkset;
	private int span;
	private int channel;
	private int zapID;
	private Schannel nativeChannel;
	private String linkName;

	private static final Logger logger = Logger.getLogger(ZapLink.class);

	// FIXME: its a bit akward, we create from lower layer, on CR this should be
	// reversed...
	public ZapLink() {
		this.nativeChannel = new Schannel();
	}

	public ZapLink(String linkset, int span, int channel) {
		this();
		this.linkset = linkset;
		this.span = span;
		this.channel = channel;

	}

	public int getChannel() {
		return channel;
	}

	public int getSpan() {
		return span;
	}

	public String getLinkset() {
		return linkset;
	}

	public void setChannel(int channel) {
		this.channel = channel;
	}

	public void setLinkset(String linkset) {
		this.linkset = linkset;
	}

	
	public void setSpan(int span) {
		this.span = span;
	}


	public int read(byte[] bb) throws IOException {

		// return is.read(bb);
		return this.nativeChannel.read(bb);

	}

	public void write(byte[] bb, int numberOfBytes) throws IOException {
		// os.write(bb);
		this.nativeChannel.write(bb, numberOfBytes);
	}

	public void open() throws IOException {

		this.zapID = 31 * this.span - 31 + this.channel;
		int fd = this.nativeChannel.open(this.zapID);

		logger.info("Opened channel " + channel + " on span " + span + ". File: " + fd);
		if(fd <0)
		{
			throw new IOException("Failed to create native pipe.");
		}

	}

	public void close() {

		this.nativeChannel.close();
	}

	public void start() {
		this.linkName = "link[" + linkset + ":" + span + ":" + channel + "]";
		logger.info("=================================================");
		logger.info("Starting L1: " + linkName);
		logger.info("=================================================");
		

	}

	public void stop() {
		logger.info("Stopping L1: " + linkName);
	}
     
}
