/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.mobicents.media.server.spi.dsp;

import java.io.Serializable;
import org.mobicents.media.Buffer;
import org.mobicents.media.Format;
import org.mobicents.media.format.AudioFormat;

/**
 * 
 * @author Oleg Kulikov
 */
public interface Codec extends Serializable {

	public final static AudioFormat PCMU = new AudioFormat(AudioFormat.ULAW, 8000, 8, 1);
	public final static AudioFormat PCMA = new AudioFormat(AudioFormat.ALAW, 8000, 8, 1);
	public final static AudioFormat SPEEX = new AudioFormat(AudioFormat.SPEEX, 8000, AudioFormat.NOT_SPECIFIED, 1);
	public final static AudioFormat G729 = new AudioFormat(AudioFormat.G729, 8000, AudioFormat.NOT_SPECIFIED, 1);
	public final static AudioFormat GSM = new AudioFormat(AudioFormat.GSM, 8000, AudioFormat.NOT_SPECIFIED, 1);
	public final static AudioFormat LINEAR_AUDIO = new AudioFormat(AudioFormat.LINEAR, 8000, 16, 1,
			AudioFormat.LITTLE_ENDIAN, AudioFormat.SIGNED);


	public Format getSupportedInputFormat();

	public Format getSupportedOutputFormat();

	/**
	 * Perform encoding/decoding procedure.
	 * 
	 * @param buffer
	 *            the media buffer.
	 */
	public void process(Buffer buffer);

}
