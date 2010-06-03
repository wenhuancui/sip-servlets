package org.mobicents.media.server;

import gov.nist.javax.sdp.fields.AttributeField;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import javax.sdp.Attribute;
import javax.sdp.MediaDescription;
import javax.sdp.SdpFactory;
import javax.sdp.SessionDescription;

import org.mobicents.media.Format;
import org.mobicents.media.format.AudioFormat;
import org.mobicents.media.format.VideoFormat;

public class SDPFactoryTest {

	public final static AudioFormat DTMF = new AudioFormat("telephone-event", 8000, AudioFormat.NOT_SPECIFIED,
			AudioFormat.NOT_SPECIFIED);
	SdpFactory sdpFactory = SdpFactory.getInstance();
	String rawSdpString = "v=0\r\n" + "o=tb640 4508814461704889920 1418150179918558920 IN IP4 74.51.38.149\r\n"
			+ "s=-\r\n" + "c=IN IP4 74.51.38.140\r\n" + "t=0 0\r\n"
			+ "m=audio 20654 RTP/AVP 0 8 18 4 3 96 97 13 101\r\n" + "a=rtpmap:96 iLBC/8000\r\n"
			+ "a=rtpmap:97 iLBC/8000\r\n" + "a=fmtp:97 mode=20\r\n" + "a=rtpmap:101 telephone-event/8000\r\n"
			+ "a=fmtp:101 0-16\r\n";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SDPFactoryTest obj = new SDPFactoryTest();
		try {
			obj.test();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void test() throws Exception {
		SessionDescription rawSdp = sdpFactory.createSessionDescription(rawSdpString);

		SessionDescription newSdp = sdpFactory.createSessionDescription();

		newSdp.setVersion(rawSdp.getVersion());
		newSdp.setOrigin(rawSdp.getOrigin());
		newSdp.setSessionName(rawSdp.getSessionName());
		newSdp.setConnection(rawSdp.getConnection());
		newSdp.setTimeDescriptions(rawSdp.getTimeDescriptions(false));

		int port = 0;
		Vector<MediaDescription> mediaDescriptions = rawSdp.getMediaDescriptions(false);
		for (MediaDescription md : mediaDescriptions) {
			String mediaType = md.getMedia().getMediaType();
			if (mediaType.equals("audio")) {
				port = md.getMedia().getMediaPort();
				//break;
				Iterator<String> payloads = md.getMedia().getMediaFormats(false).iterator();
				// for (String payload: payloads) {
				while (payloads.hasNext()) {
					Integer pt = Integer.parseInt(payloads.next());
					if (pt == 0 || pt == 101) {
						System.out.println("PCMU or DTMF");
					} else {
						payloads.remove();
					}
				}
				// }
				
				
				Iterator<AttributeField> attributes = md.getAttributes(false).iterator();
				while (attributes.hasNext()) {
					
					if(!attributes.next().getValue().contains("101")){
						attributes.remove();
					}
					
				}

			}
		}

		System.out.println("Port = " + port);

		// MediaDescription md = sdpFactory.createMediaDescription("audio", port, 1, "RTP/AVP", new int[0]);
		// Vector descriptions = new Vector();
		// HashMap<Integer, Format> map = new HashMap<Integer, Format>();
		// map.put(0, AVProfile.PCMU);
		// map.put(101, DTMF);
		//		
		// Set<Integer> keys = map.keySet();
		// for (Integer key : keys) {
		// md.getMedia().getMediaFormats(true).add(key);
		// md.getAttributes(true).addAll(this.encode(key, map.get(key)));
		// }
		// descriptions.add(md);
		//        
		// newSdp.setMediaDescriptions(descriptions);

		System.out.println("New SDP = \n" + rawSdp);

	}

	private Collection<Attribute> encode(int pt, Format fmt) {
		Vector<Attribute> attributes = new Vector();

		if (fmt instanceof AudioFormat) {
			AudioFormat f = (AudioFormat) fmt;
			attributes.add(sdpFactory.createAttribute("rtpmap", rtpmap(pt, f)));
			if (f.getEncoding().equals(AudioFormat.G729)) {
				attributes.add(sdpFactory.createAttribute("fmtp", pt + " annex=b"));
			}
		} else if (fmt instanceof VideoFormat) {
			VideoFormat f = (VideoFormat) fmt;
			attributes.add(sdpFactory.createAttribute("rtpmap", rtpmap(pt, f)));
			if (f.getEncoding().equals(VideoFormat.H263)) {
				attributes.add(sdpFactory.createAttribute("fmtp", pt + " QCIF=2 CIF=3 MaxBR=1960"));
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

}
