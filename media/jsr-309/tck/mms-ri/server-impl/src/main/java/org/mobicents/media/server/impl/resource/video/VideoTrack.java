package org.mobicents.media.server.impl.resource.video;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * 
 * @author amit bhayani
 *
 */
public class VideoTrack extends RTPTrack {

	public VideoTrack(TrackBox videoTrackBox, TrackBox videoHintTrackBox, File file) throws FileNotFoundException {
		super(videoTrackBox, videoHintTrackBox, file);
	}
}
