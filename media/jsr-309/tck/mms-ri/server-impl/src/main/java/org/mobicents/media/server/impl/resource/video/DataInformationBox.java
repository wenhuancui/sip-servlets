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
package org.mobicents.media.server.impl.resource.video;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * <b>8.7.1.1 Definition</b>
 * <ul>
 * <li>Box Type: ‘dinf’ </li>
 * <li>Container: {@link MediaInformationBox} (‘minf’) or Meta Box (‘meta’)</li>
 * <li> Mandatory: Yes (required within ‘minf’ box) and No (optional within ‘meta’ box)</li>
 * <li> Quantity: Exactly one</li>
 * </ul>
 * The data information box contains objects that declare the location of the media information in a track.
 * 
 * @author kulikov
 * @author amit bhayani
 */
public class DataInformationBox extends Box {

	// File Type = dinf
	static byte[] TYPE = new byte[] { AsciiTable.ALPHA_d, AsciiTable.ALPHA_i, AsciiTable.ALPHA_n, AsciiTable.ALPHA_f };
	static String TYPE_S = "dinf";
	static {
		bytetoTypeMap.put(TYPE, TYPE_S);
	}

	private DataReferenceBox dataReferenceBox;

	public DataInformationBox(long size) {
		super(size, TYPE_S);
	}

	@Override
	protected int load(DataInputStream fin) throws IOException {
		long len = readU32(fin);
		byte[] type = read(fin);

		if (comparebytes(type, DataReferenceBox.TYPE)) {
			dataReferenceBox = new DataReferenceBox(len);
			dataReferenceBox.load(fin);
		} else {
			throw new IOException();
		}

		return (int) getSize();
	}

	public DataReferenceBox getDataReferenceBox() {
		return dataReferenceBox;
	}

}
