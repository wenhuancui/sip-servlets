package org.mobicents.media.server.impl.resource.video;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * See {@link RtpHintSampleEntry}
 * @author amit bhayani
 * 
 */
public class TimeScaleEntry extends Box {

	// File Type = tims
	static byte[] TYPE = new byte[] { AsciiTable.ALPHA_t, AsciiTable.ALPHA_i, AsciiTable.ALPHA_m, AsciiTable.ALPHA_s };
	static String TYPE_S = "tims";
	static {
		bytetoTypeMap.put(TYPE, TYPE_S);
	}

	private int timeScale;

	public TimeScaleEntry(long size) {
		super(size, TYPE_S);
	}

	@Override
	protected int load(DataInputStream fin) throws IOException {
		timeScale = read32(fin);
		return (int) this.getSize();
	}

	public int getTimeScale() {
		return timeScale;
	}

}
