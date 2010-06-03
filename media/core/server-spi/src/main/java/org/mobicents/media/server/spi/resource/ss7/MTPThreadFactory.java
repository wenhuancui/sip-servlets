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
package org.mobicents.media.server.spi.resource.ss7;

import java.util.concurrent.ThreadFactory;


public class MTPThreadFactory implements ThreadFactory, EDU.oswego.cs.dl.util.concurrent.ThreadFactory{
	//seq, can be inacurate.
		private static long _seq;
	  	private String threadName;
	  	private int priority = Thread.NORM_PRIORITY;
	    private ThreadGroup factoryThreadGroup ;

	    
	    public MTPThreadFactory(String threadName, int priority) {
			super();
			this.threadName = threadName;
			this.priority = priority;
			this.factoryThreadGroup = new ThreadGroup("MMS_SS7_ThreadGroup");
		}


		public Thread newThread(Runnable r) {
	        Thread t = new Thread(this.factoryThreadGroup, r);
	        t.setPriority(this.priority);
	        t.setName("MTPThread["+threadName+"-"+(_seq++)+"]");
	        // ??
	        //t.start();
	        return t;
	    }
}
