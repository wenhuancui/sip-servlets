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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;

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
import org.mobicents.media.server.spi.resource.audio.AdvancedAudioPlayer;
import org.xiph.speex.spi.SpeexAudioFileReader;
import org.xiph.speex.spi.SpeexEncoding;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;

/**
 * 
 * @author kulikov
 * @author amit bhayani
 */
public class AdvancedAudioPlayerImpl extends AbstractSource implements
		AdvancedAudioPlayer {

	private final static ArrayList<String> mediaTypes = new ArrayList();
	static {
		mediaTypes.add("audio");
	}

	/** supported formats definition */
	private final static Format[] FORMATS = new Format[] { AVProfile.L16_MONO,
			AVProfile.L16_STEREO, AVProfile.PCMA, AVProfile.PCMU,
			AVProfile.SPEEX, AVProfile.GSM, Codec.LINEAR_AUDIO };
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
	private final int sampleSizeInBytes = 2;
	private volatile Codec codec;

	private volatile boolean isTTS = false;
	private String text;
	private Voice voice;

	private boolean isReady = false;
	private int totBytes = 0;

	private Vector<InputStream> outputList;

	private String mediaDir = null;

	private static final String FILE_SCHEME = "file";
	private static final String HTTP_SCHEME = "http";

	private volatile int maxDuration = 0;
	private volatile int startOffSet = 0;

	private volatile int durationElpased = 0;

	// private ByteArrayInputStream ttsByteArrInStream = null;

	private final static ArrayList<CodecFactory> codecFactories = new ArrayList();
	static {
		codecFactories
				.add(new org.mobicents.media.server.impl.dsp.audio.g711.alaw.DecoderFactory());
		codecFactories
				.add(new org.mobicents.media.server.impl.dsp.audio.g711.alaw.EncoderFactory());

		codecFactories
				.add(new org.mobicents.media.server.impl.dsp.audio.g711.ulaw.DecoderFactory());
		codecFactories
				.add(new org.mobicents.media.server.impl.dsp.audio.g711.ulaw.EncoderFactory());

		codecFactories
				.add(new org.mobicents.media.server.impl.dsp.audio.gsm.DecoderFactory());
		codecFactories
				.add(new org.mobicents.media.server.impl.dsp.audio.gsm.EncoderFactory());

		codecFactories
				.add(new org.mobicents.media.server.impl.dsp.audio.speex.DecoderFactory());
		codecFactories
				.add(new org.mobicents.media.server.impl.dsp.audio.speex.EncoderFactory());

		codecFactories
				.add(new org.mobicents.media.server.impl.dsp.audio.g729.DecoderFactory());
		codecFactories
				.add(new org.mobicents.media.server.impl.dsp.audio.g729.EncoderFactory());
	}

	private Codec selectCodec(Format f) {
		for (CodecFactory factory : codecFactories) {
			if (factory.getSupportedInputFormat().matches(f)
					&& factory.getSupportedOutputFormat().matches(format)) {
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
	 */
	public AdvancedAudioPlayerImpl(String name, Timer timer) {
		super(name);
		setSyncSource(timer);
	}

	public void setMaxDuration(int maxDuration) {
		this.maxDuration = maxDuration;
	}

	public void setStartOffSet(int startOffSet) {
		this.startOffSet = startOffSet;
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
		return this.file;
	}

	public boolean isTTS() {
		return this.isTTS;
	}

	public void setTTS(boolean isTTS) {
		this.isTTS = isTTS;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getText() {
		return this.text;
	}

	public void setVoice(String name) {
		VoiceManager voiceManager = VoiceManager.getInstance();
		voice = voiceManager.getVoice(name);
		voice.allocate();

	}

	public String getVoice() {
		return voice != null ? voice.getName() : null;
	}

	@Override
	public void beforeStart() throws Exception {
		this.durationElpased = 0;

		if (this.isTTS) {

			isReady = false;
			TTSAudioStream ttsAudioStream = new TTSAudioStream();
			voice.setAudioPlayer(ttsAudioStream);
			if (this.file != null) {
				try {
					URL uri = new URL(this.file);
					URLConnection connection = uri.openConnection();
					voice.speak(connection.getInputStream());
				} catch (IOException e) {
					failed(NotifyEvent.START_FAILED, e);
				}

			} else {
				voice.speak(text);
				ttsAudioStream.close();
			}
			frameSize = this.getPacketSize(getPeriod(), Codec.LINEAR_AUDIO);
			eom = false;
		} else {
			this.codec = null;
			closeAudioStream();

			URI uri = new URI(this.file);
			String scheme = uri.getScheme();
			if (scheme == null) {
				if (file.endsWith("spx")) {
					stream = new SpeexAudioFileReader()
							.getAudioInputStream(new File(
									(this.mediaDir + this.file)));
				} else {
					stream = AudioSystem.getAudioInputStream(new File(
							(this.mediaDir + this.file)));
				}
			} else if (scheme.equalsIgnoreCase(FILE_SCHEME)) {
				if (file.endsWith("spx")) {
					stream = new SpeexAudioFileReader()
							.getAudioInputStream(new File(uri));
				} else {
					stream = AudioSystem.getAudioInputStream(new File(uri));
				}
			} else if (scheme.equalsIgnoreCase(HTTP_SCHEME)) {
				if (file.endsWith("spx")) {
					stream = new SpeexAudioFileReader().getAudioInputStream(uri
							.toURL());
				} else {
					stream = AudioSystem.getAudioInputStream(uri.toURL());
				}
			} else {
				throw new MalformedURLException("Protocol " + scheme
						+ " is unknown");
			}

			Format f = getFormat(stream);

			if (f == null) {
				throw new IOException("Unsupported format: "
						+ stream.getFormat());
			}

			if (!f.matches(this.format)) {
				codec = this.selectCodec(f);
				if (codec == null) {
					throw new IOException("Unsupported format: "
							+ stream.getFormat());
				}
			}

			format.setFrameRate(this.getFrameRate());
			frameSize = this.getPacketSize(getPeriod(), (AudioFormat) f);
			eom = false;
		}

		logger.info("isTTS = " + this.isTTS + " TTS = " + this.text + " URL = "
				+ this.file + " MaxDuration = " + this.maxDuration
				+ " StartOffset = " + this.startOffSet + " ");

		if (this.startOffSet > 0) {
			startAtOffSet();
		}
	}

	private void startAtOffSet() throws IOException {
		byte[] b = new byte[frameSize];
		int reps = this.startOffSet / getPeriod();
		for (int i = 0; i < reps; i++) {
			int dataRead = this.readPacket(b, 0, frameSize);
			if (dataRead == -1) {
				throw new IOException("StartOffSet " + this.startOffSet
						+ " is higher than actual file playing time");
			}
		}

		logger.info("Play off set by " + (reps * this.getPeriod()));
	}

	@Override
	public void afterStop() {

		closeAudioStream();
		if (this.voice != null) {
			voice.deallocate();
		}

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
				return channels == 1 ? (AudioFormat) AVProfile.L16_MONO
						: (AudioFormat) AVProfile.L16_STEREO;
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
		int pSize = (int) (packetDuration * fmt.getChannels()
				* fmt.getSampleSizeInBits() * fmt.getSampleRate() / 8000);
		if (pSize < 0) {
			// For Format for which bit is AudioFormat.NOT_SPECIFIED, 160 is
			// passed
			pSize = 160;
			if (format == AVProfile.GSM) {
				// For GSM the RTP Packet size is 33
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
	private int readPacket(byte[] packet, int offset, int psize)
			throws IOException {
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

	private void switchEndian(byte[] b, int off, int readCount) {

		for (int i = off; i < (off + readCount); i += sampleSizeInBytes) {
			byte temp;
			temp = b[i];
			b[i] = b[i + 1];
			b[i + 1] = temp;
		}

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
		if (this.isTTS) {
			evolveTTS(buffer, timestamp, seq);
		} else {
			byte[] data = new byte[frameSize];
			buffer.setData(data);
			try {
				int len = readPacket(data, 0, frameSize);
				if (len == 0
						|| ((this.maxDuration > 0) && (this.maxDuration < this.durationElpased))) {
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
			if (codec != null) {
				codec.process(buffer);
			}
		}
		durationElpased += this.getPeriod();
	}

	private void evolveTTS(Buffer buffer, long timestamp, long seq) {
		if (isReady) {
			byte[] data = new byte[frameSize];

			try {
				int len = readPacket(data, 0, frameSize);
				if (len == 0) {
					eom = true;
				} else {
					switchEndian(data, 0, data.length);
				}

				if (len < frameSize) {
					padding(data, frameSize - len);
				}

			} catch (IOException e) {
				failed(NotifyEvent.TX_FAILED, e);
				return;
			}

			buffer.setData(data);
			buffer.setLength(frameSize);
			buffer.setSequenceNumber(seq);
			buffer.setFormat(Codec.LINEAR_AUDIO);
			buffer.setTimeStamp(getSyncSource().getTimestamp());
			buffer.setDiscard(false);
			buffer.setEOM(eom);
		} else {
			buffer.setDiscard(true);
		}

	}

	public Format[] getFormats() {
		return FORMATS;
	}

	private class TTSAudioStream implements
			com.sun.speech.freetts.audio.AudioPlayer {

		private javax.sound.sampled.AudioFormat fmt;
		private float volume;
		private byte[] localBuff;
		private int curIndex = 0;

		public TTSAudioStream() {
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
			totBytes += localBuff.length;
			isReady = true;
			return true;
		}

		public void cancel() {
		}

		public void close() {
			// try {
			InputStream is = new SequenceInputStream(outputList.elements());
			stream = new AudioInputStream(is, fmt, totBytes
					/ fmt.getFrameSize());
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

	public Collection<String> getMediaTypes() {
		return mediaTypes;
	}

	public MediaSource getMediaSource(String media) {
		if (mediaTypes.equals("audio")) {
			return this;
		}
		return null;
	}

	public void setMediaDir(String mediaDir) {
		this.mediaDir = mediaDir;
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
