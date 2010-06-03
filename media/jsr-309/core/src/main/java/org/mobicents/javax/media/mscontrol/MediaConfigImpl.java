package org.mobicents.javax.media.mscontrol;

import javax.media.mscontrol.MediaConfig;
import javax.media.mscontrol.MediaConfigException;
import javax.media.mscontrol.Parameters;
import javax.media.mscontrol.SupportedFeatures;
import javax.media.mscontrol.join.JoinableStream.StreamType;

/**
 * 
 * @author amit bhayani
 *
 */
public class MediaConfigImpl implements MediaConfig {

	private SupportedFeaturesImpl suppFeat = null;

	protected MediaConfigImpl(SupportedFeaturesImpl suppFeat) {
		this.suppFeat = suppFeat;
	}

	public MediaConfig createCustomizedClone(Parameters params) throws MediaConfigException {

		SupportedFeaturesImpl suppFeatClone = this.suppFeat.createCustomizedClone(params);
		return new MediaConfigImpl(suppFeatClone);
	}

	public SupportedFeatures getSupportedFeatures() {
		return this.suppFeat;
	}

	public boolean hasStream(StreamType arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public String marshall() {
		// TODO Auto-generated method stub
		return null;
	}
}
