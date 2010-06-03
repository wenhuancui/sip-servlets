package org.mobicents.media.server.impl.resource.video;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * <b>8.7.5.1 Definition</b>
 * <ul>
 * </li>
 * Box Type: ‘stco’, ‘co64’</li>
 * <li>Container: Sample Table Box (‘stbl’)</li>
 * <li>Mandatory: Yes</li>
 * <li>Quantity: Exactly one variant must be present</li>
 * </ul>
 * <p>
 * The chunk offset table gives the index of each chunk into the containing file. There are two variants, permitting the
 * use of 32-bit or 64-bit offsets. The latter is useful when managing very large presentations. At most one of these
 * variants will occur in any single instance of a sample table.
 * </p>
 * <p>
 * Offsets are file offsets, not the offset into any box within the file (e.g. Media Data Box). This permits referring
 * to media data in files without any box structure. It does also mean that care must be taken when constructing a
 * self-contained ISO file with its metadata (Movie Box) at the front, as the size of the Movie Box will affect the
 * chunk offsets to the media data.
 * </p>
 * 
 * @author amit bhayani
 * 
 */
public class ChunkOffsetBox extends FullBox {

	// File Type = stsd
	static byte[] TYPE = new byte[] { AsciiTable.ALPHA_s, AsciiTable.ALPHA_t, AsciiTable.ALPHA_c, AsciiTable.ALPHA_o };
	static String TYPE_S = "stco";
	static {
		bytetoTypeMap.put(TYPE, TYPE_S);
	}

	private long[] chunkOffset;

	public ChunkOffsetBox(long size) {
		super(size, TYPE_S);
	}

	@Override
	protected int load(DataInputStream fin) throws IOException {
		super.load(fin);

		long entryCount = readU32(fin);
		chunkOffset = new long[(int)entryCount];
		for (int i = 0; i < entryCount; i++) {
			chunkOffset[i] = this.readU32(fin);
		}

		return (int) this.getSize();

	}

	public long[] getChunkOffset() {
		return chunkOffset;
	}

}
