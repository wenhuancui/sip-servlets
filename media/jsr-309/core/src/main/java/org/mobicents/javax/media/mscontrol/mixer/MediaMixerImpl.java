package org.mobicents.javax.media.mscontrol.mixer;

import jain.protocol.ip.mgcp.message.parms.ConnectionIdentifier;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.media.mscontrol.Configuration;
import javax.media.mscontrol.MediaConfig;
import javax.media.mscontrol.MediaEvent;
import javax.media.mscontrol.MediaEventListener;
import javax.media.mscontrol.MediaObject;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.Parameter;
import javax.media.mscontrol.Parameters;
import javax.media.mscontrol.join.Joinable;
import javax.media.mscontrol.mixer.MediaMixer;
import javax.media.mscontrol.mixer.MixerAdapter;
import javax.media.mscontrol.mixer.MixerEvent;
import javax.media.mscontrol.resource.Action;
import javax.media.mscontrol.resource.AllocationEventListener;
import javax.media.mscontrol.resource.enums.ParameterEnum;

import org.apache.log4j.Logger;
import org.mobicents.javax.media.mscontrol.AbstractJoinableContainer;
import org.mobicents.javax.media.mscontrol.MediaConfigImpl;
import org.mobicents.javax.media.mscontrol.MediaObjectState;
import org.mobicents.javax.media.mscontrol.MediaSessionImpl;
import org.mobicents.javax.media.mscontrol.ParametersImpl;
import org.mobicents.javax.media.mscontrol.SupportedFeaturesImpl;
import org.mobicents.javax.media.mscontrol.resource.ExtendedParameter;
import org.mobicents.jsr309.mgcp.MgcpWrapper;

/**
 * 
 * @author amit bhayani
 * 
 */
public class MediaMixerImpl extends AbstractJoinableContainer implements MediaMixer {

	public static Logger logger = Logger.getLogger(MediaMixerImpl.class);

	private String CONF_ENDPOINT_NAME = "/mobicents/media/cnf/$";

	private URI uri = null;
	private final MediaConfigImpl config;
	private final MediaMixerConfig medMixConfig;
	private Parameters parameters = null;

	protected List<MixerAdapter> mxAdaptList = new ArrayList<MixerAdapter>();

	protected CopyOnWriteArrayList<MediaEventListener<? extends MediaEvent<?>>> mediaEventListenerList = new CopyOnWriteArrayList<MediaEventListener<? extends MediaEvent<?>>>();

	public MediaMixerImpl(MediaSessionImpl mediaSession, MgcpWrapper mgcpWrapper, MediaConfigImpl config)
			throws MsControlException {
		super();
		this.config = config;

		this.medMixConfig = ((SupportedFeaturesImpl) this.config.getSupportedFeatures()).getMedMixConfig();

		this.parameters = this.medMixConfig.getParametersImpl();

		this.mediaSession = mediaSession;

		String passedId = (String) this.parameters.get(MEDIAOBJECT_ID);
		if (passedId != null) {

			if (!Character.isLetterOrDigit(passedId.charAt(0))) {
				throw new MsControlException(
						"MEDIAOBJECT_ID should start with letter or digit. Invalid first character for passed MEDIAOBJECT_ID = "
								+ passedId);
			}

			for (MediaMixer medMix : this.mediaSession.getMedMxrList()) {
				if (((AbstractJoinableContainer) medMix).getId().compareTo(passedId) == 0) {
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

		this.mgcpWrapper = mgcpWrapper;

		this.endpoint = (String) this.parameters.get(ExtendedParameter.ENDPOINT_LOCAL_NAME);
		this.CONF_ENDPOINT_NAME = this.endpoint;

		this.maxJoinees = (Integer) this.parameters.get(ParameterEnum.MAX_PORTS);
	}

	public MixerAdapter createMixerAdapter(Configuration<MixerAdapter> paramConfiguration) throws MsControlException {

		if (paramConfiguration == null) {
			throw new MsControlException("Configuration<MixerAdapter> cannot be null");
		}

		MediaConfigImpl config = (MediaConfigImpl) this.mediaSession.getMsControlFactoryImpl().getMediaConfig(
				paramConfiguration);
		MixerAdapter mixerAdapter = new MixerAdapterImpl(this, this.mediaSession, this.mgcpWrapper, config, null);
		mxAdaptList.add(mixerAdapter);
		return mixerAdapter;
	}

	public MixerAdapter createMixerAdapter(Configuration<MixerAdapter> paramConfiguration, Parameters paramParameters)
			throws MsControlException {
		if (paramConfiguration == null) {
			throw new MsControlException("Configuration<MixerAdapter> cannot be null");
		}

		MediaConfigImpl config = (MediaConfigImpl) this.mediaSession.getMsControlFactoryImpl().getMediaConfig(
				paramConfiguration);
		MixerAdapter mixerAdapter = new MixerAdapterImpl(this, this.mediaSession, this.mgcpWrapper, config,
				paramParameters);
		mxAdaptList.add(mixerAdapter);
		return mixerAdapter;
	}

	public MixerAdapter createMixerAdapter(MediaConfig paramMediaConfig, Parameters paramParameters)
			throws MsControlException {
		if (paramMediaConfig == null) {
			throw new MsControlException("MediaConfig cannot be null");
		}

		MixerAdapter mixerAdapter = new MixerAdapterImpl(this, this.mediaSession, this.mgcpWrapper,
				(MediaConfigImpl) paramMediaConfig, paramParameters);
		mxAdaptList.add(mixerAdapter);
		return mixerAdapter;

	}

	@Override
	protected void checkState() {
		if (this.state.equals(MediaObjectState.RELEASED)) {
			throw new IllegalStateException("State of container " + this.getURI() + "is released");
		}
	}

	@Override
	protected MediaObjectState getState() {
		return this.state;
	}

	@Override
	public URI getURI() {
		return this.uri;
	}

	@Override
	protected void joined(ConnectionIdentifier thisConnId, ConnectionIdentifier otherConnId) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void resetContainer() {
		this.endpoint = CONF_ENDPOINT_NAME;
	}

	@Override
	protected void unjoined(ConnectionIdentifier thisConnId, ConnectionIdentifier otherConnId) {
		// TODO Auto-generated method stub

	}

	public void confirm() throws MsControlException {
		// TODO Auto-generated method stub

	}

	public MediaConfig getConfig() {
		return this.config;
	}

	public <R> R getResource(Class<R> resource) throws MsControlException {
		// TODO Auto-generated method stub
		return null;
	}

	public void triggerRTC(Action rtca) {
		// TODO Auto-generated method stub

	}

	public Parameters createParameters() {
		return new ParametersImpl();
	}

	public Parameters getParameters(Parameter[] params) {
		return this.parameters;
	}

	public void release() {
		checkState();

		try {
			Joinable[] joinableArray = this.getJoinees();
			for (Joinable joinable : joinableArray) {
				this.unjoinInitiate(joinable, this);
			}
		} catch (MsControlException e) {
			logger.error("release of NetworkConnection failed ", e);
		}

		this.state = MediaObjectState.RELEASED;

		this.mediaSession.getMedMxrList().remove(this);
	}

	public void setParameters(Parameters params) {
		// TODO Auto-generated method stub

	}

	public void addListener(MediaEventListener<MixerEvent> listener) {
		this.mediaEventListenerList.add(listener);
	}

	public void removeListener(MediaEventListener<MixerEvent> listener) {
		this.mediaEventListenerList.remove(listener);
	}

	public Iterator<MediaObject> getMediaObjects() {
		// TODO Auto-generated method stub
		return null;
	}

	public <T extends MediaObject> Iterator<T> getMediaObjects(Class<T> paramClass) {
		// TODO Auto-generated method stub
		return null;
	}

	public void addListener(AllocationEventListener paramAllocationEventListener) {
		// TODO Auto-generated method stub

	}

	public void removeListener(AllocationEventListener paramAllocationEventListener) {
		// TODO Auto-generated method stub

	}

	public void triggerAction(Action arg0) {
		// TODO Auto-generated method stub

	}

}
