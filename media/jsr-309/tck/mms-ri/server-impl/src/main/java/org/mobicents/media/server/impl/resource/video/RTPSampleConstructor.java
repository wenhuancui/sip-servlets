package org.mobicents.media.server.impl.resource.video;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * 
 * @author amit bhayani
 * 
 */
public class RTPSampleConstructor extends RTPConstructor {

	public static final int TYPE = 2;

	private byte trackRefIndex;
	private int length;
	private long sampleNumber;
	private long sampleOffSet;
	private int bytesPerBlock = 1;
	private int samplesPerBlock = 1;

	public RTPSampleConstructor() {
		super(TYPE);
	}

	@Override
	public int load(RandomAccessFile raAccFile) throws IOException {
		trackRefIndex = raAccFile.readByte();
		length = (raAccFile.read() << 8) | raAccFile.read();
		sampleNumber = ((long) (raAccFile.read() << 24 | raAccFile.read() << 16 | raAccFile.read() << 8 | raAccFile
				.read())) & 0xFFFFFFFFL;

		sampleOffSet = ((long) (raAccFile.read() << 24 | raAccFile.read() << 16 | raAccFile.read() << 8 | raAccFile
				.read())) & 0xFFFFFFFFL;

		bytesPerBlock = (raAccFile.read() << 8) | raAccFile.read();

		samplesPerBlock = (raAccFile.read() << 8) | raAccFile.read();

		return 16;
	}

	public int getTrackRefIndex() {
		return trackRefIndex;
	}

	public int getLength() {
		return length;
	}

	public long getSampleNumber() {
		return sampleNumber;
	}

	public long getSampleOffSet() {
		return sampleOffSet;
	}

	public int getBytesPerBlock() {
		return bytesPerBlock;
	}

	public int getSamplesPerBlock() {
		return samplesPerBlock;
	}

}
