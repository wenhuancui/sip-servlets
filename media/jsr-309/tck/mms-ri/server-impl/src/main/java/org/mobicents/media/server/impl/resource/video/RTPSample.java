package org.mobicents.media.server.impl.resource.video;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 
 * @author amit bhayani
 *
 */
public class RTPSample {
	private int packetCount = 0;
	private int reserved;
	RTPLocalPacket[] rtpLocalPackets = null;
	byte[] extradata = null;
	
	/**
	 * Sample Period in MicroSeconds
	 */
	private int samplePeriod = 0;

	private int index = 0;

	public int getSamplePeriod() {
		return samplePeriod;
	}

	public void setSamplePeriod(int samplePeriod) {
		this.samplePeriod = samplePeriod;
	}

	public int getPacketCount() {
		return packetCount;
	}

	public void setPacketCount(int packetCount) {
		this.packetCount = packetCount;
		rtpLocalPackets = new RTPLocalPacket[packetCount];
	}

	public int getReserved() {
		return reserved;
	}

	public void setReserved(int reserved) {
		this.reserved = reserved;
	}

	public RTPLocalPacket[] getRtpLocalPackets() {
		return rtpLocalPackets;
	}

	public void addRtpLocalPackets(RTPLocalPacket rtpLocalPacket) {
		this.rtpLocalPackets[index++] = rtpLocalPacket;
	}

	public byte[] getExtradata() {
		return extradata;
	}

	public void setExtradata(byte[] extradata) {
		this.extradata = extradata;
	}

	public byte[] toByteArray(long ssrc) throws IOException {
		if (this.packetCount == 0) {
			return null;
		} else if (this.packetCount == 1) {
			return this.rtpLocalPackets[0].toByteArray(ssrc);
		} else {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			for (RTPLocalPacket rtpLocalPacket : this.rtpLocalPackets) {
				bout.write(rtpLocalPacket.toByteArray(ssrc));
			}
			return bout.toByteArray();
		}
	}

}
