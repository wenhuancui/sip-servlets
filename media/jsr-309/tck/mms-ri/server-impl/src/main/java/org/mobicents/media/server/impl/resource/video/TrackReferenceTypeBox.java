package org.mobicents.media.server.impl.resource.video;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * 
 * @author amit bhayani
 * 
 */
public abstract class TrackReferenceTypeBox extends Box {

	private long[] trackIDs = null;

	public TrackReferenceTypeBox(long size, String type) {
		super(size, type);
	}

	@Override
	protected int load(DataInputStream fin) throws IOException {
		int noOfTracks = (int) (this.getSize() - 8) / 4;

		trackIDs = new long[noOfTracks];
		for (int i = 0; i < noOfTracks; i++) {
			trackIDs[i] = readU32(fin);

		}
		return (int) this.getSize();
	}

	public long[] getTrackIDs() {
		return trackIDs;
	}

}
