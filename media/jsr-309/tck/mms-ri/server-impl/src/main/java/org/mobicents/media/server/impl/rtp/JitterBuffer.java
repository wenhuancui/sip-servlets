/*
 * Mobicents Media Gateway
 *
 * The source code contained in this file is in in the public domain.
 * It can be used in any project or product without prior permission,
 * license or royalty payments. There is  NO WARRANTY OF ANY KIND,
 * EXPRESS, IMPLIED OR STATUTORY, INCLUDING, WITHOUT LIMITATION,
 * THE IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE,
 * AND DATA ACCURACY.  We do not warrant or make any representations
 * regarding the use of the software or the  results thereof, including
 * but not limited to the correctness, accuracy, reliability or
 * usefulness of the software.
 */
package org.mobicents.media.server.impl.rtp;

import java.io.Serializable;
import org.apache.log4j.Logger;
import org.mobicents.media.Format;

/**
 * Implements jitter buffer.
 * 
 * A jitter buffer temporarily stores arriving packets in order to minimize
 * delay variations. If packets arrive too late then they are discarded. A
 * jitter buffer may be mis-configured and be either too large or too small.
 * 
 * If a jitter buffer is too small then an excessive number of packets may be
 * discarded, which can lead to call quality degradation. If a jitter buffer is
 * too large then the additional delay can lead to conversational difficulty.
 * 
 * A typical jitter buffer configuration is 30mS to 50mS in size. In the case of
 * an adaptive jitter buffer then the maximum size may be set to 100-200mS. Note
 * that if the jitter buffer size exceeds 100mS then the additional delay
 * introduced can lead to conversational difficulty.
 * 
 * @author Oleg Kulikov
 * @author amit bhayani
 */
public class JitterBuffer implements Serializable {

    private int period;
    private int jitter;
    private BufferConcurrentLinkedQueue<RtpPacket> queue = new BufferConcurrentLinkedQueue();
    private volatile boolean ready = false;
    
    private long duration;
    private long timestamp;
    private Format format;
    private RtpClock clock;
    private Logger logger = Logger.getLogger(JitterBuffer.class);
    private long delta;
    
    /**
     * Creates new instance of jitter.
     * 
     * @param fmt
     *            the format of the received media
     * @param jitter
     *            the size of the jitter in milliseconds.
     */
    public JitterBuffer(int jitter, int period) {
        this.period = period;
        this.jitter = jitter;
    }

    public void setClock(RtpClock clock) {
        this.clock = clock;
        if (format != null) {
            clock.setFormat(format);
        }
    }

    public void setFormat(Format format) {
        this.format = format;
        if (clock != null && format != Format.ANY) {
            clock.setFormat(format);
        }
    }

    public int getJitter() {
        return jitter;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public void write(RtpPacket rtpPacket) {
        long t = clock.getTime(rtpPacket.getTimestamp());

        //when first packet arrive and timestamp already known
        //we have to determine difference between rtp stream timestamps and local time
        if (delta == 0 && timestamp > 0) {
            delta = t - timestamp;
            timestamp += delta;
        }
        
        //if buffer's ready flag equals true then it means that reading 
        //starting and we should compare timestamp of arrived packet with time of
        //last reading.
        if (ready && t > timestamp + jitter) {
            //silentrly discard otstanding packet
            logger.warn("Packet " + rtpPacket + " is discarded by jitter buffer");
            return;
        }
    
        //if RTP packet is not outstanding or reading not started yet (ready == false)
        //queue packet.
        rtpPacket.setTime(t);
        queue.offer(rtpPacket);
        
        //allow read when buffer is full;
        duration += period;
        if (!ready && duration > (period + jitter)) {
            ready = true;
        }
    }

    public void reset() {
        queue.clear();
        duration = 0;
        clock.reset();
        delta = 0;
    }

    /**
     * 
     * @return
     */
    public RtpPacket read(long timestamp) {
        //discard buffer is buffer is not full yet
        if (!ready) {
            return null;
        }

        //remember timestamp
        this.timestamp = timestamp + delta;
        
        //if packet queue is empty (but was full) we have to returns
        //silence
        if (queue.isEmpty()) {
            return null;
        }

        //fill media buffer
        return queue.poll();
    }
}
