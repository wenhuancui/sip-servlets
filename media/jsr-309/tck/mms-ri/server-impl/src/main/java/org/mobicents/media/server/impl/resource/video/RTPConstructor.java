package org.mobicents.media.server.impl.resource.video;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * 
 * @author amit bhayani
 *
 */
public abstract class RTPConstructor {
	private int constructorType;

	public RTPConstructor(int constructorType) {
		this.constructorType = constructorType;
	}

	public abstract int load(RandomAccessFile raAccFile) throws IOException;

	public int getConstructorType() {		
		return this.constructorType;
	}
	
	

}
