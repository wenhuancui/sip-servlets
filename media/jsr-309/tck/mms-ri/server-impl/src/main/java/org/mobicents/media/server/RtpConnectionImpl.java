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
package org.mobicents.media.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.sdp.Attribute;
import javax.sdp.MediaDescription;
import javax.sdp.SdpException;
import javax.sdp.SdpFactory;
import javax.sdp.SdpParseException;
import javax.sdp.SessionDescription;

import org.apache.log4j.Logger;
import org.mobicents.media.Format;
import org.mobicents.media.MediaSink;
import org.mobicents.media.MediaSource;
import org.mobicents.media.format.AudioFormat;
import org.mobicents.media.format.VideoFormat;
import org.mobicents.media.server.impl.rtp.RtpFactory;
import org.mobicents.media.server.impl.rtp.RtpSocket;
import org.mobicents.media.server.impl.rtp.RtpSocketListener;
import org.mobicents.media.server.impl.rtp.sdp.AVProfile;
import org.mobicents.media.server.resource.Channel;
import org.mobicents.media.server.resource.ChannelFactory;
import org.mobicents.media.server.spi.Connection;
import org.mobicents.media.server.spi.ConnectionMode;
import org.mobicents.media.server.spi.ConnectionState;
import org.mobicents.media.server.spi.ResourceUnavailableException;
import org.mobicents.media.server.spi.dsp.Codec;

/**
 * 
 * @author kulikov
 */
public class RtpConnectionImpl extends ConnectionImpl implements
		RtpSocketListener {

	private SdpFactory sdpFactory;

	private String localDescriptor;
	private String remoteDescriptor;

	private HashMap<String, RtpSocket> rtpSockets = new HashMap<String, RtpSocket>();
	private HashMap<String, HashMap<Integer, Format>> formats = new HashMap();

	private final static Logger logger = Logger
			.getLogger(RtpConnectionImpl.class);
	private AVProfile avProfile = new AVProfile();

	public RtpConnectionImpl(EndpointImpl endpoint, ConnectionMode mode)
			throws ResourceUnavailableException {
		super(endpoint, mode);
		sdpFactory = endpoint.getSdpFactory();

		// obtain channel factory
		ConnectionFactory connectionFactory = endpoint.getConnectionFactory();
		Map<String, ChannelFactory> rxChannelFactories = connectionFactory
				.getRxChannelFactory();
		Map<String, ChannelFactory> txChannelFactories = connectionFactory
				.getTxChannelFactory();

		// obtain rtp factory
		RtpFactory rtpFactory = endpoint.getRtpFactory();

		// obtain list of supported media types
		Collection<String> mediaTypes = endpoint.getMediaTypes();

		// creating channel for each media type
		for (String media : mediaTypes) {
			// creating rx channel
			Channel rxChannel = null;
			MediaSink sink = endpoint.getSink(media);
			if (rxChannelFactories != null && sink != null) {
				ChannelFactory rxChannelFactory = rxChannelFactories.get(media);
				if (rxChannelFactory != null) {
					rxChannel = rxChannelFactory.newInstance(endpoint);

					rxChannel.setConnection(this);
					rxChannel.setEndpoint(endpoint);

					rxChannel.connect(sink);
					rxChannels.put(media, rxChannel);
				}
			}

			// creating tx channel
			Channel txChannel = null;
			MediaSource source = endpoint.getSource(media);
			if (txChannelFactories != null && source != null) {
				ChannelFactory txChannelFactory = txChannelFactories.get(media);
				if (txChannelFactory != null) {
					txChannel = txChannelFactory.newInstance(endpoint);

					txChannel.setConnection(this);
					txChannel.setEndpoint(endpoint);

					txChannel.connect(source);
					txChannels.put(media, txChannel);
				}
			}

			RtpSocket socket = null;
			try {
				socket = rtpFactory.getRTPSocket(media);
				// save reference
				rtpSockets.put(media, socket);
			} catch (Exception e) {
				throw new ResourceUnavailableException(e);
			}

			// merging formats taking into account rtp codecs
			Collection<Format> rxFormats = null;
			if (rxChannel != null) {
				rxFormats = this.getRxFormats(rxChannel.getInputFormats(),
						socket.getCodecs());
			}

			Collection<Format> txFormats = null;
			if (txChannel != null) {
				txFormats = this.getTxFormats(txChannel.getOutputFormats(),
						socket.getCodecs());
			}

			// finally intersect supported formats with rtp configuration
			HashMap<Integer, Format> profile = new HashMap();
			if (media.equals("audio")) {
				profile.putAll(socket.getAVProfile().getAudioFormats());
			} else if (media.equals("video")) {
				profile.putAll(socket.getAVProfile().getVideoFormats());
			}
			formats.put(media, intersection(profile, rxFormats, txFormats));
			
			System.out.println("Fomrats = "+ formats);
		}

		createLocalDescriptor();
		this.mode = mode;
		setState(ConnectionState.HALF_OPEN);
	}

	private Collection<Format> getRxFormats(Format[] formats,
			Collection<Codec> codecs) {
		ArrayList<Format> list = new ArrayList();
		for (Format fmt : formats) {
			list.add(fmt);
			if (fmt == Format.ANY) {
				break;
			}
			for (Codec c : codecs) {
				if (c.getSupportedOutputFormat().matches(fmt)) {
					list.add(c.getSupportedInputFormat());
				}
			}
		}
		return list;
	}

	private Collection<Format> getTxFormats(Format[] formats,
			Collection<Codec> codecs) {
		ArrayList<Format> list = new ArrayList();
		for (Format fmt : formats) {
			list.add(fmt);
			if (fmt == Format.ANY) {
				break;
			}
			for (Codec c : codecs) {
				if (c.getSupportedInputFormat().matches(fmt)) {
					list.add(c.getSupportedOutputFormat());
				}
			}
		}
		return list;
	}

	private void print(Format[] fmts) {
		for (Format fmt : fmts) {
			System.out.println(fmt);
		}
	}

	private String createLocalDescriptor() {
		SessionDescription sdp = null;
		String userName = "MediaServer";

		long sessionID = System.currentTimeMillis() & 0xffffff;
		long sessionVersion = sessionID;

		String networkType = javax.sdp.Connection.IN;
		String addressType = javax.sdp.Connection.IP4;

		String address = rtpSockets.get("audio").getLocalAddress();

		try {
			sdp = sdpFactory.createSessionDescription();
			sdp.setVersion(sdpFactory.createVersion(0));
			sdp.setOrigin(sdpFactory.createOrigin(userName, sessionID,
					sessionVersion, networkType, addressType, address));
			sdp.setSessionName(sdpFactory.createSessionName("session"));
			sdp.setConnection(sdpFactory.createConnection(networkType,
					addressType, address));

			Vector descriptions = new Vector();

			// encode formats
			Set<String> mediaTypes = formats.keySet();
			for (String mediaType : mediaTypes) {
				HashMap<Integer, Format> map = formats.get(mediaType);

				RtpSocket rtpSocket = rtpSockets.get(mediaType);
				int port = rtpSocket.getLocalPort();

				MediaDescription md = sdpFactory.createMediaDescription(
						mediaType, port, 1, "RTP/AVP", new int[0]);

				Set<Integer> keys = map.keySet();
				for (Integer key : keys) {
					md.getMedia().getMediaFormats(true).add(key);
					md.getAttributes(true).addAll(
							this.encode(key, map.get(key)));
				}
				descriptions.add(md);
			}
			sdp.setMediaDescriptions(descriptions);
		} catch (SdpException e) {
		}

		localDescriptor = sdp.toString();
		return localDescriptor;
	}

	public String getLocalDescriptor() {
		if (getState() == ConnectionState.NULL
				|| getState() == ConnectionState.CLOSED) {
			throw new IllegalStateException("State is " + getState());
		}
		return this.localDescriptor;
	}

	public String getRemoteDescriptor() {
		return this.remoteDescriptor;
	}

	public void setRemoteDescriptor(String descriptor) throws SdpException,
			IOException, ResourceUnavailableException {
		this.remoteDescriptor = descriptor;

		if (getState() != ConnectionState.HALF_OPEN
				&& getState() != ConnectionState.OPEN) {
			throw new IllegalStateException("State is " + getState());
		}

		// preparing hash map for common formats
		HashMap<String, HashMap<Integer, Format>> subset = new HashMap();

		// parse session descriptor
		SessionDescription sdp = sdpFactory
				.createSessionDescription(descriptor);

		// determine address of the remote party
		// InetAddress address =
		// InetAddress.getByName(sdp.getConnection().getAddress());

		javax.sdp.Connection conn = null;

		// analyze media descriptions
		Vector<MediaDescription> mediaDescriptions = sdp
				.getMediaDescriptions(false);
		for (MediaDescription md : mediaDescriptions) {
			// determine media type
			String mediaType = md.getMedia().getMediaType();

			// parse offered formats
			HashMap<Integer, Format> offer = new HashMap();
			if (mediaType.equals("audio")) {
				this.parseAudioFormats(md, offer);
			} else if (mediaType.equals("video")) {
				this.parseVideoFormats(md, offer);
			}

			// obtain supported formats
			HashMap<Integer, Format> supported = formats.get(mediaType);

			// determine subset and check it
			HashMap<Integer, Format> common = this.subset(offer, supported
					.values());
			if (common.isEmpty()) {
				throw new IOException("Formats are not negotiated");
			}

			RtpSocket rtpSocket = rtpSockets.get(mediaType);
			subset.put(mediaType, common);

			// check RFC2833 signals
			int dtmf = this.peekDTMF(common);

			// select preffered as first format from list
			int pt = common.keySet().iterator().next();
			Format f = common.get(pt);

			logger.info("RTP format=" + f);

			if (dtmf > 0) {
				common.put(dtmf, AVProfile.DTMF);
			}
			// assign preffered format
			rtpSocket.setFormat(pt, f);
			rtpSocket.setDtmfPayload(dtmf);

			conn = md.getConnection();
			if (conn == null) {
				// Use session-level if media-level "c=" field is not defined
				conn = sdp.getConnection();
			}
			InetAddress address = InetAddress.getByName(conn.getAddress());

			int port = md.getMedia().getMediaPort();
			rtpSocket.setPeer(address, port);

			Channel rxChannel = rxChannels.get(mediaType);
			Channel txChannel = txChannels.get(mediaType);

			if (rxChannel != null) {
				rxChannel.connect(rtpSocket.getReceiveStream());
			}
			if (txChannel != null) {
				txChannel.connect(rtpSocket.getSendStream());
			}

			if (rxChannel != null) {
				rxChannel.start();
			}

			if (txChannel != null) {
				txChannel.start();
			}

			rtpSocket.getReceiveStream().start();
			rtpSocket.getSendStream().start();

			setMode(mode);
		}

		formats = subset;
		createLocalDescriptor();
		setState(ConnectionState.OPEN);
	}

	private int peekDTMF(HashMap<Integer, Format> formats) {
		Set<Integer> keys = formats.keySet();
		int pt = -1;
		for (Integer key : keys) {
			Format f = formats.get(key);
			if (f.matches(AVProfile.DTMF)) {
				pt = key;
				break;
			}
		}

		if (pt > 0) {
			formats.remove(pt);
		}
		return pt;
	}

	public void setOtherParty(Connection other) throws IOException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/**
	 * Finds subset between two sets of formats.
	 * 
	 * @param map
	 *            one set of formats where key is rtp payload type and value is
	 *            RTPFormat Object
	 * @param fmts
	 *            the second set of formats
	 * @return the common subset as map where key is rtp payload type and value
	 *         is RTPFormat Object.
	 */
	private HashMap<Integer, Format> subset(HashMap<Integer, Format> map,
			Collection<Format> fmts) {
		HashMap<Integer, Format> subset = new HashMap();
		for (Integer k : map.keySet()) {
			Format rf = map.get(k);
			for (Format f : fmts) {
				if (f.matches(rf)) {
					subset.put(k, rf);
				}
			}
		}
		return subset;
	}

	@Override
	public void close() {
		EndpointImpl endpoint = (EndpointImpl) getEndpoint();
		Set<String> mediaTypes = formats.keySet();
		for (String media : mediaTypes) {
			RtpSocket socket = rtpSockets.get(media);

			Channel rxChannel = rxChannels.get(media);
			Channel txChannel = txChannels.get(media);

			if (rxChannel != null) {
				rxChannel.stop();
			}

			if (txChannel != null) {
				txChannel.stop();
			}

			if (socket != null) {
				socket.getReceiveStream().stop();
				socket.getSendStream().stop();
			}

			if (rxChannel != null) {
				rxChannel.disconnect(endpoint.getSink(media));
			}

			if (txChannel != null) {
				txChannel.disconnect(endpoint.getSource(media));
			}

			if (rxChannel != null) {
				rxChannel.disconnect(socket.getReceiveStream());
				rxChannel.close();
			}

			if (txChannel != null) {
				txChannel.disconnect(socket.getSendStream());
				txChannel.close();
			}

			if (socket != null) {
				socket.release();
			}
		}

		rxChannels.clear();
		txChannels.clear();

		rtpSockets.clear();
		super.close();
	}

	public void error(Exception e) {
		getEndpoint().deleteConnection(this.getId());
	}

	public long getPacketsReceived(String media) {
		return rtpSockets.get(media).getReceiveStream().getPacketsTransmitted();
	}

	public long getPacketsTransmitted(String media) {
		return rtpSockets.get(media).getSendStream().getBytesReceived();
	}

	protected String getSupportedFormatList(Format[] formats) {
		String s = "";
		for (int i = 0; i < formats.length; i++) {
			s += formats[i] + ";";
		}
		return s;
	}

	@Override
	public String toString() {
		return "RTP Connection [" + getEndpoint().getLocalName() + ", idx="
				+ getIndex() + "]";
	}

	public void txStart(String media) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void rxStart(String media) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void txStop(String media) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void rxStop(String media) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/**
	 * Calculates the configuration of supported RTP formats.
	 * 
	 * @param formats
	 *            the default RTP config. This is a map between supported
	 *            formats and payload numbers
	 * @param rxFormats
	 *            the list of supported rx formats
	 * @param txFormats
	 *            the list of supported tx formats.
	 * @return the map which is an intesection of default map, rx and tx
	 *         formats.
	 */
	private HashMap<Integer, Format> intersection(
			HashMap<Integer, Format> formats, Collection<Format> rxFormats,
			Collection<Format> txFormats) {
		HashMap<Integer, Format> res = new HashMap();
		Set<Integer> types = formats.keySet();
		for (Integer type : types) {
			Format format = formats.get(type);

			// if rtp configured for RFC2833 events we have to add it
			if (format.matches(AVProfile.DTMF)) {
				res.put(type, format);
				continue;
			}
			boolean inrx = rxFormats == null || contains(format, rxFormats)
					|| rxFormats.contains(Format.ANY);
			boolean intx = txFormats == null || contains(format, txFormats)
					|| txFormats.contains(Format.ANY);

			if (inrx && intx) {
				res.put(type, format);
			}
		}
		return res;
	}

	private boolean contains(Format fmt, Collection<Format> list) {
		for (Format f : list) {
			if (f.matches(fmt))
				return true;
		}
		return false;
	}

	private Collection<Attribute> encode(int pt, Format fmt) {
		Vector<Attribute> attributes = new Vector();

		if (fmt instanceof AudioFormat) {
			AudioFormat f = (AudioFormat) fmt;
			attributes.add(sdpFactory.createAttribute("rtpmap", rtpmap(pt, f)));
			if (f.getEncoding().equals(AudioFormat.G729)) {
				attributes.add(sdpFactory.createAttribute("fmtp", pt
						+ " annex=b"));
			}
		} else if (fmt instanceof VideoFormat) {
			VideoFormat f = (VideoFormat) fmt;
			attributes.add(sdpFactory.createAttribute("rtpmap", rtpmap(pt, f)));
			if (f.getEncoding().equals(VideoFormat.H263)) {
				attributes.add(sdpFactory.createAttribute("fmtp", pt
						+ " QCIF=2 CIF=3 MaxBR=1960"));
			}
		}
		return attributes;
	}

	private String rtpmap(int pt, AudioFormat fmt) {
		String encName = fmt.getEncoding().toLowerCase();
		StringBuffer buff = new StringBuffer();
		buff.append(pt);
		buff.append(" ");

		if (encName.equals("alaw")) {
			buff.append("pcma");
		} else if (encName.equals("ulaw")) {
			buff.append("pcmu");
		} else if (encName.equals("linear")) {
			buff.append("l" + fmt.getSampleSizeInBits());
		} else {
			buff.append(encName);
		}

		double sr = fmt.getSampleRate();
		if (sr > 0) {
			buff.append("/");

			if ((sr - (int) sr) < 1E-6) {
				buff.append((int) sr);
			} else {
				buff.append(sr);
			}
		}
		if (fmt.getChannels() > 1) {
			buff.append("/" + fmt.getChannels());
		}

		return buff.toString();
	}

	private String rtpmap(int pt, VideoFormat fmt) {
		String encName = fmt.getEncoding().toLowerCase();
		StringBuffer buff = new StringBuffer();
		buff.append(pt);
		buff.append(" ");
		buff.append(encName);
		buff.append("/");
		buff.append(fmt.getClockRate());
		return buff.toString();
	}

	private void parseAudioFormats(MediaDescription md,
			HashMap<Integer, Format> formats) throws SdpParseException {
		Vector<String> payloads = md.getMedia().getMediaFormats(false);
		for (String payload : payloads) {
			Integer pt = Integer.parseInt(payload);
			// System.out.println("Getting format for " + pt);
			formats.put(pt, avProfile.getAudioFormat(pt));
		}
		Vector<Attribute> attributes = md.getAttributes(false);
		for (Attribute attribute : attributes) {
			if (attribute.getName().equals("rtpmap")) {
				parseAudioFormat(attribute.getValue(), formats);
			}
		}
	}

	private void parseVideoFormats(MediaDescription md,
			HashMap<Integer, Format> formats) throws SdpParseException {
		Vector<String> payloads = md.getMedia().getMediaFormats(false);
		for (String payload : payloads) {
			Integer pt = Integer.parseInt(payload);
			formats.put(pt, avProfile.getVideoFormat(pt));
		}
		Vector<Attribute> attributes = md.getAttributes(false);
		for (Attribute attribute : attributes) {
			if (attribute.getName().equals("rtpmap")) {
				parseVideoFormat(attribute.getValue(), formats);
			}
		}
	}

	private void parseAudioFormat(String rtpmap,
			HashMap<Integer, Format> formats) {
		String tokens[] = rtpmap.toLowerCase().split(" ");

		// split params
		int p = Integer.parseInt(tokens[0]);
		tokens = tokens[1].split("/");

		String encodingName = tokens[0];
		double clockRate = Double.parseDouble(tokens[1]);

		int chans = 1;
		if (tokens.length == 3) {
			chans = Integer.parseInt(tokens[2]);
		}

		if (encodingName.equals("pcmu")) {
			formats.put(p, new AudioFormat(AudioFormat.ULAW, clockRate, 8,
					chans));
		} else if (encodingName.equals("pcma")) {
			formats.put(p, new AudioFormat(AudioFormat.ALAW, clockRate, 8,
					chans));
		} else if (encodingName.equals("speex")) {
			formats.put(p, new AudioFormat(AudioFormat.SPEEX, clockRate,
					AudioFormat.NOT_SPECIFIED, chans));
		} else if (encodingName.equals("telephone-event")) {
			formats.put(p, new AudioFormat("telephone-event", clockRate,
					AudioFormat.NOT_SPECIFIED, AudioFormat.NOT_SPECIFIED));
		} else if (encodingName.equals("g729")) {
			formats.put(p, new AudioFormat(AudioFormat.G729, clockRate,
					AudioFormat.NOT_SPECIFIED, chans));
		} else if (encodingName.equals("gsm")) {
			formats.put(p, new AudioFormat(AudioFormat.GSM, clockRate,
					AudioFormat.NOT_SPECIFIED, chans));
		} else if (encodingName.equals("l16")) {
			formats.put(p, new AudioFormat(AudioFormat.LINEAR, clockRate, 16,
					chans, AudioFormat.LITTLE_ENDIAN, AudioFormat.SIGNED));
		}
	}

	private void parseVideoFormat(String rtpmap,
			HashMap<Integer, Format> formats) {
		String tokens[] = rtpmap.toLowerCase().split(" ");

		// split params
		int p = Integer.parseInt(tokens[0]);
		tokens = tokens[1].split("/");

		String encodingName = tokens[0];
		double clockRate = Double.parseDouble(tokens[1]);

		formats.put(p, new VideoFormat(encodingName, 25, (int) clockRate));
	}

	public void setOtherParty(String media, InetSocketAddress address)
			throws IOException {
		rtpSockets.get(media).setPeer(address.getAddress(), address.getPort());
	}

}
