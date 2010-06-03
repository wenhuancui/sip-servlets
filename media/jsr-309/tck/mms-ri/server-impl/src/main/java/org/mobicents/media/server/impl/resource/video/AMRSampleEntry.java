package org.mobicents.media.server.impl.resource.video;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Look 3Gpp TS 26.244 section 6.5
 * 
 * @author amit bhayani
 * 
 */
public class AMRSampleEntry extends AudioSampleEntry {

	// File Type = samr
	static byte[] TYPE = new byte[] { AsciiTable.ALPHA_s, AsciiTable.ALPHA_a, AsciiTable.ALPHA_m, AsciiTable.ALPHA_r };
	static String TYPE_S = "samr";
	static {
		bytetoTypeMap.put(TYPE, TYPE_S);
	}

	private AmrSpecificBox amrSpecificBox;

	public AMRSampleEntry(long size) {
		super(size, TYPE_S);
	}

	@Override
	protected int load(DataInputStream fin) throws IOException {
		int count = super.load(fin);
		if (count < getSize()) {
			long len = readU32(fin);
			String type = readType(fin);
			if (type.equals("damr")) {
				amrSpecificBox = new AmrSpecificBox(len, type);
				count += amrSpecificBox.load(fin);
			} else {
				throw new IOException("Unknown box=" + new String(type) + "parent = AudioSampleEntry");
			}
		}
		return count;
	}

	public AmrSpecificBox getAmrSpecificBox() {
		return amrSpecificBox;
	}

}
