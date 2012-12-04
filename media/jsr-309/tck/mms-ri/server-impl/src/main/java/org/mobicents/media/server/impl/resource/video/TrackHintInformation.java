package org.mobicents.media.server.impl.resource.video;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * 
 * @author amit bhayani
 *
 */
public class TrackHintInformation extends Box {

	// File Type = hnti
	static byte[] TYPE = new byte[] { AsciiTable.ALPHA_h, AsciiTable.ALPHA_n, AsciiTable.ALPHA_t, AsciiTable.ALPHA_i };
	static String TYPE_S = "hnti";
	static {
		bytetoTypeMap.put(TYPE, TYPE_S);
	}

	private RTPTrackSdpHintInformation rtpTrackSdpHintInformation;

	public TrackHintInformation(long size) {
		super(size, TYPE_S);
	}

	@Override
	protected int load(DataInputStream fin) throws IOException {
		long len = readU32(fin);

		byte[] type = read(fin);
		if (comparebytes(type, RTPTrackSdpHintInformation.TYPE)) {
			rtpTrackSdpHintInformation = new RTPTrackSdpHintInformation(len);
			rtpTrackSdpHintInformation.load(fin);
		}
		return (int) this.getSize();
	}

	public RTPTrackSdpHintInformation getRtpTrackSdpHintInformation() {
		return rtpTrackSdpHintInformation;
	}

}
