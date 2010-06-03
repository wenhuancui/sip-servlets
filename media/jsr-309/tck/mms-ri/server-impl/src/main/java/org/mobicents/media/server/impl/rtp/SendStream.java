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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import org.mobicents.media.Buffer;
import org.mobicents.media.Format;
import org.mobicents.media.MediaSink;
import org.mobicents.media.MediaSource;
import org.mobicents.media.server.impl.AbstractSink;
import org.mobicents.media.server.impl.resource.dtmf.DtmfEvent;
import org.mobicents.media.server.impl.rtp.sdp.AVProfile;
import org.mobicents.media.server.spi.dsp.Codec;

/**
 * 
 * @author kulikov
 */
public class SendStream extends AbstractSink {

    // sequence number
    private int seq = 0;
    // source synchronization
    private final long ssrc = System.currentTimeMillis();
    private RtpSocket rtpSocket;
    protected RtpClock clock;
    private ArrayList<Format> formats = new ArrayList();
    protected byte mainstream;
    private byte dtmf;
    private long time;
    private Codec codec;
    private AVProfile formatConfig;
    private int eventDuration;
    private boolean endOfEvent;

    public SendStream(RtpSocket rtpSocket, AVProfile avProfile) {
        super("SendStream");
        this.rtpSocket = rtpSocket;
        this.formatConfig = avProfile;
        this.clock = rtpSocket.getClock();
    }

    @Override
    public void connect(MediaSource source) {
        if (this.rtpSocket.getFormat() == null) {
            throw new IllegalStateException("RTP has no negotiated formats");
        }
        super.connect(source);
    }

    public void setDtmf(int dtmf) {
        this.dtmf = (byte) dtmf;
    }
    
    @Override
    protected Format selectPreffered(Collection<Format> set) {
        for (Format f : set) {
            if (f.matches(rtpSocket.getFormat())) {
                codec = null;
                return f;
            }
        }

        for (Format f : set) {
            for (Codec c : rtpSocket.codecs) {
                if (f.matches(c.getSupportedInputFormat()) && 
                        c.getSupportedOutputFormat().matches(rtpSocket.getFormat())) {
                    codec = c;
                    return f;
                }
            }
        }

        //should never happen
        return null;
    }

    public void onMediaTransfer(Buffer buffer) throws IOException {
        if (buffer.getFlags() == Buffer.FLAG_RTP_BINARY) {
            rtpSocket.send(buffer.getData());
            return;
        }
        
        if (codec != null) {
            codec.process(buffer);
        }

        int timestamp = (int) clock.getTimestamp(buffer.getTimeStamp());
        RtpPacket packet = null;

        if (buffer.getHeader() != null && buffer.getHeader() instanceof DtmfEvent && dtmf > 0) {
            DtmfEvent evt = (DtmfEvent) buffer.getHeader();
            int digit = evt.getEventID();
            int volume = evt.getVolume();

            byte[] data = new byte[4];
            data[0] = (byte) digit;
            data[1] = endOfEvent ? (byte) (volume | 0x80) : (byte) (volume & 0x7f);

            data[2] = (byte) (eventDuration >> 8);
            data[3] = (byte) (eventDuration);

            eventDuration = eventDuration + 160;
            packet = new RtpPacket(dtmf, seq++, timestamp, ssrc,
                    data, 0, 4);        
        } else {
            packet = new RtpPacket(mainstream, seq++, timestamp, ssrc,
                    buffer.getData(), buffer.getOffset(), buffer.getLength());
        }
        rtpSocket.send(packet);

        if (logger.isDebugEnabled()) {
            logger.debug("Sending " + packet);
        }
    }

    /**
     * Resets this stream.
     */
    protected void reset() {
        mainstream = -1;
    }

    /**
     * (Non Java-doc.)
     * 
     * @see org.mobicents.media.MediaSink.isAcceptable(Format).
     */
    public boolean isAcceptable(Format fmt) {
        return true;
    }

    /**
     * Configures supported formats.
     * 
     * @param payloadID the payload number of format used by rtp socket
     * @param format the format used by rtp socket.
     */
    protected void setFormat(int payloadID, Format format) {
        this.mainstream = (byte) payloadID;
        if (format != Format.ANY) {
            clock.setFormat(format);
        }

        //supported formats are combination of
        //specified format and possible transcodings
        formats.clear();
        formats.add(format);

        //looking for possible transcodings
        for (Codec c : rtpSocket.codecs) {
            if (c.getSupportedOutputFormat().matches(format)) {
                formats.add(c.getSupportedInputFormat());
            }
        }
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
}
