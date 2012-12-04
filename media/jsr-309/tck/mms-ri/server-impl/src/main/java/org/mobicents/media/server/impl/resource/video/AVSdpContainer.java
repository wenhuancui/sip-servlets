package org.mobicents.media.server.impl.resource.video;

/**
 * 
 * @author amit bhayani
 * 
 */
public class AVSdpContainer {
	private String audioSdp = null;
	private String videoSdp = null;
	
	private String url = null;
	private int audioTrackId = -1;
	private int videoTrackId = -1;

	public AVSdpContainer() {
		super();

	}

	public String getAudioSdp() {
		return audioSdp;
	}

	public String getVideoSdp() {
		return videoSdp;
	}

}
