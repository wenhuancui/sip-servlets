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
package org.mobicents.media.server.impl.resource.ss7;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.mobicents.media.server.spi.resource.ss7.Mtp1;
import org.mobicents.media.server.spi.resource.ss7.factories.MTP1Factory;

/**
 * This is factory for fake mtp1 tunnel = it creates mtp1impl in pairs,
 * connected as two way data pipe. 
 * 
 * @author baranowb
 * 
 */
public class TunnelMTP1Factory implements MTP1Factory {

	// used to associate tunels :)
	private TunneledMtp1Impl lastCreated;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.media.server.spi.resource.ss7.factories.MTP1Factory#create
	 * (java.lang.String, int, int)
	 */
	public Mtp1 create(String prefix, int span, int channel) {
		TunneledMtp1Impl local = new TunneledMtp1Impl(prefix, span, channel);
		if (lastCreated != null) {
			local.otherEnd = lastCreated;
			lastCreated.otherEnd = local;
//			local.synchronizationExecutor = lastCreated.synchronizationExecutor;
//			local.lock = lastCreated.lock;
//			synchronized (lastCreated.lock) {
//				lastCreated.lock.notify();
//			}
			lastCreated = null;
		} else {
			
			//ExecutorService synchronizationExecutor = Executors.newSingleThreadExecutor();
			lastCreated = local;
			//lastCreated.synchronizationExecutor = synchronizationExecutor;
			//lastCreated.lock = new Object();
		}
//		try {
//			local.open();
//		} catch (IOException e) {
//			throw new RuntimeException(e);
//		}
		
		return local;
	}

	private class DataBuffer {
		private byte[] data;
		private int dataCount;

		public void write(byte[] b, int count) {
			data = new byte[count];
			System.arraycopy(b, 0, data, 0, count);
			dataCount = count;
		}

		/**
		 * @return the data
		 */
		public byte[] readData() {
			if (dataCount > 0) {
				dataCount = 0;
				return data;
			}
			dataCount = 0;
			return new byte[0];
		}

		/**
		 * @return the dataCount
		 */
		public int getDataCount() {
			return dataCount;
		}

		/**
		 * @return the available
		 */
		public boolean isAvailable() {
			return dataCount > 0;
		}

		public void clear() {
			dataCount = 0;
		}

	}

	private class TunneledMtp1Impl implements Mtp1 {
		
		private TunneledMtp1Impl otherEnd;
		//private Thread worker;
		//private ExecutorService synchronizationExecutor;
		//private Object lock;
		private final DataBuffer dataBuffer = new DataBuffer();
		//private ExecHandler xx;
		private String prefix;
		private int span, channel;
		
		
		private TunneledMtp1Impl(String prefix, int span, int channel) {
			super();
			this.prefix = prefix;
			this.span = span;
			this.channel = channel;

		}

		public void close() {
			dataBuffer.clear();
		//	this.worker = null;
		}


		public void open() throws IOException {
			dataBuffer.clear();
			
		}

		public int read(byte[] buffer) throws IOException {
			if(otherEnd == null )
			{
				return 0;
			}
			if(otherEnd.dataBuffer.isAvailable())
			{
				int count = this.otherEnd.dataBuffer.getDataCount();
				byte[] b = this.otherEnd.dataBuffer.readData();
				System.arraycopy(b, 0, buffer, 0, count);
				return count;
				
			}
//			synchronized (lock) {
//
//				if(this.worker==null)
//				{
//					this.worker = Thread.currentThread();
//				}
//				if(otherEnd.worker == null)
//				{
//					try {
//						lock.wait();
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}else
//				{
//					//we can trigger
//					lock.notify();
//					//this.worker.suspend();
//				}
//			}
//			
//			if(this.otherEnd.dataBuffer.isAvailable())
//			{
//				int count = this.otherEnd.dataBuffer.getDataCount();
//				byte[] b = this.otherEnd.dataBuffer.readData();
//				System.arraycopy(b, 0, buffer, 0, count);
//				return count;
//			}
			//int readCount = xx.performRead(this,otherEnd,buffer);
			return 0;
		}


		public void write(byte[] buffer, int count) throws IOException {
			this.dataBuffer.write(buffer, count);
			//this.otherEnd.worker.resume();
			//this.worker.suspend();
			
		}

		private String toString = null;
		public String toString()
		{
			if(toString == null)
			{
				toString = "MTP1["+prefix+":"+span+":"+channel+"]";
			}
			return toString; 
		}

		

	}

}
