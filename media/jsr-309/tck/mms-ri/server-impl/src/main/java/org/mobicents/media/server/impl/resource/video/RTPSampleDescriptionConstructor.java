package org.mobicents.media.server.impl.resource.video;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * 
 * @author amit bhayani
 * 
 */
public class RTPSampleDescriptionConstructor extends RTPConstructor {

	public static final int TYPE = 3;

	private int trackRefIndex;
	private int length;
	private long sampleDescIndex;
	private long sampleDescOffset;
	private long reserved;

	public RTPSampleDescriptionConstructor() {
		super(TYPE);
	}

	@Override
	public int load(RandomAccessFile raAccFile) throws IOException {
		trackRefIndex = raAccFile.read();
		length = (raAccFile.read() << 8) | raAccFile.read();
		sampleDescIndex = ((long) (raAccFile.read() << 24 | raAccFile.read() << 16 | raAccFile.read() << 8 | raAccFile
				.read())) & 0xFFFFFFFFL;
		sampleDescOffset = ((long) (raAccFile.read() << 24 | raAccFile.read() << 16 | raAccFile.read() << 8 | raAccFile
				.read())) & 0xFFFFFFFFL;
		reserved = ((long) (raAccFile.read() << 24 | raAccFile.read() << 16 | raAccFile.read() << 8 | raAccFile
				.read())) & 0xFFFFFFFFL;
		return 16;
	}

	public int getTrackRefIndex() {
		return trackRefIndex;
	}

	public int getLength() {
		return length;
	}

	public long getSampleDescIndex() {
		return sampleDescIndex;
	}

	public long getSampleDescOffset() {
		return sampleDescOffset;
	}

	public long getReserved() {
		return reserved;
	}

}
