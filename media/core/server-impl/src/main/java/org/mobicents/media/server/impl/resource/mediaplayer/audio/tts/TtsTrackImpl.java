/*
 * JBoss, Home of Professional Open Source
 * Copyright XXXX, Red Hat Middleware LLC, and individual contributors as indicated
 * by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a full listing
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 */
package org.mobicents.media.server.impl.resource.mediaplayer.audio.tts;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Vector;

import javax.sound.sampled.AudioInputStream;

import org.mobicents.media.Buffer;
import org.mobicents.media.Format;
import org.mobicents.media.format.AudioFormat;
import org.mobicents.media.server.impl.resource.mediaplayer.Track;

import com.sun.speech.freetts.Voice;

/**
 *
 * @author kulikov
 * @author amit bhayani
 */
public class TtsTrackImpl implements Track {

    /** audio stream */
    private transient AudioInputStream stream = null;
    private AudioFormat format = new AudioFormat(AudioFormat.LINEAR, 8000, 16, 1,
            AudioFormat.LITTLE_ENDIAN, AudioFormat.SIGNED);
    private int period = 20;
    private int frameSize;
    private boolean eom;
    private boolean isReady = false;
    private Vector<InputStream> outputList;
    private Voice voice;
    
    private long duration;
    private long timestamp;
    
    public TtsTrackImpl(URL url, String voiceName) throws IOException {
        isReady = false;
        URLConnection connection = url.openConnection();

        frameSize = (int) (period * format.getChannels() * format.getSampleSizeInBits() *
                format.getSampleRate() / 8000);
        
        // creating voice
        VoiceManager voiceManager = VoiceManager.getInstance();
        voice = voiceManager.getVoice(voiceName);
        voice.allocate();
        
        //creating speech buffer for writting
        TTSAudioBuffer audioBuffer = new TTSAudioBuffer();
        
        //assign buffer to speech engine and start generation
        //produced media data will be stored in the audio buffer
        voice.setAudioPlayer(audioBuffer);
        voice.speak(connection.getInputStream());

        audioBuffer.flip();
    }

    public TtsTrackImpl(String text, String voiceName) {
        isReady = false;
        
        // creating voice
        VoiceManager voiceManager = VoiceManager.getInstance();
        voice = voiceManager.getVoice(voiceName);
        voice.allocate();
        
        //creating speech buffer for writting
        TTSAudioBuffer audioBuffer = new TTSAudioBuffer();

        //assign buffer to speech engine and start generation
        //produced media data will be stored in the audio buffer
        voice.setAudioPlayer(audioBuffer);
        voice.speak(text);

        audioBuffer.flip();
        frameSize = (int) (period * format.getChannels() * format.getSampleSizeInBits() *
                format.getSampleRate() / 8000);
        
    }
    
    public void setPeriod(int period) {
        this.period = period;
        frameSize = (int) (period * format.getChannels() * format.getSampleSizeInBits() *
                format.getSampleRate() / 8000);
    }

    public int getPeriod() {
        return period;
    }

    public long getMediaTime() {
        return timestamp;
    }
    
    public void setMediaTime(long timestamp) {
        this.timestamp = timestamp;
        try {
            stream.reset();
            long offset = frameSize * (timestamp / period);
            stream.skip(offset);
        } catch (IOException e) {
        }
    }
    
    public long getDuration() {
        return duration;
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

    private void switchEndian(byte[] b, int off, int readCount) {

        for (int i = off; i < (off + readCount); i += 2) {
            byte temp;
            temp = b[i];
            b[i] = b[i + 1];
            b[i + 1] = temp;
        }

    }

    public void process(Buffer buffer) throws IOException {
        if (!isReady) {
            buffer.setDiscard(true);
            return;
        }
        
        byte[] data = buffer.getData();
        if (data == null) {
            data = new byte[frameSize];
        }
        buffer.setData(data);

        int len = readPacket(data, 0, frameSize);
        if (len == 0) {
            eom = true;
        }
        switchEndian(data, 0, data.length);

        if (len < frameSize) {
            padding(data, frameSize - len);
            eom = true;
        }

        buffer.setOffset(0);
        buffer.setLength(frameSize);
        buffer.setEOM(eom);
        buffer.setDuration(20);
    }

    public void close() {
        try {
            voice.deallocate();
            stream.close();
        } catch (Exception e) {
        }
    }

    private class TTSAudioBuffer implements com.sun.speech.freetts.audio.AudioPlayer {

        private javax.sound.sampled.AudioFormat fmt;
        private float volume;
        private byte[] localBuff;
        private int curIndex = 0;
        private int totalBytes = 0;

        public TTSAudioBuffer() {
            outputList = new Vector<InputStream>();
        }

        public void setAudioFormat(javax.sound.sampled.AudioFormat fmt) {
            this.fmt = fmt;
        }

        public javax.sound.sampled.AudioFormat getAudioFormat() {
            return fmt;
        }

        public void pause() {
        }

        public void resume() {
        }

        public void reset() {
            curIndex = 0;
            localBuff = null;
            isReady = false;
        }

        public boolean drain() {
            return true;
        }

        public void begin(int size) {
            localBuff = new byte[size];
            curIndex = 0;
        }

        public boolean end() {
            outputList.add(new ByteArrayInputStream(localBuff));
            totalBytes += localBuff.length;
            isReady = true;
            return true;
        }

        public void cancel() {
        }

        public void close() {
        }

        public void flip() {
        	InputStream is = new SequenceInputStream(outputList.elements());
            stream = new AudioInputStream(is, fmt, totalBytes / fmt.getFrameSize());
            duration = (long)(totalBytes/320 * 20);
        }
        
        public float getVolume() {
            return volume;
        }

        public void setVolume(float volume) {
            this.volume = volume;
        }

        public long getTime() {
            return 0;
        }

        public void resetTime() {
        }

        public void startFirstSampleTimer() {
        }

        public boolean write(byte[] buff) {
            return write(buff, 0, buff.length);
        }

        public boolean write(byte[] buff, int off, int len) {
            System.arraycopy(buff, off, localBuff, curIndex, len);
            curIndex += len;
            return true;
        }

        public void showMetrics() {
        }
    }

    public Format getFormat() {
        return format;
    }
}
