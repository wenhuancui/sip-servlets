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
 * <b>4.3.1 Definition<b>
 * <ul>
 * <li>Box Type: ‘ftyp’</li>
 * <li>Container: File</li>
 * <li>Mandatory: Yes</li>
 * <li>Quantity: Exactly one</li>
 * </ul>
 * 
 * @author kulikov
 * @author amit abhayani
 */
public class FileTypeBox extends Box {

	// File Type = ftyp
	static byte[] TYPE = new byte[] { AsciiTable.ALPHA_f, AsciiTable.ALPHA_t, AsciiTable.ALPHA_y, AsciiTable.ALPHA_p };
	static String TYPE_S = "ftyp";
	static {
		bytetoTypeMap.put(TYPE, TYPE_S);
	}

	private String majorBrand;
	private long minorVersion;
	private String[] compatibleBrands;

	public FileTypeBox(long size) {
		super(size, TYPE_S);
	}

	public String getMajorBrand() {
		return this.majorBrand;
	}

	public long getMinorVersion() {
		return this.minorVersion;
	}

	public String[] getCompatibleBrands() {
		return this.compatibleBrands;
	}

	@Override
	protected int load(DataInputStream fin) throws IOException {
		this.majorBrand = new String(read(fin));
		this.minorVersion = this.readU32(fin);

		long remainder = getSize() - 16;
		int count = (int) (remainder / 4);

		compatibleBrands = new String[count];
		for (int i = 0; i < count; i++) {
			compatibleBrands[i] = new String(read(fin));
		}
		return (int) getSize();
	}

	@Override
	public String toString() {
		StringBuffer b = new StringBuffer("FileTypeBox(ftyp)[majorBrand=").append(this.majorBrand).append(
				",minorVersion=").append(this.minorVersion).append(",compatibleBrands[");
		for (String s : this.compatibleBrands) {
			b.append(s);
			b.append(",");
		}
		b.append("]]");
		return b.toString();
	}
}
