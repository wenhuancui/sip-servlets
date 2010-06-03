package org.mobicents.javax.media.mscontrol.mixer;

import jain.protocol.ip.mgcp.message.parms.ConnectionIdentifier;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;

import javax.media.mscontrol.MediaConfig;
import javax.media.mscontrol.MediaObject;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.Parameter;
import javax.media.mscontrol.Parameters;
import javax.media.mscontrol.mixer.MediaMixer;
import javax.media.mscontrol.mixer.MixerAdapter;
import javax.media.mscontrol.resource.Action;
import javax.media.mscontrol.resource.AllocationEventListener;

import org.apache.log4j.Logger;
import org.mobicents.javax.media.mscontrol.AbstractJoinableContainer;
import org.mobicents.javax.media.mscontrol.MediaConfigImpl;
import org.mobicents.javax.media.mscontrol.MediaObjectState;
import org.mobicents.javax.media.mscontrol.MediaSessionImpl;
import org.mobicents.javax.media.mscontrol.SupportedFeaturesImpl;
import org.mobicents.jsr309.mgcp.MgcpWrapper;

public class MixerAdapterImpl extends AbstractJoinableContainer implements MixerAdapter {

	public static Logger logger = Logger.getLogger(MixerAdapterImpl.class);

	private URI uri = null;
	private final MediaConfigImpl config;
	private final MixerAdapterConfig mixAdapterConfig;
	private final MediaMixerImpl mediaMixerImpl;
	private Parameters parameters = null;

	public MixerAdapterImpl(MediaMixerImpl mediaMixerImpl, MediaSessionImpl mediaSession, MgcpWrapper mgcpWrapper,
			MediaConfigImpl config, Parameters params) throws MsControlException {
		super();
		this.config = config;
		this.mediaMixerImpl = mediaMixerImpl;
		this.mixAdapterConfig = ((SupportedFeaturesImpl) this.config.getSupportedFeatures()).getMixAdaConfig()
				.createCustomizedClone(params);

		this.parameters = this.mixAdapterConfig.getParametersImpl();

		String passedId = (String) this.parameters.get(MEDIAOBJECT_ID);
		if (passedId != null) {

			if (!Character.isLetterOrDigit(passedId.charAt(0))) {
				throw new MsControlException(
						"MEDIAOBJECT_ID should start with letter or digit. Invalid first character for passed MEDIAOBJECT_ID = "
								+ passedId);
			}

			for (MixerAdapter mixAda : this.mediaMixerImpl.mxAdaptList) {
				if (((AbstractJoinableContainer) mixAda).getId().compareTo(passedId) == 0) {
					throw new MsControlException("Duplicate MEDIAOBJECT_ID = " + passedId);
				}
			}
			this.id = passedId;
		}

		try {
			this.uri = new URI(mediaSession.getURI().toString() + "/MediaMixer." + this.id);
		} catch (URISyntaxException e) {
			// Ignore
		}
	}

	@Override
	protected void checkState() {
		// TODO Auto-generated method stub

	}

	@Override
	protected MediaObjectState getState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public URI getURI() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void joined(ConnectionIdentifier thisConnId, ConnectionIdentifier otherConnId) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void resetContainer() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void unjoined(ConnectionIdentifier thisConnId, ConnectionIdentifier otherConnId) {
		// TODO Auto-generated method stub

	}

	public void confirm() throws MsControlException {
		// TODO Auto-generated method stub

	}

	public MediaConfig getConfig() {
		// TODO Auto-generated method stub
		return null;
	}

	public <R> R getResource(Class<R> arg0) throws MsControlException {
		// TODO Auto-generated method stub
		return null;
	}

	public void triggerAction(Action arg0) {
		// TODO Auto-generated method stub

	}

	public Parameters createParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	public Iterator<MediaObject> getMediaObjects() {
		// TODO Auto-generated method stub
		return null;
	}

	public <T extends MediaObject> Iterator<T> getMediaObjects(Class<T> arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public Parameters getParameters(Parameter[] arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public void release() {
		this.mediaMixerImpl.mxAdaptList.remove(this);
	}

	public void setParameters(Parameters arg0) {
		// TODO Auto-generated method stub

	}

	public void addListener(AllocationEventListener arg0) {
		// TODO Auto-generated method stub

	}

	public void removeListener(AllocationEventListener arg0) {
		// TODO Auto-generated method stub

	}

}
