package org.mobicents.javax.media.mscontrol;

import jain.protocol.ip.mgcp.message.parms.CallIdentifier;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.media.mscontrol.Configuration;
import javax.media.mscontrol.MediaConfig;
import javax.media.mscontrol.MediaObject;
import javax.media.mscontrol.MediaSession;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.Parameter;
import javax.media.mscontrol.Parameters;
import javax.media.mscontrol.mediagroup.MediaGroup;
import javax.media.mscontrol.mixer.MediaMixer;
import javax.media.mscontrol.networkconnection.NetworkConnection;
import javax.media.mscontrol.vxml.VxmlDialog;

import org.apache.log4j.Logger;
import org.mobicents.javax.media.mscontrol.mediagroup.MediaGroupImpl;
import org.mobicents.javax.media.mscontrol.mixer.MediaMixerImpl;
import org.mobicents.javax.media.mscontrol.networkconnection.NetworkConnectionImpl;
import org.mobicents.jsr309.mgcp.MgcpWrapper;

/**
 * 
 * @author amit bhayani
 * 
 */
public class MediaSessionImpl implements MediaSession {
	private static final Logger logger = Logger.getLogger(MediaSessionImpl.class);

	// The default Session Timeout value
	public static final int SESSION_TIMEOUT = 30000;

	private MgcpWrapper mgcpWrapper;
	private static CallIdentifier callIdentifier = null;

	private final MsControlFactoryImpl msControlFactoryImpl;

	private URI uri = null;

	List<NetworkConnection> netConnList = new ArrayList<NetworkConnection>();
	List<MediaGroup> medGrpList = new ArrayList<MediaGroup>();
	List<MediaMixer> medMxrList = new ArrayList<MediaMixer>();

	private Map attributeMap = new HashMap();

	public MediaSessionImpl(MgcpWrapper mgcpWrapper, MsControlFactoryImpl msControlFactoryImpl) {
		this.mgcpWrapper = mgcpWrapper;
		this.msControlFactoryImpl = msControlFactoryImpl;

		// callIdentifier acts as media session id
		if(this.callIdentifier == null) this.callIdentifier = this.mgcpWrapper.getUniqueCallIdentifier();
		try {
			this.uri = new URI("mscontrol://" + this.mgcpWrapper.getPeerIp() + "/ms" + this.callIdentifier.toString());
		} catch (URISyntaxException e) {
			logger.error(e);
		}

	}

	public MediaGroup createMediaGroup(Configuration<MediaGroup> paramConfiguration) throws MsControlException {
		if(paramConfiguration == null){
			throw new MsControlException("Configuration<MediaGroup> cannot be null");
		}
		MediaConfigImpl config = (MediaConfigImpl) this.msControlFactoryImpl.getMediaConfig(paramConfiguration);
		MediaGroup mediaGroupImpl = new MediaGroupImpl(this, mgcpWrapper, config);
		medGrpList.add(mediaGroupImpl);
		return mediaGroupImpl;
	}

	public MediaGroup createMediaGroup(Configuration<MediaGroup> paramConfiguration, Parameters paramParameters)
			throws MsControlException {
		if(paramConfiguration == null){
			throw new MsControlException("Configuration<MediaGroup> cannot be null");
		}		
		MediaConfigImpl config = (MediaConfigImpl) this.msControlFactoryImpl.getMediaConfig(paramConfiguration);
		config = (MediaConfigImpl) config.createCustomizedClone(paramParameters);

		MediaGroup mediaGroupImpl = new MediaGroupImpl(this, mgcpWrapper, config);
		medGrpList.add(mediaGroupImpl);
		return mediaGroupImpl;
	}

	public MediaGroup createMediaGroup(MediaConfig paramMediaConfig, Parameters paramParameters)
			throws MsControlException {
		if(paramMediaConfig == null){
			throw new MsControlException("MediaConfig cannot be null");
		}		
		paramMediaConfig = paramMediaConfig.createCustomizedClone(paramParameters);
		MediaGroup mediaGroupImpl = new MediaGroupImpl(this, mgcpWrapper, (MediaConfigImpl) paramMediaConfig);
		medGrpList.add(mediaGroupImpl);
		return mediaGroupImpl;
	}

	public MediaMixer createMediaMixer(Configuration<MediaMixer> paramConfiguration) throws MsControlException {
		if(paramConfiguration == null){
			throw new MsControlException("Configuration<MediaMixer> cannot be null");
		}
		MediaConfigImpl config = (MediaConfigImpl) this.msControlFactoryImpl.getMediaConfig(paramConfiguration);
		MediaMixerImpl mediaMixerImpl = new MediaMixerImpl(this, mgcpWrapper, config);
		medMxrList.add(mediaMixerImpl);
		return mediaMixerImpl;
	}

	public MediaMixer createMediaMixer(Configuration<MediaMixer> paramConfiguration, Parameters paramParameters)
			throws MsControlException {
		if(paramConfiguration == null){
			throw new MsControlException("Configuration<MediaMixer> cannot be null");
		}
		MediaConfigImpl config = (MediaConfigImpl) this.msControlFactoryImpl.getMediaConfig(paramConfiguration);
		config = (MediaConfigImpl) config.createCustomizedClone(paramParameters);

		MediaMixerImpl mediaMixerImpl = new MediaMixerImpl(this, mgcpWrapper, config);
		medMxrList.add(mediaMixerImpl);
		return mediaMixerImpl;
	}

	public MediaMixer createMediaMixer(MediaConfig paramMediaConfig, Parameters paramParameters)
			throws MsControlException {
		if(paramMediaConfig == null){
			throw new MsControlException("MediaConfig cannot be null");
		}		
		paramMediaConfig = paramMediaConfig.createCustomizedClone(paramParameters);
		MediaMixerImpl mediaMixerImpl = new MediaMixerImpl(this, mgcpWrapper, (MediaConfigImpl) paramMediaConfig);
		medMxrList.add(mediaMixerImpl);
		return mediaMixerImpl;
	}

	public NetworkConnection createNetworkConnection(Configuration<NetworkConnection> paramConfiguration)
			throws MsControlException {
		if(paramConfiguration == null){
			throw new MsControlException("Configuration<NetworkConnection> cannot be null");
		}	
		MediaConfigImpl config = (MediaConfigImpl) this.msControlFactoryImpl.getMediaConfig(paramConfiguration);
		NetworkConnectionImpl networkConnectionImpl = new NetworkConnectionImpl(this, mgcpWrapper, config);
		netConnList.add(networkConnectionImpl);
		return networkConnectionImpl;
	}

	public NetworkConnection createNetworkConnection(Configuration<NetworkConnection> paramConfiguration,
			Parameters paramParameters) throws MsControlException {
		if(paramConfiguration == null){
			throw new MsControlException("Configuration<NetworkConnection> cannot be null");
		}
		MediaConfigImpl config = (MediaConfigImpl) this.msControlFactoryImpl.getMediaConfig(paramConfiguration);
		config = (MediaConfigImpl) config.createCustomizedClone(paramParameters);

		NetworkConnectionImpl networkConnectionImpl = new NetworkConnectionImpl(this, mgcpWrapper, config);
		netConnList.add(networkConnectionImpl);
		return networkConnectionImpl;
	}

	public NetworkConnection createNetworkConnection(MediaConfig paramMediaConfig, Parameters paramParameters)
			throws MsControlException {
		if(paramMediaConfig == null){
			throw new MsControlException("MediaConfig cannot be null");
		}		
		paramMediaConfig = paramMediaConfig.createCustomizedClone(paramParameters);
		NetworkConnectionImpl networkConnectionImpl = new NetworkConnectionImpl(this, mgcpWrapper,
				(MediaConfigImpl) paramMediaConfig);
		netConnList.add(networkConnectionImpl);
		return networkConnectionImpl;
	}

	public VxmlDialog createVxmlDialog(Parameters paramParameters) throws MsControlException {
		throw new MsControlException("VxmlDialog is not yet supported");
//		VxmlDialogImpl vxmlDialogImpl = new VxmlDialogImpl(this, mgcpWrapper, paramParameters);
//		return vxmlDialogImpl;
	}

	public Object getAttribute(String paramString) {
		return attributeMap.get(paramString);
	}

	public Iterator<String> getAttributeNames() {
		return attributeMap.keySet().iterator();
	}

	public void removeAttribute(String paramString) {
		attributeMap.remove(paramString);

	}

	public void setAttribute(String paramString, Object paramObject) {
		attributeMap.put(paramString, paramObject);

	}

	public Parameters createParameters() {
		return new ParametersImpl();
	}

	public Iterator<MediaObject> getMediaObjects() {
		// TODO Auto-generated method stub
		return null;
	}

	public <T extends MediaObject> Iterator<T> getMediaObjects(Class<T> paramClass) {
		// TODO Auto-generated method stub
		return null;
	}

	public Parameters getParameters(Parameter[] paramArrayOfParameter) {
		// TODO Auto-generated method stub
		return null;
	}

	public URI getURI() {
		return this.uri;
	}

	public void release() {

		while (medGrpList.size() != 0) {
			MediaGroup mg = medGrpList.get(0);
			mg.release();
		}

		while (medMxrList.size() != 0) {
			MediaMixer mx = medMxrList.get(0);
			mx.release();
		}

		while (netConnList.size() != 0) {
			NetworkConnection nc = netConnList.get(0);
			nc.release();
		}

		this.attributeMap.clear();

		this.msControlFactoryImpl.getMediaSessionList().remove(this);
	}

	public void setParameters(Parameters paramParameters) {
		// TODO Auto-generated method stub

	}

	public CallIdentifier getCallIdentifier() {
		return this.callIdentifier;
	}

	public List<NetworkConnection> getNetConnList() {
		return netConnList;
	}

	public List<MediaGroup> getMedGrpList() {
		return medGrpList;
	}

	public List<MediaMixer> getMedMxrList() {
		return medMxrList;
	}

	public MsControlFactoryImpl getMsControlFactoryImpl() {
		return this.msControlFactoryImpl;
	}

}