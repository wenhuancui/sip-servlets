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

import java.util.List;

import org.mobicents.media.server.impl.resource.ss7.Mtp3Impl;
import org.mobicents.media.server.spi.resource.ss7.LinkSet;
import org.mobicents.media.server.spi.resource.ss7.Mtp3;
import org.mobicents.media.server.spi.resource.ss7.SS7Layer4;
import org.mobicents.media.server.spi.resource.ss7.factories.Mtp3Factory;

public class Mtp3FactoryImpl implements Mtp3Factory {

	public Mtp3 create(String name, List<LinkSet> linkSets, boolean l3Debug) {
		Mtp3Impl mtp3 = new Mtp3Impl(name);
		mtp3.setL3Debug(l3Debug);
		mtp3.setLinkSets(linkSets);
		return mtp3;
	}

	public Mtp3 create(String name, List<LinkSet> linkSets, SS7Layer4 layer4, boolean l3Debug) {
		Mtp3 mtp3 = this.create(name, linkSets, l3Debug);
		mtp3.setLayer4(layer4);
		return mtp3;
	}

}
