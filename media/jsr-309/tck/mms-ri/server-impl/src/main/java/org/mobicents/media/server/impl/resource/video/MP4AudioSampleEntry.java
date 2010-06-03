package org.mobicents.media.server.impl.resource.video;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Look 3Gpp TS 26.244 section 6.4
 * 
 * @author amit bhayani
 * 
 */
public class MP4AudioSampleEntry extends AudioSampleEntry {

	// File Type = mp4a
	static byte[] TYPE = new byte[] { AsciiTable.ALPHA_m, AsciiTable.ALPHA_p, AsciiTable.DIGIT_FOUR, AsciiTable.ALPHA_a };
	static String TYPE_S = "mp4a";
	static {
		bytetoTypeMap.put(TYPE, TYPE_S);
	}

	private ESDBox esdBox = null;

	public MP4AudioSampleEntry(long size) {
		super(size, TYPE_S);
	}

	@Override
	protected int load(DataInputStream fin) throws IOException {
		int count = super.load(fin);
		if (count < getSize()) {
			long len = readU32(fin);
			byte[] type = read(fin);

			if (comparebytes(type, ESDBox.TYPE)) {
				esdBox = new ESDBox(len);
				count += esdBox.load(fin);
			} else {
				throw new IOException("Unknown box=" + new String(type) + "parent = AudioSampleEntry");
			}
		}
		return count;
	}

	public ESDBox getEsdBox() {
		return esdBox;
	}

}
