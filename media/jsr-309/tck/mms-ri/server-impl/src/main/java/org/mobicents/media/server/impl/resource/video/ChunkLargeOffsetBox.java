package org.mobicents.media.server.impl.resource.video;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * @see ChunkOffsetBox
 * @author amit bhayani
 * 
 */
public class ChunkLargeOffsetBox extends FullBox {

	// File Type = co64
	static byte[] TYPE = new byte[] { AsciiTable.ALPHA_c, AsciiTable.ALPHA_o, AsciiTable.DIGIT_SIX,
			AsciiTable.DIGIT_FOUR };
	static String TYPE_S = "co64";
	static {
		bytetoTypeMap.put(TYPE, TYPE_S);
	}

	private long[] chunkOffset;

	public ChunkLargeOffsetBox(long size) {
		super(size, TYPE_S);
	}

	@Override
	protected int load(DataInputStream fin) throws IOException {
		super.load(fin);

		long entryCount = readU32(fin);
		chunkOffset = new long[(int)entryCount];
		for (int i = 0; i < entryCount; i++) {
			chunkOffset[i] = fin.readLong();
		}

		return (int) this.getSize();
	}

}
