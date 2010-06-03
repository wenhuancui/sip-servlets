/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mobicents.media.server.impl.rtp.clock;

import org.mobicents.media.Format;
import org.mobicents.media.format.VideoFormat;
import org.mobicents.media.server.impl.rtp.RtpClock;

/**
 *
 * @author kulikov
 */
public class VideoClock extends RtpClock {

    private double rtpUnit;
    private double frameDuration;
    
    @Override
    public void setFormat(Format format) {
        super.setFormat(format);
        VideoFormat fmt = (VideoFormat) format;
        frameDuration = 1000 / fmt.getFrameRate();
        rtpUnit = (int)(fmt.getClockRate()/fmt.getFrameRate());
    }
    
    @Override
    public long getTime(long timestamp) {
        return (long) (timestamp/rtpUnit * frameDuration);
    }

    @Override
    public long getTimestamp(long time) {
        return (long) (time/frameDuration * rtpUnit);
    }

}
