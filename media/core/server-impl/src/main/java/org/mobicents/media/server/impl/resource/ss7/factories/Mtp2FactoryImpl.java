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
package org.mobicents.media.server.impl.resource.ss7.factories;

import org.mobicents.media.server.impl.resource.ss7.Mtp2Impl;
import org.mobicents.media.server.spi.resource.ss7.Mtp1;
import org.mobicents.media.server.spi.resource.ss7.Mtp2;
import org.mobicents.media.server.spi.resource.ss7.factories.MTP1Factory;
import org.mobicents.media.server.spi.resource.ss7.factories.MTP2Factory;

/**
 * @author baranowb
 * 
 */
public class Mtp2FactoryImpl implements MTP2Factory {

	private MTP1Factory mtp1Factory;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.media.server.impl.resource.ss7.factories.MTP2Factory#create
	 * (java.lang.String, int, int, int, int, boolean, boolean, boolean)
	 */
	public Mtp2 create(String prefix, int span, int channel, byte sls,
			int subservice, boolean enabledDebug, boolean enableSuTrace,
			boolean enableDataTrace, String name) {

		Mtp1 mtp1 = mtp1Factory.create(prefix, span, channel);
		Mtp2 mtp2 = new Mtp2Impl(name, sls, subservice);

		mtp2.setLayer1(mtp1);
		mtp2.setL2Debug(enabledDebug);
		mtp2.setEnableDataTrace(enableDataTrace);
		mtp2.setEnableSuTrace(enableSuTrace);

		return mtp2;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.mobicents.media.server.impl.resource.ss7.factories.MTP2Factory#
	 * getMtp1Factory()
	 */
	public MTP1Factory getMtp1Factory() {

		return this.mtp1Factory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.mobicents.media.server.impl.resource.ss7.factories.MTP2Factory#
	 * setMtp1Factory
	 * (org.mobicents.media.server.impl.resource.ss7.factories.MTP1Factory)
	 */
	public void setMtp1Factory(MTP1Factory factory) {
		this.mtp1Factory = factory;

	}



}
