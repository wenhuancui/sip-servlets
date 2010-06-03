package org.mobicents.media.server.impl.resource.video;

import java.io.IOException;
import java.io.RandomAccessFile;

import org.mobicents.media.server.impl.resource.zap.Mtp2;





/**
 * 
 * @author amit bhayani
 *
 */
public class RTPImmediateConstructor extends RTPConstructor {

	public static final int TYPE = 1;

	private int count;
	private byte[] data;

	public RTPImmediateConstructor() {
		super(TYPE);
	}

	@Override
	public int load(RandomAccessFile raAccFile) throws IOException {
		// 1 is for Type + 1 is for count
		int bytesRead = 2;

		count = raAccFile.read();
		data = new byte[count];
		for (int i = 0; i < count; i++) {
			data[i] = raAccFile.readByte();
		}		
 
		bytesRead += count;

		if (bytesRead < 16) {
			// Each Constructor needs to be 16bytes.
			raAccFile.skipBytes(16 - bytesRead);

		}
		return 16;
	}

	public int getCount() {
		return count;
	}

	public byte[] getData() {
		return data;
	}

}
