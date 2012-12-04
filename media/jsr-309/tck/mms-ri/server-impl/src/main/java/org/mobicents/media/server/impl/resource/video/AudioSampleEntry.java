package org.mobicents.media.server.impl.resource.video;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * 
 * @author amit bhayani
 * 
 */
public abstract class AudioSampleEntry extends SampleEntry {

	// ChannelCount is either 1 (mono) or 2 (stereo)
	private int channelCount;

	// SampleSize is in bits, and takes the default value of 16
	private int sampleSize = 16;

	// SampleRate is the sampling rate expressed as a 16.16 fixed-point number (hi.lo)
	private double sampleRate;

	public AudioSampleEntry(long size, String type) {
		super(size, type);
	}

	@Override
	protected int load(DataInputStream fin) throws IOException {
		super.load(fin);

		// int[2] reserved
		fin.skip(8);

		channelCount = read16(fin);
		sampleSize = read16(fin);

		fin.skip(2);

		// reserved
		fin.skip(2);

		sampleRate = readFixedPoint1616(fin);//(a >> 16) + (a & 0xffff) / 10;

		int count = 28 + 8;

		return count;
	}

	public int getChannelCount() {
		return channelCount;
	}

	public int getSampleSize() {
		return sampleSize;
	}

	public double getSampleRate() {
		return sampleRate;
	}

}
