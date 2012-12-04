package org.mobicents.media.server.impl.resource.video;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * 
 * @author amit bhayani
 *
 */
public class TimeToSampleBox extends FullBox {

	// File Type = stsd
	static byte[] TYPE = new byte[] { AsciiTable.ALPHA_s, AsciiTable.ALPHA_t, AsciiTable.ALPHA_t, AsciiTable.ALPHA_s };
	static String TYPE_S = "stts";
	static {
		bytetoTypeMap.put(TYPE, TYPE_S);
	}

	private long entryCount;
	private long[] sampleCount;
	private long[] sampleDelta;

	public TimeToSampleBox(long size) {
		super(size, TYPE_S);
	}

	@Override
	protected int load(DataInputStream fin) throws IOException {
		super.load(fin);

		entryCount = readU32(fin);

		sampleCount = new long[(int)entryCount];
		sampleDelta = new long[(int)entryCount];
		for (int i = 0; i < entryCount; i++) {
			sampleCount[i] = readU32(fin);
			sampleDelta[i] = readU32(fin);
		}

		return (int) this.getSize();
	}

	public long[] getSampleCount() {
		return sampleCount;
	}

	public long[] getSampleDelta() {
		return sampleDelta;
	}

	public long getEntryCount() {
		return entryCount;
	}

}
