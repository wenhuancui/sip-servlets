package org.mobicents.media.server.impl.resource.video;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * <b> 8.10.2.1 Definition</b>
 * <ul>
 * <li>Box Type: ‘cprt’</li>
 * <li>Container: User data box (‘udta’)</li>
 * <li>Mandatory: No</li>
 * <li>Quantity: Zero or more</li>
 * </ul>
 * <p>
 * The Copyright box contains a copyright declaration which applies to the entire presentation, when contained within
 * the Movie Box, or, when contained in a track, to that entire track. There may be multiple copyright boxes using
 * different language codes.
 * </p>
 * 
 * @author amit bhayani
 * 
 */
public class CopyrightBox extends FullBox {

	// File Type = cprt
	static byte[] TYPE = new byte[] { AsciiTable.ALPHA_c, AsciiTable.ALPHA_p, AsciiTable.ALPHA_r, AsciiTable.ALPHA_t };
	static String TYPE_S = "cprt";
	static {
		bytetoTypeMap.put(TYPE, TYPE_S);
	}

	private String language;
	private String copyright;

	public CopyrightBox(long size) {
		super(size, TYPE_S);
	}

	@Override
	protected int load(DataInputStream fin) throws IOException {
		super.load(fin);

		return (int) this.getSize();
	}

}
