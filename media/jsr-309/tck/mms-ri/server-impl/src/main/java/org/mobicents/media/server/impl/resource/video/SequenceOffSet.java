package org.mobicents.media.server.impl.resource.video;

import java.io.DataInputStream;
import java.io.IOException;
/**
 * {@link RtpHintSampleEntry}
 * 
 * @author amit bhayani
 * 
 */
public class SequenceOffSet extends Box {
	
	// File Type = tsro
	static byte[] TYPE = new byte[] { AsciiTable.ALPHA_s, AsciiTable.ALPHA_n, AsciiTable.ALPHA_r, AsciiTable.ALPHA_o };
	static String TYPE_S = "snro";
	static {
		bytetoTypeMap.put(TYPE, TYPE_S);
	}
	
	private int offSet;

	public SequenceOffSet(long size) {
		super(size, TYPE_S);
	}

	@Override
	protected int load(DataInputStream fin) throws IOException {
		offSet = read32(fin);
		return (int)this.getSize();
	}

	public int getOffSet() {
		return offSet;
	}

}
