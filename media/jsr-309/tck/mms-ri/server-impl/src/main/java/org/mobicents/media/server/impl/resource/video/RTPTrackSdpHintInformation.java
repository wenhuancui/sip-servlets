package org.mobicents.media.server.impl.resource.video;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * 
 * @author amit bhayani
 *
 */
public class RTPTrackSdpHintInformation extends Box {

	// File Type = sdp
	static byte[] TYPE = new byte[] { AsciiTable.ALPHA_s, AsciiTable.ALPHA_d, AsciiTable.ALPHA_p, AsciiTable.SPACE };
	static String TYPE_S = "sdp ";
	static {
		bytetoTypeMap.put(TYPE, TYPE_S);
	}

	private String sdpText;

	public RTPTrackSdpHintInformation(long size) {
		super(size, TYPE_S);

	}

	@Override
	protected int load(DataInputStream fin) throws IOException {
		int lenOfSdp = (int) (this.getSize() - 8);
		byte[] sdpbytArr = new byte[lenOfSdp];
		fin.read(sdpbytArr, 0, (lenOfSdp));
		sdpText = new String(sdpbytArr, "UTF-8");

		return (int) this.getSize();
	}

	public String getSdpText() {
		return sdpText;
	}

}
