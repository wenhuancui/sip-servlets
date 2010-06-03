/*
 * Mobicents, Communications Middleware
 * 
 * Copyright (c) 2008, Red Hat Middleware LLC or third-party
 * contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Middleware LLC.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 *
 * Boston, MA  02110-1301  USA
 */
package org.mobicents.media.server.impl.resource.video;

/**
 * <b>8.4.5.5 Null Media Header Box</b>
 * <p>
 * Streams other than visual and audio (e.g., timed metadata streams) may use a null Media Header Box, as defined here.
 * </p>
 * 
 * @author kulikov
 */
public class NullMediaHeaderBox extends FullBox {

	// File Type = moov
	static byte[] TYPE = new byte[] { AsciiTable.ALPHA_n, AsciiTable.ALPHA_m, AsciiTable.ALPHA_h, AsciiTable.ALPHA_d };
	static String TYPE_S = "nmhd";
	static {
		bytetoTypeMap.put(TYPE, TYPE_S);
	}

	public NullMediaHeaderBox(long size) {
		super(size, TYPE_S);
	}

}
