package org.mobicents.media.server.impl.resource.video;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * 
 * @author amit bhayani
 *
 */
public class RTPNoOpConstructor extends RTPConstructor {

	public static final int TYPE = 0;

	public RTPNoOpConstructor() {
		super(TYPE);
	}

	@Override
	public int load(RandomAccessFile raAccFile) throws IOException {
		// 1 is for Type
		int count = 15;
		while (count != 0) {
			// TODO : do you want to keep paded data?
			count -= raAccFile.skipBytes(count);
		}
		return 16;
	}
}
