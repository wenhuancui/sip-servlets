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

package org.mobicents.media.server.impl.resource.mediaplayer.audio;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import org.mobicents.media.Buffer;
import org.mobicents.media.Format;
import org.mobicents.media.format.AudioFormat;
import org.mobicents.media.server.Utils;
import org.mobicents.media.server.impl.AbstractSource;
import org.mobicents.media.server.impl.resource.mediaplayer.Track;
import org.mobicents.media.server.impl.resource.mediaplayer.audio.gsm.GsmTrackImpl;
import org.mobicents.media.server.impl.resource.mediaplayer.audio.mpeg.AMRTrackImpl;
import org.mobicents.media.server.impl.resource.mediaplayer.audio.tts.TtsTrackImpl;
import org.mobicents.media.server.impl.resource.mediaplayer.audio.tts.VoicesCache;
import org.mobicents.media.server.impl.resource.mediaplayer.audio.wav.WavTrackImpl;
import org.mobicents.media.server.spi.ResourceUnavailableException;
import org.mobicents.media.server.spi.dsp.Codec;
import org.mobicents.media.server.spi.dsp.CodecFactory;
import org.mobicents.media.server.spi.events.NotifyEvent;
import org.mobicents.media.server.spi.resource.Player;
import org.mobicents.media.server.spi.resource.TTSEngine;
import org.mobicents.media.server.spi.rtp.AVProfile;

/**
 * @author baranowb
 * @author Oleg Kulikov
 */
public class AudioPlayerImpl extends AbstractSource implements Player, TTSEngine {

    private final static AudioFormat LINEAR_AUDIO = new AudioFormat(AudioFormat.LINEAR, 8000, 16, 1,
            AudioFormat.LITTLE_ENDIAN, AudioFormat.SIGNED);
    private final static ArrayList<String> mediaTypes = new ArrayList();
    

    static {
        mediaTypes.add("audio");
    }
    /** supported formats definition */
    private final static Format[] FORMATS = new Format[]{AVProfile.L16_MONO, AVProfile.L16_STEREO, AVProfile.PCMA,
        AVProfile.PCMU, AVProfile.SPEEX, AVProfile.GSM, LINEAR_AUDIO
    };
    private Track track;
    private Codec codec;
    private String audioMediaDirectory;
    private VoicesCache voicesCache;
    //private final static ArrayList<CodecFactory> codecFactories = new ArrayList();
    private final static CodecFactory[] codecFactories;
    

    static {
//		codecFactories.add(new org.mobicents.media.server.impl.dsp.audio.g711.alaw.DecoderFactory());
//		codecFactories.add(new org.mobicents.media.server.impl.dsp.audio.g711.alaw.EncoderFactory());
//
//		codecFactories.add(new org.mobicents.media.server.impl.dsp.audio.g711.ulaw.DecoderFactory());
//		codecFactories.add(new org.mobicents.media.server.impl.dsp.audio.g711.ulaw.EncoderFactory());
//
//		codecFactories.add(new org.mobicents.media.server.impl.dsp.audio.gsm.DecoderFactory());
//		codecFactories.add(new org.mobicents.media.server.impl.dsp.audio.gsm.EncoderFactory());
//
//		codecFactories.add(new org.mobicents.media.server.impl.dsp.audio.speex.DecoderFactory());
//		codecFactories.add(new org.mobicents.media.server.impl.dsp.audio.speex.EncoderFactory());
//
//		codecFactories.add(new org.mobicents.media.server.impl.dsp.audio.g729.DecoderFactory());
//		codecFactories.add(new org.mobicents.media.server.impl.dsp.audio.g729.EncoderFactory());
        codecFactories = new CodecFactory[]{
                    new org.mobicents.media.server.impl.dsp.audio.g711.alaw.DecoderFactory(), new org.mobicents.media.server.impl.dsp.audio.g711.alaw.EncoderFactory(), new org.mobicents.media.server.impl.dsp.audio.g711.ulaw.DecoderFactory(), new org.mobicents.media.server.impl.dsp.audio.g711.ulaw.EncoderFactory(), new org.mobicents.media.server.impl.dsp.audio.gsm.DecoderFactory(), new org.mobicents.media.server.impl.dsp.audio.gsm.EncoderFactory(), new org.mobicents.media.server.impl.dsp.audio.speex.DecoderFactory(), new org.mobicents.media.server.impl.dsp.audio.speex.EncoderFactory(), new org.mobicents.media.server.impl.dsp.audio.g729.DecoderFactory(), new org.mobicents.media.server.impl.dsp.audio.g729.EncoderFactory()
                };

    }
    private String voiceName = "kevin";
    private int volume;

    private Codec selectCodec(Format f) {
        for (CodecFactory factory : codecFactories) {
            if (factory.getSupportedInputFormat().matches(f) && factory.getSupportedOutputFormat().matches(format)) {
                return factory.getCodec();
            }
        }
        return null;
    }

    /**
     * Creates new instance of the Audio player.
     * 
     * @param name
     *            the name of the AudioPlayer to be created.
     * @param timer
     *            source of synchronization.
     * @param audioMediaDirectory 
     */
    public AudioPlayerImpl(String name, String audioMediaDirectory, VoicesCache vc) {
        super(name);
        this.audioMediaDirectory = audioMediaDirectory;
        this.voicesCache = vc;

    }

    @Override
    public long getDuration() {
        return track.getDuration();
    }

    @Override
    public void setMediaTime(long timestamp) {
        track.setMediaTime(timestamp);
    }

    @Override
    public long getMediaTime() {
        return track.getMediaTime();
    }

    /**
     * (Non Java-doc.)
     * 
     * @see org.mobicents.media.server.spi.resource.AudioPlayer#setURL(java.lang.String)
     */
    public void setURL(String passedURI) throws IOException, ResourceUnavailableException {
        // let's disallow to assign file is player is not connected
        if (!this.isConnected()) {
            throw new IllegalStateException("Component should be connected");
        }

        // now using extension we have to determne the suitable stream parser
        int pos = passedURI.lastIndexOf('.');

        // extension is not specified?
        if (pos == -1) {
            throw new IOException("Unknow file type: " + passedURI);
        }

        String ext = passedURI.substring(pos + 1).toLowerCase();
        // creating required extension
        try {
            URL targetURL = Utils.getAbsoluteURL(this.audioMediaDirectory, passedURI);
            //check scheme, if its file, we should try to create dirs
            if (ext.matches(Extension.WAV)) {
                track = new WavTrackImpl(targetURL);
            } else if (ext.matches(Extension.GSM)) {
                track = new GsmTrackImpl(targetURL);
            } else if (ext.matches(Extension.TXT)) {
                track = new TtsTrackImpl(targetURL, voiceName, this.voicesCache);
            } else if (ext.matches(Extension.MOV) || ext.matches(Extension.MP4) || ext.matches(Extension.THREE_GP)) {
                track = new AMRTrackImpl(targetURL);
            } else {
                throw new ResourceUnavailableException("Unknown extension: " + passedURI);
            }

        } catch (Exception e) {

            throw new ResourceUnavailableException(e);
        }


        // checking format of the specified file and trying to understand
        // do we need transcoding
        Format fileFormat = track.getFormat();
        codec = null;
        if (!fileFormat.matches(this.getFormat())) {
            // we need transcode. let's see if this possible
            codec = this.selectCodec(fileFormat);
            if (codec == null) {
                // transcoding is not possible with existing codecs
                throw new ResourceUnavailableException("Format is not supported: " + fileFormat);
            }
        }

    }

    @Override
    public void start() {
        if (track == null) {
            throw new IllegalStateException("The media source is not specified");
        }
        super.start();
    }

    @Override
    public void stop() {
        if (track != null) {
            track.close();
            track = null;
        }
        super.stop();
    }

    @Override
    public void evolve(Buffer buffer, long timestamp) {
        try {
            track.process(buffer);
            buffer.setTimeStamp(timestamp);

            if (codec != null && !buffer.isEOM()) {
                codec.process(buffer);
            }

            if (buffer.isEOM()) {
                track.close();
            }
        } catch (IOException e) {
            track.close();
            this.failed(NotifyEvent.TX_FAILED, e);
            buffer.setDuration(0);
        }
    }

    public Format[] getFormats() {
        return FORMATS;
    }

    public void setVoiceName(String voiceName) {
        this.voiceName = voiceName;
    }

    public String getVoiceName() {
        return voiceName;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public int getVolume() {
        return volume;
    }

    public void setText(String text) {
        track = new TtsTrackImpl(text, voiceName, this.voicesCache);
        Format fileFormat = track.getFormat();
        codec = null;
        if (!fileFormat.matches(this.getFormat())) {
            // we need transcode. let's see if this possible
            codec = this.selectCodec(fileFormat);
            if (codec == null) {
                // transcoding is not possible with existing codecs
                //throw new ResourceUnavailableException("Format is not supported: " + fileFormat);
            }
        }
    }

    @Override
    public <T> T getInterface(Class<T> interfaceType) {
        if (interfaceType.equals(Player.class)) {
            return (T) this;
        }
        if (interfaceType.equals(TTSEngine.class)) {
            return (T) this;
        } else {
            return null;
        }
    }
}
