package org.mobicents.media.server.impl.resource.video;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * We don't know the type of box but we keep content as byte[]
 * 
 * @author amit bhayani
 * 
 */
public class UndefinedBox extends Box {

	public UndefinedBox(long size, String type) {
		super(size, type);
	}

	byte[] rawData = null;

	@Override
	protected int load(DataInputStream fin) throws IOException {
		int length = (int) (this.getSize() - 8);
		rawData = new byte[length];
		fin.read(rawData, 0, length);
		return (int) this.getSize();
	}

}
