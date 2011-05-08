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

package org.mobicents.media.server.impl.dsp.audio.gsm;

import org.apache.log4j.Logger;
import org.mobicents.media.Buffer;
import org.mobicents.media.Format;
import org.mobicents.media.server.spi.dsp.Codec;
import org.mobicents.media.server.spi.dsp.SignalingProcessor;
import org.tritonus.lowlevel.gsm.InvalidGSMFrameException;

/**
 * 
 * @author amit bhayani
 * 
 */
public class Decoder implements Codec {

    private transient Logger logger = Logger.getLogger(Decoder.class);
    private org.tritonus.lowlevel.gsm.GSMDecoder decoder = new org.tritonus.lowlevel.gsm.GSMDecoder();
    private static final int BUFFER_SIZE = 320;

    public Decoder() {
    }

    public void process(Buffer buffer) {
        byte[] data = buffer.getData();
        byte[] res = process(data);
        buffer.setData(res);
        buffer.setOffset(0);
        buffer.setLength(res.length);
        buffer.setFormat(Codec.LINEAR_AUDIO);
    }
    
    private byte[] m_abBuffer;

    /**
     * Perform compression.
     * 
     * @param input
     *            media
     * @return compressed media.
     */
    public byte[] process(byte[] media) {

        m_abBuffer = new byte[BUFFER_SIZE];

        try {
            decoder.decode(media, 0, m_abBuffer, 0, false);
        } catch (InvalidGSMFrameException e) {
            e.printStackTrace();
        }
        return m_abBuffer;
    }

    public Format getSupportedInputFormat() {
        return Codec.GSM;
    }

    public Format getSupportedOutputFormat() {
        return Codec.LINEAR_AUDIO;
    }

    public void setProc(SignalingProcessor processor) {
        // TODO Auto-generated method stub
    }
}
