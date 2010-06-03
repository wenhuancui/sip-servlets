package org.mobicents.media.server.impl.resource.video;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * 
 * @author amit bhayani
 *
 */
public class PixelAspectRatioBox extends Box {
	
	static byte[] TYPE = new byte[] { AsciiTable.ALPHA_p, AsciiTable.ALPHA_a, AsciiTable.ALPHA_s, AsciiTable.ALPHA_p };
	static String TYPE_S = "pasp";
	static {
		bytetoTypeMap.put(TYPE, TYPE_S);
	}
	
	private long hSpacing;
	private long vSpacing;

	public PixelAspectRatioBox(long size) {
		super(size, TYPE_S);
	}

	@Override
	protected int load(DataInputStream fin) throws IOException {		
		int count = 8;
		
		hSpacing = readU32(fin);
		vSpacing = readU32(fin);
		return (int)this.getSize();
	}

	public long getHSpacing() {
		return hSpacing;
	}

	public long getVSpacing() {
		return vSpacing;
	}

}
