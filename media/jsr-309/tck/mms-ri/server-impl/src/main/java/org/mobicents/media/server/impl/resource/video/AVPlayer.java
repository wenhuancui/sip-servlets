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
package org.mobicents.media.server.impl.resource.video;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.mobicents.media.Buffer;
import org.mobicents.media.Format;
import org.mobicents.media.MediaSink;
import org.mobicents.media.MediaSource;
import org.mobicents.media.server.impl.AbstractSource;
import org.mobicents.media.server.impl.BaseComponent;
import org.mobicents.media.server.impl.resource.ss7.Mtp2;
import org.mobicents.media.server.spi.Timer;
import org.mobicents.media.server.spi.resource.Player;

/**
 * 
 * @author kulikov
 * @author amit bhayani
 */
public class AVPlayer extends BaseComponent implements Player {

	private final static Format[] AUDIO_FORMATS = new Format[] { Format.ANY };
	private final static Format[] VIDEO_FORMATS = new Format[] { Format.ANY };
	private final static Logger logger = Logger.getLogger(AVPlayer.class);
	private volatile String fileName = null;
	private volatile FileInputStream fin = null;
	private volatile DataInputStream ds = null;
	private FileTypeBox fileTypeBox;
	private MovieBox movieBox;
	private MediaDataBox mediaDataBox;
	private volatile TrackBox audioTrackBox = null;
	private volatile TrackBox audioHintTrackBox = null;
	private volatile TrackBox videoTrackBox = null;
	private volatile TrackBox videoHintTrackBox = null;
	private AudioSource audioSource;
	private VideoSource videoSource;
	private String url;
	private static ArrayList<String> mediaTypes = new ArrayList();

	static {
		mediaTypes.add("audio");
		mediaTypes.add("video");
	}

	private HashMap<String, String> tracks = new HashMap();

	public AVPlayer(String name, Timer timer) {
		super(name);
		this.audioSource = new AudioSource(name, this);
		this.audioSource.setSyncSource(timer);

		this.videoSource = new VideoSource(name, this);
		this.videoSource.setSyncSource(timer);
	}

	private String getMediaType(String type) {
		return type.startsWith("track") ? tracks.get(type) : type;
	}

	public void setSSRC(String media, long ssrc) {

		//System.out.println("setSSRC media = " + media + " ssrc = " + ssrc);

		String mediaType = getMediaType(media);
		if (mediaType.equals("audio")) {
			audioSource.setSSRC(media, ssrc);
		} else {
			videoSource.setSSRC(media, ssrc);
		}
	}
	
	public void setRtpTime(String media, long rtpTime) {
		//System.out.println("setRtpTime media = " + media + " ssrc = " + rtpTime);

		String mediaType = getMediaType(media);
		if (mediaType.equals("audio")) {
			audioSource.setRtpTime(media, rtpTime);
		} else {
			videoSource.setRtpTime(media, rtpTime);
		}		
	}

	public void setFile(String fileName) {
		this.fileName = fileName;
	}

	public String getSdp(String mediaType) {
		String type = getMediaType(mediaType);
		if (type.equals("audio")) {
			return audioSource.getSdp();
		} else {
			return videoSource.getSdp();
		}
	}

	public MediaSource getMediaSource(String media) {
		String type = getMediaType(media);
		if (type.equals("audio")) {
			return audioSource;
		} else {
			return videoSource;
		}
	}

	private byte[] read(DataInputStream in) throws IOException {
		byte[] buff = new byte[4];
		for (int i = 0; i < buff.length; i++) {
			buff[i] = in.readByte();
		}
		return buff;
	}

	public void prepareTracks(String fileName) throws Exception {

		// Here is check to make sure that Audio and Video is coming from same file
		if (this.audioHintTrackBox != null || this.videoHintTrackBox != null) {
			if (this.fileName.compareTo(fileName) != 0) {
				throw new Exception("The passed fileName " + fileName + " doesnt match with fileName " + this.fileName
						+ " set in AVPlayer ");
			}
			return;
		}

		closeStream();
		this.audioTrackBox = null;
		this.audioHintTrackBox = null;
		this.videoTrackBox = null;
		this.videoHintTrackBox = null;

		this.fileTypeBox = null;
		this.movieBox = null;
		this.mediaDataBox = null;

		this.fileName = fileName;

		try {
			parse(fileName);

			for (TrackBox tBox : this.movieBox.getTrackBoxes()) {
				String type = tBox.getMediaBox().getHandlerReferenceBox().getHandlerType();
				if (type.equalsIgnoreCase("soun")) {
					audioTrackBox = tBox;
				} else if (type.equalsIgnoreCase("vide")) {
					videoTrackBox = tBox;
				} else if (type.equalsIgnoreCase("hint")) {
					TrackReferenceBox trackReferenceBox = tBox.getTrackReferenceBox();
					List<TrackReferenceTypeBox> trackRefTypList = trackReferenceBox.getTrackReferenceTypeBoxes();

					for (TrackReferenceTypeBox trkRefTypBox : trackRefTypList) {
						if (trkRefTypBox.getType().equals(HintTrackReferenceTypeBox.TYPE_S)) {
							long[] trackIds = trkRefTypBox.getTrackIDs();
							// FIXME we are assuming here there is always 1 track that this hint track is referencing
							// but there could be more than 1
							long trackId = trackIds[0];
							// FIXME audioTrackBox can be still null if hint track is placed before audioTrack
							if (audioTrackBox.getTrackHeaderBox().getTrackID() == trackId) {
								audioHintTrackBox = tBox;
							} else if (videoTrackBox.getTrackHeaderBox().getTrackID() == trackId) {
								videoHintTrackBox = tBox;
							}
						}
					}
				}
			}// for

			// TODO : Should we set the fileTypeBox, movieBox and mediaDataBox back to null?
		} finally {
			closeStream();
		}

	}

	public void afterStop() {
		closeStream();
	}

	private void closeStream() {

		if (fin != null) {
			try {
				fin.close();
			} catch (Exception e) {
				logger.warn("Error while closing the FileInputStream for file " + fileName, e);
			}
		}

		if (ds != null) {
			try {
				ds.close();
			} catch (Exception e) {
				logger.warn("Error while closing the FileInputStream for file " + fileName, e);
			}
		}
	}

	private void parse(String fileName) throws IOException {
		fin = new FileInputStream(fileName);
		ds = new DataInputStream(fin);

		long count = 0;

		while (ds.available() > 0) {
			long len = readU32(ds);
			byte[] type = read(ds);

			if (comparebytes(type, FileTypeBox.TYPE)) {
				fileTypeBox = new FileTypeBox(len);
				count += fileTypeBox.load(ds);

				if (logger.isDebugEnabled()) {
					logger.debug(this.fileTypeBox.toString());
				}
			} else if (comparebytes(type, MovieBox.TYPE)) {
				movieBox = new MovieBox(len);
				count += movieBox.load(ds);
			} else if (comparebytes(type, FreeSpaceBox.TYPE_FREE)) {
				FreeSpaceBox free = new FreeSpaceBox(len, FreeSpaceBox.TYPE_FREE_S);
				count += free.load(ds);
			} else if (comparebytes(type, FreeSpaceBox.TYPE_SKIP)) {
				FreeSpaceBox skip = new FreeSpaceBox(len, FreeSpaceBox.TYPE_SKIP_S);
				count += skip.load(ds);
			} else if (comparebytes(type, MediaDataBox.TYPE)) {
				// TODO : How should we handle multiple MediaDataBox?
				mediaDataBox = new MediaDataBox(len);
				count += mediaDataBox.load(ds);

				// if (file == null) {
				// file = new File(this.fileName);
				// }
			} else {
				// TODO : Do we care for other boxes?
				if (len - 8 > 0) {
					ds.skipBytes((int) len - 8);
				}
			}
		}
	}

	private boolean comparebytes(byte[] arg1, byte[] arg2) {
		if (arg1.length != arg2.length) {
			return false;
		}
		for (int i = 0; i < arg1.length; i++) {
			if (arg1[i] != arg2[i]) {
				return false;
			}
		}
		return true;
	}

	private long readU32(DataInputStream in) throws IOException {
		return ((long) (in.read() << 24 | in.read() << 16 | in.read() << 8 | in.read())) & 0xFFFFFFFFL;
	}

	public static void main(String[] args) throws Exception {

		// BasicConfigurator.configure();

		AVPlayer vp = new AVPlayer("PlayerTest", null);
		vp.setURL("/home/abhayani/Desktop/tones/sample_50kbit.3gp");

		System.out.println("AUDIO SDP = " + vp.audioSource.getSdp());
		System.out.println("VIDEO SDP = " + vp.videoSource.getSdp());

		RTPSample sample = null;
		// Audio
		for (;;) {
			try {
				sample = vp.audioSource.audioTrack.process();
				if (sample == null) {
					System.out.println("Audio Stream completed !!!");
					break;
				}
				System.out.println("\nSample Period = " + sample.getSamplePeriod());
				for (RTPLocalPacket rtpPacket : sample.getRtpLocalPackets()) {
					System.out.println("\nrtpSequenceSeed = " + rtpPacket.getRtpSequenceSeed() + " payloadType = "
							+ rtpPacket.getPayloadType() + " RtpTimeStamp = " + rtpPacket.getRtpTimestamp()
							+ " PBit = " + rtpPacket.getPbit() + " X Bit = " + rtpPacket.getXbit() + " Marker = "
							+ rtpPacket.getMbit());

					System.out.println("The Payload Data ");
					System.out.print(Mtp2.dump(rtpPacket.getPayload(), rtpPacket.getPayload().length, false));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		// Video
		// for (;;) {
		// try {
		// sample = vp.videoSource.videoTrack.process();
		// if (sample == null) {
		// System.out.println("Audio Stream completed !!!");
		// break;
		// }
		// System.out.println("Sample Period = " + sample.getSamplePeriod());
		// for (RTPLocalPacket rtpPacket : sample.getRtpLocalPackets()) {
		// System.out.println("\nrtpSequenceSeed = " + rtpPacket.getRtpSequenceSeed() + " payloadType = "
		// + rtpPacket.getPayloadType() + " RtpTimeStamp = " + rtpPacket.getRtpTimestamp()
		// + " PBit = " + rtpPacket.getPbit() + " X Bit = " + rtpPacket.getXbit() + " Marker = "
		// + rtpPacket.getMbit());
		//
		// // System.out.println("The Payload Data ");
		// // System.out.print(Mtp2.dump(rtpPacket.getPayload(), rtpPacket.getPayload().length, false));
		// }
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		//
		// }
	}

	public Format[] getFormats() {
		// TODO Auto-generated method stub
		return null;
	}

	public void start() {
		this.audioSource.start();
		this.videoSource.start();
	}

	public void stop() {
		this.audioSource.stop();
		this.videoSource.stop();
	}

	public Collection<String> getMediaTypes() {
		return mediaTypes;
	}

	public MediaSink getSink(String media) {
		return null;
	}

	public MediaSource getSource(String media) {
		String mediaType = getMediaType(media);
		if (mediaType.equals("audio")) {
			return audioSource;
		} else if (mediaType.equals("video")) {
			return videoSource;
		}
		return null;
	}
	
	public void setURL(String url) {
		this.url = url;
		try {
			// this.prepareTracks(url);
			this.audioSource.setURL(url);
			this.videoSource.setURL(url);

			if (audioSource.getTrackId() != -1) {
				tracks.put("trackID=" + audioSource.getTrackId(), "audio");
				//System.out.println("Set the Audio TrackId "+ audioSource.getTrackId() + " ***********");
			}

			if (videoSource.getTrackId() != -1) {
				tracks.put("trackID=" + videoSource.getTrackId(), "video");
				//System.out.println("Set the Video TrackId "+ videoSource.getTrackId() + " ***********");
			}

		} catch (Exception e) {
			logger.error("Setting of URL failed for AVPlayer", e);
		}
	}

	public String getURL() {
		return this.url;
	}

	public double getNPT(String media) {
		String mediaType = getMediaType(media);
		if (mediaType.equals("audio")) {
			return audioSource.getNPT(media);
		} else if (mediaType.equals("video")) {
			return videoSource.getNPT(media);
		}
		return 0;
	}


	private class AudioSource extends AbstractSource implements Player {

		private String url;
		private AVPlayer avPlayer;
		private AudioTrack audioTrack;
		private volatile boolean eom = false;
		private volatile long ssrc = 0;
		private volatile long rtpTime = 0;

		private volatile RTPLocalPacket[] packets;
		private volatile int idx;
		private volatile boolean isEmpty = true;
		private volatile long duration;

		public AudioSource(String name, AVPlayer avPlayer) {
			super(name);
			this.avPlayer = avPlayer;
		}

		public long getTrackID() {
			return audioTrack.getTrackId();
		}

		public void prepareTracks() throws Exception {
			this.avPlayer.prepareTracks(this.url);
			this.audioTrack = new AudioTrack(audioTrackBox, audioHintTrackBox, new File(this.url));

		}

		@Override
		public void beforeStart() throws Exception {
			//System.out.println("AudioSource.beforeStart()");
			this.eom = false;
		}

		@Override
		public void afterStop() {
			if (this.audioTrack != null) {
				this.audioTrack.close();
			}
			this.avPlayer.audioHintTrackBox = null;
			this.avPlayer.audioTrackBox = null;

			if (this.avPlayer.videoHintTrackBox == null && this.avPlayer.videoTrackBox == null) {
				this.avPlayer.fileName = null;
			}
		}

		@Override
		public void evolve(Buffer buffer, long timestamp, long sequenceNumber) {
			if (isEmpty) {
				try {
					RTPSample rtpSample = this.audioTrack.process();
					if (rtpSample == null) {
						buffer.setEOM(eom);
						buffer.setFlags(buffer.getFlags() | Buffer.FLAG_RTP_BINARY);
						return;
					}
					packets = rtpSample.getRtpLocalPackets();
					duration = rtpSample.getSamplePeriod();
					if(packets.length == 0){
						buffer.setLength(0);
						buffer.setDuration(duration);
						return;
					}
					idx = 0;
					isEmpty = false;
				} catch (Exception e) {
					// TODO we have to rework this part
					throw new IllegalArgumentException(e);
				}
			}
			byte[] data = packets[idx++].toByteArray(this.ssrc);
			isEmpty = idx == packets.length;

			buffer.setData(data);
			buffer.setLength(data.length);
			buffer.setTimeStamp(0);
			buffer.setOffset(0);
			buffer.setSequenceNumber(0);
			buffer.setEOM(eom);
			buffer.setFlags(buffer.getFlags() | Buffer.FLAG_RTP_BINARY);
			buffer.setDuration(isEmpty ? duration : -1);
		}

		public Format[] getFormats() {
			return AUDIO_FORMATS;
		}

		public void setURL(String url) {
			this.url = url;
			try {
				prepareTracks();
			} catch (Exception e) {
				logger.error("pprepareTracks failed for AudioSource", e);
				//e.printStackTrace();
				// TODO : Throw exception
			}
		}

		public String getURL() {
			return url;
		}

		public String getSdp() {
			return this.audioTrack != null ? this.audioTrack.getSdpText() : null;
		}

		public long getTrackId() {
			return this.audioTrack != null ? this.audioTrack.getTrackId() : -1;
		}

		public Collection<String> getMediaTypes() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public MediaSource getMediaSource(String media) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public void setSSRC(String media, long ssrc) {
			this.ssrc = ssrc;
		}

		public void setRtpTime(String media, long rtpTime) {
			if(this.audioTrack != null){
				this.audioTrack.setRtpTime(rtpTime);
			}
		}

		public double getNPT(String media) {
			if(this.audioTrack != null){
				return this.audioTrack.getNPT();
			}
			return 0;
		}
	}

	private class VideoSource extends AbstractSource implements Player {

		private String url;
		private AVPlayer avPlayer;
		private VideoTrack videoTrack;
		private volatile boolean eom = false;
		private volatile long ssrc = 0;
		private volatile long rtpTime = 0;

		private volatile RTPLocalPacket[] packets;
		private volatile int idx;
		private volatile boolean isEmpty = true;
		private volatile long duration;

		public VideoSource(String name, AVPlayer avPlayer) {
			super(name);
			this.avPlayer = avPlayer;
		}

		public long getTrackID() {
			return videoTrack.getTrackId();
		}

		public void prepareTracks() throws Exception {
			this.avPlayer.prepareTracks(this.url);
			this.videoTrack = new VideoTrack(videoTrackBox, videoHintTrackBox, new File(this.url));

		}

		@Override
		public void beforeStart() throws Exception {
			//System.out.println("VideoSource.beforeStart()");
			this.eom = false;
		}

		@Override
		public void afterStop() {
			if (this.videoTrack != null) {
				this.videoTrack.close();
			}
			this.avPlayer.videoHintTrackBox = null;
			this.avPlayer.videoTrackBox = null;

			if (this.avPlayer.audioHintTrackBox == null && this.avPlayer.audioTrackBox == null) {
				this.avPlayer.fileName = null;
			}
		}

		@Override
		public void evolve(Buffer buffer, long timestamp, long sequenceNumber) {
			if (isEmpty) {
				try {
					RTPSample rtpSample = videoTrack.process();
					if (rtpSample == null) {
						buffer.setEOM(true);
						buffer.setFlags(buffer.getFlags() | Buffer.FLAG_RTP_BINARY);
						buffer.setDuration(0);
						return;
					}
					packets = rtpSample.getRtpLocalPackets();
					duration = rtpSample.getSamplePeriod();
					idx = 0;
					isEmpty = false;
				} catch (Exception e) {
					// TODO we have to rework this part
					throw new IllegalArgumentException(e);
				}
			}

			byte[] data = packets[idx++].toByteArray(ssrc);
			isEmpty = idx == packets.length;

			buffer.setData(data);
			buffer.setLength(data.length);
			buffer.setTimeStamp(0);
			buffer.setOffset(0);
			buffer.setSequenceNumber(0);
			buffer.setEOM(false);
			buffer.setFlags(buffer.getFlags() | Buffer.FLAG_RTP_BINARY);
			buffer.setDuration(isEmpty ? duration : -1);
		}

		public Format[] getFormats() {
			return VIDEO_FORMATS;
		}

		public void setURL(String url) {
			this.url = url;
			try {
				prepareTracks();
			} catch (Exception e) {
				logger.error("prepareTracks failed for VideoSource", e);
				//e.printStackTrace();

				// TODO : Throw exception
			}
		}

		public String getURL() {
			return url;
		}

		public String getSdp() {
			return this.videoTrack != null ? this.videoTrack.getSdpText() : null;
		}

		public long getTrackId() {
			return this.videoTrack != null ? this.videoTrack.getTrackId() : -1;
		}

		public Collection<String> getMediaTypes() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public MediaSource getMediaSource(String media) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public void setSSRC(String media, long ssrc) {
			this.ssrc = ssrc;
		}
		
		public void setRtpTime(String media, long rtpTime) {
			if(this.videoTrack != null){
				this.videoTrack.setRtpTime(rtpTime);
			}
		}
		
		public double getNPT(String media) {
			if(this.videoTrack != null){
				return this.videoTrack.getNPT();
			}
			return 0;
		}
	}


}
