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
package org.mobicents.media.server.impl.rtp;

import java.util.ArrayList;
import org.mobicents.media.Buffer;
import org.mobicents.media.Format;
import org.mobicents.media.MediaSink;
import org.mobicents.media.server.impl.AbstractSource;
import org.mobicents.media.server.impl.rtp.rfc2833.DtmfConverter;
import org.mobicents.media.server.impl.rtp.sdp.AVProfile;
import org.mobicents.media.server.spi.dsp.Codec;

/**
 * 
 * @author Oleg Kulikov
 */
public class ReceiveStream extends AbstractSource {

    private RtpSocket rtpSocket;
    private JitterBuffer jitterBuffer;
    protected int mainstream;
    private int dtmf;
    private ArrayList<Format> formats = new ArrayList();
    private AVProfile avProfile;
    private Codec codec;
    private DtmfConverter dtmfConverter = new DtmfConverter();
    /** Creates a new instance of ReceiveStream */
    public ReceiveStream(RtpSocket rtpSocket, int jitter, AVProfile formatConfig) {
        super("ReceiveStream");
        this.avProfile = formatConfig;
        this.rtpSocket = rtpSocket;
        //synchronize stream from socket's timer
        setSyncSource(rtpSocket.timer);

        //construct jitter buffer
        jitterBuffer = new JitterBuffer(jitter, getPeriod());
        jitterBuffer.setClock(rtpSocket.getClock());
    }

    @Override
    public void setPeriod(int period) {
        super.setPeriod(period);
        jitterBuffer.setPeriod(period);
    }

    /**
     * Processes received RTP packet.
     * 
     * @param rtpPacket packet for processing
     */
    protected void process(RtpPacket rtpPacket) {
        if (logger.isDebugEnabled()) {
            logger.debug("Receive " + rtpPacket);
        }
        jitterBuffer.write(rtpPacket);
    }

    @Override
    public void beforeStart() {
        jitterBuffer.reset();
    }

    @Override
    public void connect(MediaSink sink) {
        if (this.rtpSocket.getFormat() == null) {
            throw new IllegalStateException("RTP has no negotiated formats");
        }
        super.connect(sink);
    }

    public void setDtmf(int dtmf) {
        this.dtmf = dtmf;
    }
    
    /**
     * Configures supported formats.
     * 
     * @param payloadID the payload number of format used by rtp socket
     * @param format the format used by rtp socket.
     */
    protected void setFormat(int payloadID, Format format) {
        this.mainstream = payloadID;
        jitterBuffer.setFormat(format);

        //supported formats are combination of
        //specified format and possible transcodings
        formats.clear();
        formats.add(format);

        //looking for possible transcodings
        for (Codec c : rtpSocket.codecs) {
            if (c.getSupportedInputFormat().matches(format)) {
                formats.add(c.getSupportedOutputFormat());
            }
        }
        
        this.dtmfConverter.setPreffered(format);
    }

    /**
     * (Non Java-doc.)
     * 
     * @see org.mobicents.media.MediaSink#getFormats() 
     */
    public Format[] getFormats() {
        //if RTP socket is not configured send stream can 
        //not send something
        if (rtpSocket.getFormat() == null) {
            return new Format[0];
        }

        //return supported formats
        Format[] fmts = new Format[formats.size()];
        formats.toArray(fmts);

        return fmts;
    }

    @Override
    public void setPreffered(Format format) {
        super.setPreffered(format);
        if (format == null) {
            return;
        }
        
        //if preffred format matches to socket's format then no need 
        //in transcoding
        if (format.matches(rtpSocket.getFormat())) {
            codec = null;
            return;
        }

        //transcoding required. selecting suitable codec
        //NOTE! at least one suitable codec exist because components  
        //has just comleted analysis of supported formats!
        for (Codec c : rtpSocket.codecs) {
            if (c.getSupportedOutputFormat().matches(format)) {
                codec = c;
                return;
            }
        }
    }

    public void evolve(Buffer buffer, long timestamp, long seq) {
        RtpPacket packet = jitterBuffer.read(timestamp);
        if (packet == null) {
            buffer.setFlags(Buffer.FLAG_SILENCE);
            return;
        }
        if (packet.getPayloadType() == mainstream) {
            byte[] data = packet.getPayload();
        
            buffer.setData(data);
            buffer.setLength(data.length);
            buffer.setOffset(0);
            buffer.setFormat(format);
            buffer.setDuration(getPeriod());
            buffer.setFlags(0);
        } else if (packet.getPayloadType() == dtmf) {
            dtmfConverter.process(packet, getPeriod(), buffer);
        }
        
        
        if (codec != null && buffer.getFlags() != Buffer.FLAG_SILENCE) {
            codec.process(buffer);
        }
        
        buffer.setSequenceNumber(seq);
        buffer.setTimeStamp(packet.getTime());
        
    }

    /**
     * Resets this stream.
     */
    protected void reset() {
        mainstream = -1;
        formats.clear();
    }
    
    
}
