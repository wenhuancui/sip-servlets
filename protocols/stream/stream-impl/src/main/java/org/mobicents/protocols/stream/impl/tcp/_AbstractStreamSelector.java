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

package org.mobicents.protocols.stream.impl.tcp;

import java.io.IOException;

import org.mobicents.protocols.stream.api.StreamSelector;
import org.mobicents.protocols.stream.api.Stream;

abstract class _AbstractStreamSelector implements StreamSelector {

//	protected int ops = 0;
//	protected AbstractTCPStream streamer;
//	protected boolean readable = false;
//	protected boolean writeable = false;
//
//	
//	
//	protected _AbstractStreamSelector() {
//		super();
//		// TODO Auto-generated constructor stub
//	}
//	
//	protected _AbstractStreamSelector(AbstractTCPStream streamer) {
//		super();
//		this.streamer = streamer;
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see org.mobicents.protocols.stream.api.StreamSelector#getOperations()
//	 */
//	public int getOperations() {
//
//		return ops;
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see org.mobicents.protocols.stream.api.StreamSelector#getStream()
//	 */
//	public Stream getStream() {
//		// TODO Auto-generated method stub
//		return streamer;
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see org.mobicents.protocols.stream.api.StreamSelector#isValid()
//	 */
//	public boolean isValid() {
//		// TODO Auto-generated method stub
//		return streamer != null;
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see org.mobicents.protocols.stream.api.StreamSelector#setOperation(int)
//	 */
//	public void setOperation(int v) {
//
//		this.ops = v & 0x03;
//
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see org.mobicents.protocols.stream.api.StreamSelector#isReadOperation()
//	 */
//	public boolean isReadOperation() {
//		// TODO Auto-generated method stub
//		return readable;
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see org.mobicents.protocols.stream.api.StreamSelector#isWriteOperation()
//	 */
//	public boolean isWriteOperation() {
//		// TODO Auto-generated method stub
//		return writeable;
//	}
//
//	/**
//	 * Marks readines based on
//	 * 
//	 * @param v
//	 */
//	void markOp(int v) {
//		if ((this.ops & v & OP_READ) > 0) {
//			this.readable = true;
//		} else {
//			readable = false;
//		}
//
//		if ((this.ops & v & OP_WRITE) > 0) {
//			this.writeable = true;
//		} else {
//			writeable = false;
//		}
//	}
//
//	public void close() {
//		if (isValid()) {
//			streamer.selectors.remove(this);
//			ops = 0;
//			streamer = null;
//			readable = false;
//			writeable = false;
//		}
//	}
//
//	/* (non-Javadoc)
//	 * @see org.mobicents.protocols.stream.api.StreamSelector#select()
//	 */
//	public boolean select() throws IOException {
//		
//		throw new UnsupportedOperationException();
//	}
//
//	/* (non-Javadoc)
//	 * @see org.mobicents.protocols.stream.api.StreamSelector#select(long)
//	 */
//	public boolean select(long timeout) throws IOException {
//		throw new UnsupportedOperationException();
//		
//	}
//
//	/* (non-Javadoc)
//	 * @see org.mobicents.protocols.stream.api.StreamSelector#selectNow()
//	 */
//	public boolean selectNow() throws IOException {
//		if(isValid())
//		{
//			return this.streamer.implSelectNow();
//		}else
//		{
//			return false;
//		}
//		
//	}

}
