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
package org.mobicents.media.server.spi.resource.ss7.factories;

import org.mobicents.media.server.spi.resource.ss7.Mtp2;

/**
 * Factory for mtp2 layer.
 * 
 * @author baranowb
 * 
 */
public interface MTP2Factory {

	public void setMtp1Factory(MTP1Factory factory);

	public MTP1Factory getMtp1Factory();

	//FIXME: subservice possible should go into linkset ?
	public Mtp2 create(String prefix, int span, int channel, byte sls,
			int subservice, boolean enabledDebug, boolean enableSuTrace,
			boolean enableDataTrace, String names);
}
