package org.mobicents.media.server.impl.resource.video;
/**
 * 
 * @author amit bhayani
 *
 */
public class CdscTrackReferenceTypeBox extends TrackReferenceTypeBox {
	
	// File Type = cdsc
	static byte[] TYPE = new byte[] { AsciiTable.ALPHA_c, AsciiTable.ALPHA_d, AsciiTable.ALPHA_s, AsciiTable.ALPHA_c };
	static String TYPE_S = "cdsc";
	static {
		bytetoTypeMap.put(TYPE, TYPE_S);
	}

	public CdscTrackReferenceTypeBox(long size) {
		super(size, TYPE_S);
	}

}
