package org.mobicents.media.server.impl.resource.ss7;

import java.util.concurrent.ThreadFactory;


public class MTPThreadFactory implements ThreadFactory, EDU.oswego.cs.dl.util.concurrent.ThreadFactory{
	//seq, can be inacurate.
		private static long _seq;
	  	private String groupName;
	  	private int priority = Thread.NORM_PRIORITY;
	    private ThreadGroup factoryThreadGroup ;

	    
	    public MTPThreadFactory(String groupName, int priority) {
			super();
			this.groupName = groupName;
			this.priority = priority;
			this.factoryThreadGroup = new ThreadGroup("MMSClockThreadGroup[" + groupName + "]");
		}


		public Thread newThread(Runnable r) {
	        Thread t = new Thread(this.factoryThreadGroup, r);
	        t.setPriority(this.priority);
	        t.setName("MTPThread["+(_seq++)+"]");
	        // ??
	        //t.start();
	        return t;
	    }
}
