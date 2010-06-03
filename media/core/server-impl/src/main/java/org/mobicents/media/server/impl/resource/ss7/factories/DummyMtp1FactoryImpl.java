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

import java.io.IOException;

import org.mobicents.media.server.spi.resource.ss7.Mtp1;
import org.mobicents.media.server.spi.resource.ss7.factories.MTP1Factory;
/**
 * Simple dummy to allow test on microcontainer on win.
 * @author baranowb
 *
 */
public class DummyMtp1FactoryImpl implements MTP1Factory {

	public Mtp1 create(String prefix, int span, int channel) {
		return new Mtp1() {
			
			public void write(byte[] buffer, int bytesRead) throws IOException {
				// TODO Auto-generated method stub
				
			}
			
			public int read(byte[] buffer) throws IOException {
				// TODO Auto-generated method stub
				return 0;
			}
			
			public void open() throws IOException {
				// TODO Auto-generated method stub
				
			}
			
			public void close() {
				// TODO Auto-generated method stub
				
			}
		};
	}

}
