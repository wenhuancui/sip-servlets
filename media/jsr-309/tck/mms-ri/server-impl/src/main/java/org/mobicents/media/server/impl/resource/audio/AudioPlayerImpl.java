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
package org.mobicents.media.server.impl.resource.audio;

import java.io.IOException;
import java.net.URL;

import java.util.ArrayList;
import java.util.Collection;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioFormat.Encoding;

import org.mobicents.media.Buffer;
import org.mobicents.media.Format;
import org.mobicents.media.MediaSource;
import org.mobicents.media.format.AudioFormat;
import org.mobicents.media.server.impl.AbstractSource;
import org.mobicents.media.server.impl.rtp.sdp.AVProfile;
import org.mobicents.media.server.spi.Timer;
import org.mobicents.media.server.spi.dsp.Codec;
import org.mobicents.media.server.spi.dsp.CodecFactory;
import org.mobicents.media.server.spi.events.NotifyEvent;
import org.mobicents.media.server.spi.resource.Player;
import org.xiph.speex.spi.SpeexAudioFileReader;
import org.xiph.speex.spi.SpeexEncoding;

/**
 * 
 * @author Oleg Kulikov
 */
public class AudioPlayerImpl extends AbstractSource implements Player, Runnable {

    private final static ArrayList<String> mediaTypes = new ArrayList();
    static {
        mediaTypes.add("audio");
    }
    
    /** supported formats definition */
    private final static Format[] FORMATS = new Format[]{
        AVProfile.L16_MONO,
        AVProfile.L16_STEREO,
        AVProfile.PCMA,
        AVProfile.PCMU,
        AVProfile.SPEEX,
        AVProfile.GSM,
        Codec.LINEAR_AUDIO
    };
    /** GSM Encoding constant used by Java Sound API */
    private final static Encoding GSM_ENCODING = new Encoding("GSM0610");
    /** audio stream */
    private transient AudioInputStream stream = null;
    /** Name (path) of the file to play */
    private String file;
    /** Flag indicating end of media */
    private volatile boolean eom = false;
    /** The countor for errors occured during processing */
    private int frameSize;

    private Codec codec;
    
    private final static ArrayList<CodecFactory> codecFactories = new ArrayList();
    static {
        codecFactories.add(new org.mobicents.media.server.impl.dsp.audio.g711.alaw.DecoderFactory());
        codecFactories.add(new org.mobicents.media.server.impl.dsp.audio.g711.alaw.EncoderFactory());

        codecFactories.add(new org.mobicents.media.server.impl.dsp.audio.g711.ulaw.DecoderFactory());
        codecFactories.add(new org.mobicents.media.server.impl.dsp.audio.g711.ulaw.EncoderFactory());

        codecFactories.add(new org.mobicents.media.server.impl.dsp.audio.gsm.DecoderFactory());
        codecFactories.add(new org.mobicents.media.server.impl.dsp.audio.gsm.EncoderFactory());

        codecFactories.add(new org.mobicents.media.server.impl.dsp.audio.speex.DecoderFactory());
        codecFactories.add(new org.mobicents.media.server.impl.dsp.audio.speex.EncoderFactory());

        codecFactories.add(new org.mobicents.media.server.impl.dsp.audio.g729.DecoderFactory());
        codecFactories.add(new org.mobicents.media.server.impl.dsp.audio.g729.EncoderFactory());
    }
    
    private Codec selectCodec(Format f) {
        for (CodecFactory factory : codecFactories) {
            if (factory.getSupportedInputFormat().matches(f) &&
                    factory.getSupportedOutputFormat().matches(format)) {
                return factory.getCodec();
            }
        }
        return null;
    }
    
    /**
     * Creates new instance of the Audio player.
     * 
     * @param name the name of the AudioPlayer to be created.
     * @param timer source of synchronization.
     */
    public AudioPlayerImpl(String name, Timer timer) {
        super(name);
        setSyncSource(timer);
    }

    /**
     * (Non Java-doc.)
     * 
     * @see org.mobicents.media.server.spi.resource.AudioPlayer#setURL(java.lang.String) 
     */
    public void setURL(String url) {
        this.file = url;
    }

    /**
     * (Non Java-doc.)
     * 
     * @see org.mobicents.media.server.spi.resource.AudioPlayer#getURL() 
     */
    public String getURL() {
        return file;
    }

    @Override
    public void beforeStart() throws Exception {
        closeAudioStream();
        if (file.endsWith("spx")) {
            stream = new SpeexAudioFileReader().getAudioInputStream(new URL(file));
        } else {
            stream = AudioSystem.getAudioInputStream(new URL(file));
        }

        Format f = getFormat(stream);
        if (f == null) {
            throw new IOException("Unsupported format: " + stream.getFormat());
        }
        
        if (!f.matches(this.format)) {
            codec = this.selectCodec(f);
            if (codec == null) {
                throw new IOException("Unsupported format: " + stream.getFormat());
            }
        }
        format.setFrameRate(this.getFrameRate());
        frameSize = this.getPacketSize(getPeriod(), (AudioFormat)f);
        
//        setFormat(format);


        eom = false;
    }

    @Override
    public void afterStop() {
        closeAudioStream();
    }

    /**
     * Gets the format of specified stream.
     * 
     * @param stream
     *            the stream to obtain format.
     * @return the format object.
     */
    private AudioFormat getFormat(AudioInputStream stream) {
        Encoding encoding = stream.getFormat().getEncoding();
        if (encoding == Encoding.ALAW) {
            return (AudioFormat) AVProfile.PCMA;
        } else if (encoding == Encoding.ULAW) {
            return (AudioFormat) AVProfile.PCMU;
        } else if (encoding == SpeexEncoding.SPEEX) {
            return (AudioFormat) AVProfile.SPEEX;
        } else if (encoding.equals(GSM_ENCODING)) {
            return (AudioFormat) AVProfile.GSM;
        } else if (encoding == Encoding.PCM_SIGNED) {
            int sampleSize = stream.getFormat().getSampleSizeInBits();
            if (sampleSize != 16) {
                return null;
            }
            int sampleRate = (int) stream.getFormat().getSampleRate();
            if (sampleRate == 44100) {
                int channels = stream.getFormat().getChannels();
                return channels == 1 ? (AudioFormat) AVProfile.L16_MONO : (AudioFormat) AVProfile.L16_STEREO;
            } else if (sampleRate == 8000) {
                return Codec.LINEAR_AUDIO;
            } else {
                return null;
            }
        }
        return null;
    }

    /**
     * Calculates size of packets for the currently opened stream.
     * 
     * @return the size of packets in bytes;
     */
    private int getPacketSize(double packetDuration, AudioFormat fmt) {
        int pSize = (int) (packetDuration * fmt.getChannels() * fmt.getSampleSizeInBits() * fmt.getSampleRate() / 8000);
        if (pSize < 0) {
            // For Format for which bit is AudioFormat.NOT_SPECIFIED, 160 is
            // passed
            pSize = 160;
            if (format == AVProfile.GSM) {
                //For GSM the RTP Packet size is 33
                pSize = (int) (33 * (packetDuration / 20));
            }
        }
        return pSize;
    }

    /**
     * Reads packet from currently opened stream.
     * 
     * @param packet
     *            the packet to read
     * @param offset
     *            the offset from which new data will be inserted
     * @return the number of actualy read bytes.
     * @throws java.io.IOException
     */
    private int readPacket(byte[] packet, int offset, int psize) throws IOException {
        int length = 0;
        try {
            while (length < psize) {
                int len = stream.read(packet, offset + length, psize - length);
                if (len == -1) {
                    return length;
                }
                length += len;
            }
            return length;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return length;
    }

    private void padding(byte[] data, int count) {
        int offset = data.length - count;
        for (int i = 0; i < count; i++) {
            data[i + offset] = 0;
        }
    }

    /**
     * Closes audio stream
     */
    private void closeAudioStream() {
        try {
            if (stream != null) {
                stream.close();
            }
        } catch (IOException e) {
        }
    }

    @Override
    public void evolve(Buffer buffer, long timestamp, long seq) {
        byte[] data = new byte[frameSize];
        buffer.setData(data);
        try {
            int len = readPacket(data, 0, frameSize);
            if (len == 0) {
                eom = true;
            }

            if (len < frameSize) {
                padding(data, frameSize - len);
            }

        } catch (IOException e) {
            failed(NotifyEvent.TX_FAILED, e);
            return;
        }

        buffer.setTimeStamp(timestamp);
        buffer.setOffset(0);
        buffer.setLength(frameSize);
        buffer.setEOM(eom);
        buffer.setSequenceNumber(seq);
        buffer.setDuration(getPeriod());
        if (codec != null) {
            codec.process(buffer);
        }
    }

    public Format[] getFormats() {
        return FORMATS;
    }

    public Collection<String> getMediaTypes() {
        return mediaTypes;
    }

    public MediaSource getMediaSource(String media) {
        if (mediaTypes.equals("audio")) {
            return this;
        }
        return null;
    }

	public void setSSRC(String media, long ssrc) {
		// TODO Auto-generated method stub
		
	}

	public void setRtpTime(String media, long rtpTime) {
		// TODO Auto-generated method stub
		
	}

	public double getNPT(String media) {
		// TODO Auto-generated method stub
		return 0;
	}
}
