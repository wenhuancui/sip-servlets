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
package org.mobicents.media.server.impl.rtp.sdp;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Set;
import org.mobicents.media.Format;
import org.mobicents.media.format.AudioFormat;
import org.mobicents.media.format.VideoFormat;

/**
 * Defines relation between audio/video format and RTP payload number as
 * specified by Audio/Video Profile spec.
 * 
 * @author Oleg Kulikov
 */
public class AVProfile implements Cloneable {

    public final static String AUDIO = "audio";
    public final static String VIDEO = "video";
    
    public final static AudioFormat PCMU = new AudioFormat(AudioFormat.ULAW, 8000, 8, 1);
    public final static AudioFormat PCMA = new AudioFormat(AudioFormat.ALAW, 8000, 8, 1);
    public final static AudioFormat SPEEX = new AudioFormat(AudioFormat.SPEEX, 8000, AudioFormat.NOT_SPECIFIED, 1);
    public final static AudioFormat G729 =  new AudioFormat(AudioFormat.G729, 8000, AudioFormat.NOT_SPECIFIED, 1);
    public final static AudioFormat GSM =  new AudioFormat(AudioFormat.GSM, 8000, AudioFormat.NOT_SPECIFIED, 1);
    public final static AudioFormat MPEG4_GENERIC = new AudioFormat("mpeg4-generic", 8000, AudioFormat.NOT_SPECIFIED, 2);
    public final static AudioFormat L16_STEREO =  
            new AudioFormat(AudioFormat.LINEAR, 44100, 16, 2, AudioFormat.LITTLE_ENDIAN, AudioFormat.SIGNED);
    public final static AudioFormat L16_MONO =  
            new AudioFormat(AudioFormat.LINEAR, 44100, 16, 1, AudioFormat.LITTLE_ENDIAN, AudioFormat.SIGNED);
    public final static AudioFormat DTMF =  new AudioFormat("telephone-event", 8000, AudioFormat.NOT_SPECIFIED, AudioFormat.NOT_SPECIFIED);
    
    public final static VideoFormat H261 =  new VideoFormat(VideoFormat.H261, 25, 90000);
    public final static VideoFormat MP4V =  new VideoFormat("MP4V-ES", 25, 90000);
    
    public final static AudioFormat AMR =  new AudioFormat(AudioFormat.AMR, 8000, AudioFormat.NOT_SPECIFIED, 1);
    
    private final HashMap<Integer, AudioFormat> audio = new HashMap();
    private final HashMap<Integer, VideoFormat> video = new HashMap();
    
    
    public AVProfile() {
        audio.put(0, PCMU);
        audio.put(8, PCMA);
        audio.put(97, SPEEX);
        audio.put(2, G729);
        audio.put(3, GSM);
        audio.put(16, L16_STEREO);
        audio.put(17, L16_MONO);        
        audio.put(101, DTMF);    
        audio.put(99, AMR); 
        video.put(45, H261);
    }
    
    public void setProfile(Hashtable<Integer, Format> profile) {
        Set<Integer> keys = profile.keySet();
        for (Integer key: keys) {
            Format f = profile.get(key);
            if (f instanceof AudioFormat) {
                audio.put(key,(AudioFormat) f);
            } else if (f instanceof VideoFormat) {
                video.put(key,(VideoFormat) f);
            }
        }
    }
    
    public Hashtable<Integer, Format> getProfile() {
        Hashtable<Integer, Format> profile = new Hashtable();
        profile.putAll(audio);
        profile.putAll(video);
        return profile;
    }
    
    public HashMap<Integer, AudioFormat> getAudioFormats() {
        return audio;
    }
    
    public HashMap<Integer, VideoFormat> getVideoFormats() {
        return video;
    }
    
    /**
     * Gets the audio format related to payload type.
     * 
     * @param pt the payload type
     * @return AudioFormat object.
     */
    public AudioFormat getAudioFormat(int pt) {
        return audio.get(pt);
    }

    /**
     * Gets the video format related to payload type.
     * 
     * @param pt the payload type
     * @return VideoFormat object.
     */
    public VideoFormat getVideoFormat(int pt) {
        return video.get(pt);
    }
    
    @Override
    public AVProfile clone() {
        AVProfile profile = new AVProfile();
        profile.setProfile(this.getProfile());
        return profile;
    }
}
