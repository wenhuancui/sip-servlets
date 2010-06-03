package org.mobicents.media.server.spi.resource;

import org.mobicents.media.Component;
import org.mobicents.media.server.spi.MultimediaSource;

/**
 * 
 * @author amit bhayani
 *
 */
public interface Player extends Component, MultimediaSource {
    public void setURL(String url);
    public String getURL();
    public void setSSRC(String media, long ssrc);
    public void setRtpTime(String media, long rtpTime);
    public double getNPT(String media);
}
