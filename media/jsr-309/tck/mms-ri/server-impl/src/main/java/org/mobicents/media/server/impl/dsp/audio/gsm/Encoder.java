package org.mobicents.media.server.impl.dsp.audio.gsm;

import org.mobicents.media.Buffer;
import org.mobicents.media.Format;
import org.mobicents.media.server.spi.dsp.Codec;
import org.mobicents.media.server.spi.dsp.SignalingProcessor;

/**
 * 
 * @author amit bhayani
 * 
 */
public class Encoder implements Codec {

    private org.tritonus.lowlevel.gsm.Encoder encoder = new org.tritonus.lowlevel.gsm.Encoder();
    private short[] signal = new short[160];
    private byte[] res = new byte[33];

    public void process(Buffer buffer) {
        if (buffer.getLength() != 320) {
            buffer.setFlags(Buffer.FLAG_BUF_UNDERFLOWN);
            return;
        }

        // encode into short values

            byte[] data = buffer.getData();

            // int k = 0;
            for (int i = 0; i < 160; i++) {
                // signal[i] = (short) ((data[k++] << 8) & (data[k++]));
                signal[i] = bytesToShort16(data, i * 2, false);
            }

            res = new byte[33];
            encoder.encode(signal, res);
            buffer.setData(res);
            buffer.setOffset(0);
            buffer.setLength(res.length);
        //buffer.setData(res);
        buffer.setFormat(Codec.GSM);
    }

    private short bytesToShort16(byte[] buffer, int byteOffset, boolean bigEndian) {
        return bigEndian ? ((short) ((buffer[byteOffset] << 8) | (buffer[byteOffset + 1] & 0xFF)))
                : ((short) ((buffer[byteOffset + 1] << 8) | (buffer[byteOffset] & 0xFF)));
    }

    public Format getSupportedInputFormat() {
        return Codec.LINEAR_AUDIO;
    }

    public Format getSupportedOutputFormat() {
        return Codec.GSM;
    }

    public void setProc(SignalingProcessor processor) {
        // TODO Auto-generated method stub
    }
}
