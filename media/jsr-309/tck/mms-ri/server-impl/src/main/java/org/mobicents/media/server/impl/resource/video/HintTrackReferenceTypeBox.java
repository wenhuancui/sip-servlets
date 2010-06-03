package org.mobicents.media.server.impl.resource.video;

/**
 * 
 * @author amit bhayani
 *
 */
public class HintTrackReferenceTypeBox extends TrackReferenceTypeBox {

	// File Type = hint
	static byte[] TYPE = new byte[] { AsciiTable.ALPHA_h, AsciiTable.ALPHA_i, AsciiTable.ALPHA_n, AsciiTable.ALPHA_t };
	static String TYPE_S = "hint";
	static {
		bytetoTypeMap.put(TYPE, TYPE_S);
	}

	public HintTrackReferenceTypeBox(long size) {
		super(size, TYPE_S);
	}

}
