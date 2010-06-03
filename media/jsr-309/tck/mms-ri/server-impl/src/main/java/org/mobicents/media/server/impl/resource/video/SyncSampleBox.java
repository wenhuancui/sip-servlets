package org.mobicents.media.server.impl.resource.video;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * <b>8.6.2.1 Definition</b>
 * <ul>
 * <li>Box Type: ‘stss’</li>
 * <li>Container: {@link SampleTableBox} (‘stbl’)</li>
 * <li>Mandatory: No</li>
 * <li>Quantity: Zero or one</li>
 * </ul>
 * <p>
 * This box provides a compact marking of the random access points within the stream. The table is arranged in strictly
 * increasing order of sample number.
 * </p>
 * <p>
 * If the sync sample box is not present, every sample is a random access point.
 * </p>
 * 
 * @author amit bhayani
 * 
 */
public class SyncSampleBox extends FullBox {

	// File Type = stss
	static byte[] TYPE = new byte[] { AsciiTable.ALPHA_s, AsciiTable.ALPHA_t, AsciiTable.ALPHA_s, AsciiTable.ALPHA_s };
	static String TYPE_S = "stss";
	static {
		bytetoTypeMap.put(TYPE, TYPE_S);
	}

	private long[] sampleNumber;

	public SyncSampleBox(long size) {
		super(size, TYPE_S);
	}

	@Override
	protected int load(DataInputStream fin) throws IOException {
		int count = 8;
		count += super.load(fin);

		long entryCount = readU32(fin);
		sampleNumber = new long[(int) entryCount];
		for (int i = 0; i < entryCount; i++) {
			sampleNumber[i] = readU32(fin);
		}

		return (int) (count + (entryCount * 4) + 4);
	}

	public long[] getSampleNumber() {
		return sampleNumber;
	}

}
