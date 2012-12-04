package org.mobicents.media.server.impl.resource.video;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * 
 * @author amit bhayani
 * 
 */
public class RTPMovieHintInformation extends Box {

	// File Type = rtp
	static byte[] TYPE = new byte[] { AsciiTable.ALPHA_r, AsciiTable.ALPHA_t, AsciiTable.ALPHA_p, AsciiTable.SPACE };
	static String TYPE_S = "rtp ";
	static {
		bytetoTypeMap.put(TYPE, TYPE_S);
	}

	private String sdpText;
	private String descriptionFormat;

	public RTPMovieHintInformation(long size) {
		super(size, TYPE_S);
	}

	@Override
	protected int load(DataInputStream fin) throws IOException {
		descriptionFormat = new String(this.read(fin));
		
		int lenOfSdp = (int) (this.getSize() - 12);
		byte[] sdpbytArr = new byte[lenOfSdp];
		fin.read(sdpbytArr, 0, (lenOfSdp));
		sdpText = new String(sdpbytArr, "UTF-8");	

		return (int) this.getSize();
	}

	public String getSdpText() {
		return sdpText;
	}

	public String getDescriptionFormat() {
		return descriptionFormat;
	}

}
