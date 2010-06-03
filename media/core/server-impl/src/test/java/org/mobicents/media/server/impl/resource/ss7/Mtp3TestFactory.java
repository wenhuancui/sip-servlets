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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.mobicents.media.server.spi.resource.ss7.LinkSet;
import org.mobicents.media.server.spi.resource.ss7.MTPThreadFactory;
import org.mobicents.media.server.spi.resource.ss7.Mtp2;
import org.mobicents.media.server.spi.resource.ss7.Mtp3;
import org.mobicents.media.server.spi.resource.ss7.SS7Layer4;
import org.mobicents.media.server.spi.resource.ss7.Utils;
import org.mobicents.media.server.spi.resource.ss7.factories.Mtp3Factory;

/**
 * Fake factory to provide extension.
 * @author baranowb
 *
 */
public class Mtp3TestFactory implements Mtp3Factory {

	//possibly would be better to hack some executor service, but....
	private final ExecutorService testExecutor = Executors.newSingleThreadExecutor(new MTPThreadFactory("TestThreads",Thread.MAX_PRIORITY));
	private Future f;
	private int count;
	private TestRunner runner = new TestRunner();
	/* (non-Javadoc)
	 * @see org.mobicents.media.server.spi.resource.ss7.factories.Mtp3Factory#create(java.lang.String, java.util.List, boolean)
	 */
	public Mtp3 create(String name, List<LinkSet> linkSets, boolean l3Debug) {
		Mtp3TestImpl mtp3 = new Mtp3TestImpl(name);
		mtp3.setL3Debug(l3Debug);
		mtp3.setLinkSets(linkSets);
		return mtp3;
	}

	public Mtp3 create(String name, List<LinkSet> linkSets, SS7Layer4 layer4, boolean l3Debug) {
		Mtp3 mtp3 = this.create(name, linkSets, l3Debug);
		mtp3.setLayer4(layer4);
		return mtp3;
	}

	private class  Mtp3TestImpl extends Mtp3Impl
	{
	
		public Mtp3TestImpl(String name) {
			super(name);
			// TODO Auto-generated constructor stub
		}

		/* (non-Javadoc)
		 * @see org.mobicents.media.server.impl.resource.ss7.Mtp3Impl#run()
		 */
		@Override
		public void run() {
			try {
				long thisTickStamp = System.currentTimeMillis();

				for(LinkSet ls: this.linkSets)
				{
					ls.threadTick(thisTickStamp);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		/* (non-Javadoc)
		 * @see org.mobicents.media.server.impl.resource.ss7.Mtp3Impl#start()
		 */
		@Override
		public void start() throws IOException {
			synchronized (testExecutor) {
				if(f == null)
				{
					runner.started = true;
					count++;
					runner.toRuns.add(this);
					f=testExecutor.submit(runner);
					
				}else
				{
					count++;
					runner.toRuns.add(this);
				}
				Utils.getInstance().startDebug();

				for(LinkSet ls: this.linkSets)
				{
					for (Mtp2 mtp2 : ls.getLinks()) {
					
						mtp2._startLink();
					
					}
				}
				
				manageLinkSet();
			}
			
			started = true;
		}

		
		@Override
		public void stop() {
			synchronized (testExecutor) {
				count--;
				runner.toRuns.remove(this);
				if(count==0)
				{
					runner.started = false;
					f.cancel(false);
					f = null;
				}
				for(LinkSet ls: this.linkSets)
				{
					for(Mtp2 mtp2:ls.getLinks())
					{
						mtp2._stopLink();
					}
				}
			}
			
		}
		
	}
	private class TestRunner implements Runnable
	{
		private boolean started = true;
		private List<Mtp3TestImpl> toRuns = new ArrayList<Mtp3TestImpl>();
		public void run() {
			while (started) {
				try{
					
					for(int i = 0;i<toRuns.size(); i++)
					{
						toRuns.get(i).run();
					}
				}catch(java.lang.IndexOutOfBoundsException e)
				{
					
				
				}catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			
		}
		
	}
 
}
