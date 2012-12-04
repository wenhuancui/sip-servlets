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
 * <b>8.4.5.4 Hint Media Header Box</b>
 * <p>
 * The hint media header contains general information, independent of the protocol, for hint tracks. (A PDU is a
 * Protocol Data Unit.)
 * </p>
 * 
 * @author kulikov
 * @author amit bhayani
 */
public class HintMediaHeaderBox extends FullBox {

	// File Type = hmhd
	static byte[] TYPE = new byte[] { AsciiTable.ALPHA_h, AsciiTable.ALPHA_m, AsciiTable.ALPHA_h, AsciiTable.ALPHA_d };
	static String TYPE_S = "hmhd";
	static {
		bytetoTypeMap.put(TYPE, TYPE_S);
	}

	// maxPDUsize gives the size in bytes of the largest PDU in this (hint) stream
	private int maxPDUSize;

	// avgPDUsize gives the average size of a PDU over the entire presentation
	private int avgPDUSize;

	// maxbitrate gives the maximum rate in bits/second over any window of one second
	private long maxBitRate;

	// avgbitrate gives the average rate in bits/second over the entire presentation
	private long avgBitRate;

	public HintMediaHeaderBox(long size) {
		super(size, TYPE_S);
	}

	public long getAvgBitRate() {
		return avgBitRate;
	}

	public int getAvgPDUSize() {
		return avgPDUSize;
	}

	public long getMaxBitRate() {
		return maxBitRate;
	}

	public int getMaxPDUSize() {
		return maxPDUSize;
	}

	@Override
	protected int load(DataInputStream fin) throws IOException {
		super.load(fin);
		maxPDUSize = read16(fin);
		avgPDUSize = read16(fin);
		maxBitRate = readU32(fin);
		avgBitRate = readU32(fin);
		fin.readInt();
		return (int) getSize();
	}

}
