package org.mobicents.javax.media.mscontrol.networkconnection;

import jain.protocol.ip.mgcp.JainMgcpCommandEvent;
import jain.protocol.ip.mgcp.JainMgcpEvent;
import jain.protocol.ip.mgcp.JainMgcpResponseEvent;
import jain.protocol.ip.mgcp.message.Constants;
import jain.protocol.ip.mgcp.message.CreateConnection;
import jain.protocol.ip.mgcp.message.CreateConnectionResponse;
import jain.protocol.ip.mgcp.message.DeleteConnection;
import jain.protocol.ip.mgcp.message.DeleteConnectionResponse;
import jain.protocol.ip.mgcp.message.ModifyConnection;
import jain.protocol.ip.mgcp.message.ModifyConnectionResponse;
import jain.protocol.ip.mgcp.message.parms.CallIdentifier;
import jain.protocol.ip.mgcp.message.parms.ConflictingParameterException;
import jain.protocol.ip.mgcp.message.parms.ConnectionDescriptor;
import jain.protocol.ip.mgcp.message.parms.ConnectionIdentifier;
import jain.protocol.ip.mgcp.message.parms.ConnectionMode;
import jain.protocol.ip.mgcp.message.parms.EndpointIdentifier;
import jain.protocol.ip.mgcp.message.parms.ReturnCode;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.media.mscontrol.EventType;
import javax.media.mscontrol.MediaConfig;
import javax.media.mscontrol.MediaErr;
import javax.media.mscontrol.MediaEvent;
import javax.media.mscontrol.MediaEventListener;
import javax.media.mscontrol.MediaObject;
import javax.media.mscontrol.MediaSession;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.Parameter;
import javax.media.mscontrol.Parameters;
import javax.media.mscontrol.join.JoinEvent;
import javax.media.mscontrol.join.Joinable;
import javax.media.mscontrol.networkconnection.CodecPolicy;
import javax.media.mscontrol.networkconnection.NetworkConnection;
import javax.media.mscontrol.networkconnection.SdpPortManager;
import javax.media.mscontrol.networkconnection.SdpPortManagerEvent;
import javax.media.mscontrol.networkconnection.SdpPortManagerException;
import javax.media.mscontrol.resource.Action;
import javax.media.mscontrol.resource.AllocationEventListener;
import javax.sdp.MediaDescription;
import javax.sdp.SdpException;
import javax.sdp.SdpFactory;
import javax.sdp.SdpParseException;
import javax.sdp.SessionDescription;

import org.apache.log4j.Logger;
import org.mobicents.javax.media.mscontrol.AbstractJoinableContainer;
import org.mobicents.javax.media.mscontrol.AudioJoinableStream;
import org.mobicents.javax.media.mscontrol.EventExecutor;
import org.mobicents.javax.media.mscontrol.MediaConfigImpl;
import org.mobicents.javax.media.mscontrol.MediaObjectState;
import org.mobicents.javax.media.mscontrol.MediaSessionImpl;
import org.mobicents.javax.media.mscontrol.ParametersImpl;
import org.mobicents.javax.media.mscontrol.SupportedFeaturesImpl;
import org.mobicents.javax.media.mscontrol.resource.ExtendedParameter;
import org.mobicents.jsr309.mgcp.MgcpWrapper;
import org.mobicents.jsr309.mgcp.Provider;
import org.mobicents.protocols.mgcp.stack.JainMgcpExtendedListener;


/**
 * 
 * @author amit bhayani
 * 
 */
public final class NetworkConnectionImpl extends AbstractJoinableContainer implements NetworkConnection {

	public static Logger logger = Logger.getLogger(NetworkConnectionImpl.class);

	private static final SdpFactory sdpFactory = SdpFactory.getInstance();

	private String PR_ENDPOINT_NAME = null;
	private URI uri = null;

	private final SupportedFeaturesImpl suppFeatures;
	private final NetworkConnectionConfig ncConfig;

	private volatile ConnectionIdentifier connectionIdentifier = null;

	private byte[] remoteSessionDescription = null;
	private byte[] localSessionDescription = null;

	private SdpPortManager sdpPortManager = null;

	private Parameters parameters = null;

	protected CopyOnWriteArrayList<MediaEventListener<? extends MediaEvent<?>>> eventListenerList = new CopyOnWriteArrayList<MediaEventListener<? extends MediaEvent<?>>>();

	public NetworkConnectionImpl(MediaSessionImpl mediaSession, MgcpWrapper mgcpWrapper, MediaConfigImpl config)
			throws MsControlException {
		super();
		this.config = config;
		this.suppFeatures = (SupportedFeaturesImpl) this.config.getSupportedFeatures();

		this.ncConfig = this.suppFeatures.getNetConConfig();

		this.parameters = this.ncConfig.getParametersImpl();
		this.mediaSession = mediaSession;

		String passedId = (String) this.parameters.get(MEDIAOBJECT_ID);

		if (passedId != null) {

			if (!Character.isLetterOrDigit(passedId.charAt(0))) {
				throw new MsControlException(
						"MEDIAOBJECT_ID should start with letter or digit. Invalid first character for passed MEDIAOBJECT_ID = "
								+ passedId);
			}

			for (NetworkConnection nc : this.mediaSession.getNetConnList()) {
				if (((AbstractJoinableContainer) nc).getId().compareTo(passedId) == 0) {
					throw new MsControlException("Duplicate MEDIAOBJECT_ID = " + passedId);
				}
			}
			this.id = passedId;
		}

		try {
			this.uri = new URI(mediaSession.getURI().toString() + "/NetworkConnection." + this.id);
		} catch (URISyntaxException e) {
			// Ignore
		}

		this.mgcpWrapper = mgcpWrapper;
		this.maxJoinees = 1;

		this.endpoint = (String) this.ncConfig.getParametersImpl().get(ExtendedParameter.ENDPOINT_LOCAL_NAME);
		this.PR_ENDPOINT_NAME = this.endpoint;

		sdpPortManager = new SdpPortManagerImpl(this);
	}

	@Override
	protected void checkState() {
		if (this.state.equals(MediaObjectState.RELEASED)) {
			throw new IllegalStateException("State of container " + this.getURI() + " is released");
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
		// TODO Do we want to preserve the connectionId of 2nd Connection of PR?
	}

	@Override
	protected void resetContainer() {
		// App didn't call NC.modify() yet. We can still reuse this NC object
		if (this.endpoint == null) {
			this.audioJoinableStream = null;
			this.endpoint = PR_ENDPOINT_NAME;
		}
	}

	@Override
	protected void unjoined(ConnectionIdentifier thisConnId, ConnectionIdentifier otherConnId) {
		// TODO any further cleaning action?

	}

	public SdpPortManager getSdpPortManager() throws MsControlException {
		return sdpPortManager;
	}

	public void confirm() throws MsControlException {
		throw new MsControlException("Operation not yet supported");
	}

	public MediaConfig getConfig() {
		return this.config;
	}

	public <R> R getResource(Class<R> paramClass) throws MsControlException {
		// TODO Auto-generated method stub
		return null;
	}

	public void triggerRTC(Action paramAction) {
		// TODO Auto-generated method stub

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

	public void release() {
		checkState();

		if (this.connectionIdentifier != null) {
			Runnable tx = new DeleteTx(this);
			Provider.submit(tx);
		}

		try {
			Joinable[] joinableArray = this.getJoinees();
			for (Joinable joinable : joinableArray) {
				this.unjoinInitiate(joinable, this);
			}
		} catch (MsControlException e) {
			logger.error("release of NetworkConnection failed ", e);
		}

		this.state = MediaObjectState.RELEASED;

		this.mediaSession.getNetConnList().remove(this);
	}

	public void setParameters(Parameters paramParameters) {
		this.ncConfig.setParametersImpl(paramParameters);
	}

	public void addListener(AllocationEventListener paramAllocationEventListener) {
		// TODO Auto-generated method stub

	}

	public void removeListener(AllocationEventListener paramAllocationEventListener) {
		// TODO Auto-generated method stub

	}

	public class SdpPortManagerImpl implements SdpPortManager {

		private NetworkConnectionImpl networkConn = null;
		private CodecPolicy codecPolicy = null;

		public SdpPortManagerImpl(NetworkConnectionImpl networkConn) {
			this.networkConn = networkConn;

		}

		public void generateSdpOffer() throws SdpPortManagerException {
			checkState();
			// Async call

			if (connectionIdentifier == null) {
				Runnable tx = new CreateTx(this.networkConn, this, false, true, false);
				Provider.submit(tx);
			} else {
				throw new SdpPortManagerException("Sdp Offer already generated once");
			}

		}

		public CodecPolicy getCodecPolicy() {
			return this.codecPolicy;
		}

		public byte[] getMediaServerSessionDescription() throws SdpPortManagerException {
			return localSessionDescription;
		}

		public byte[] getUserAgentSessionDescription() throws SdpPortManagerException {
			return remoteSessionDescription;
		}

		private String processCodecPolicy(String rawSdp) throws SdpPortManagerException {

			if (this.codecPolicy != null) {
				if (this.codecPolicy.getCodecCapabilities().length != 0
						|| this.codecPolicy.getCodecPreferences().length != 0
						|| this.codecPolicy.getExcludedCodecs().length != 0
						|| this.codecPolicy.getMediaTypeCapabilities().length != 0
						|| this.codecPolicy.getRequiredCodecs().length != 0) {
					try {
						SessionDescription sessiondescription = sdpFactory.createSessionDescription(rawSdp);
						Vector v = sessiondescription.getMediaDescriptions(false);
						Iterator itr = v.iterator();
						while (itr.hasNext()) {
							MediaDescription m = (MediaDescription) itr.next();
							if (this.codecPolicy.getExcludedCodecs().length != 0) {
								MediaParser.excludeCodec(m, this.codecPolicy.getExcludedCodecs());
							}
						}
						return sessiondescription.toString();
					} catch (SdpParseException e) {
						throw new SdpPortManagerException("Exception while processing the CodecPolicy", e);
					} catch (SdpException e) {
						throw new SdpPortManagerException("Exception while processing the CodecPolicy", e);
					}

				} else {
					return rawSdp;
				}
			} else {
				return rawSdp;
			}

		}

		public void processSdpAnswer(byte[] paramArrayOfByte) throws SdpPortManagerException {
			checkState();
			if (paramArrayOfByte != null) {

				remoteSessionDescription = paramArrayOfByte;
				Runnable tx = connectionIdentifier == null ? new CreateTx(this.networkConn, this, false, false, true)
						: new ModifyTx(this.networkConn, this, false, false, true);
				Provider.submit(tx);
			} else {
				throw new SdpPortManagerException("The sdp argument passed cannot be null");
			}
		}

		public void processSdpOffer(byte[] paramArrayOfByte) throws SdpPortManagerException {
			checkState();
			if (paramArrayOfByte != null) {

				remoteSessionDescription = (processCodecPolicy(new String(paramArrayOfByte))).getBytes();

				Runnable tx = connectionIdentifier == null ? new CreateTx(this.networkConn, this, true, false, false)
						: new ModifyTx(this.networkConn, this, true, false, false);
				Provider.submit(tx);
			} else {
				throw new SdpPortManagerException("The sdp argument passed cannot be null");
			}
		}

		public void rejectSdpOffer() throws SdpPortManagerException {
			release();
		}

		public void setCodecPolicy(CodecPolicy paramCodecPolicy) throws SdpPortManagerException {
			this.codecPolicy = paramCodecPolicy;
		}

		public NetworkConnection getContainer() {
			return this.networkConn;
		}

		public boolean stop() {
			return false;
		}

		public void addListener(MediaEventListener<SdpPortManagerEvent> paramMediaEventListener) {
			eventListenerList.add(paramMediaEventListener);
		}

		public MediaSession getMediaSession() {
			return mediaSession;
		}

		public void removeListener(MediaEventListener<SdpPortManagerEvent> paramMediaEventListener) {
			eventListenerList.remove(paramMediaEventListener);
		}

		protected void update(SdpPortManagerEvent anEvent) {
			for (MediaEventListener m : eventListenerList) {
				
				//Since MGCP Stack is Single Threaded, we take care of 
				//Listener taking very long here.
				EventExecutor exe = new EventExecutor(m, anEvent);
				Provider.submit(exe);
				//m.onEvent(anEvent);
			}
		}

		private class CreateTx implements Runnable, JainMgcpExtendedListener {
			private NetworkConnectionImpl networkConnectionImpl = null;
			private SdpPortManager sdpPortManager;
			private int tx = -1;

			private EventType eveType = null;

			public CreateTx(NetworkConnectionImpl networkConnectionImpl, SdpPortManager sdpPortManager,
					boolean processSdpOffer, boolean generateSDPOffer, boolean processSdpAnswer) {
				this.networkConnectionImpl = networkConnectionImpl;
				this.sdpPortManager = sdpPortManager;
				if (processSdpOffer) {
					eveType = SdpPortManagerEvent.ANSWER_GENERATED;
				} else if (generateSDPOffer) {
					eveType = SdpPortManagerEvent.OFFER_GENERATED;
				} else {
					eveType = SdpPortManagerEvent.ANSWER_PROCESSED;
				}

			}

			public void run() {
				try {
					this.tx = mgcpWrapper.getUniqueTransactionHandler();
					mgcpWrapper.addListener(this.tx, this);
					CallIdentifier callId = mediaSession.getCallIdentifier();
					EndpointIdentifier endpointID = new EndpointIdentifier(endpoint, mgcpWrapper.getPeerIp() + ":"
							+ mgcpWrapper.getPeerPort());

					CreateConnection createConnection = new CreateConnection(this, callId, endpointID,
							ConnectionMode.SendRecv);
					if (remoteSessionDescription != null) {
						createConnection.setRemoteConnectionDescriptor(new ConnectionDescriptor(new String(
								remoteSessionDescription)));
					}

					createConnection.setTransactionHandle(tx);
					createConnection.setNotifiedEntity(mgcpWrapper.getDefaultNotifiedEntity());
					mgcpWrapper.sendMgcpEvents(new JainMgcpEvent[] { createConnection });

				} catch (ConflictingParameterException e) {
					logger.error(e);
				}
			}

			public void transactionEnded(int arg0) {
				if (logger.isDebugEnabled()) {
					logger.debug("Successfully completed Tx = " + arg0);
				}
			}

			public void transactionRxTimedOut(JainMgcpCommandEvent arg0) {
			}

			public void transactionTxTimedOut(JainMgcpCommandEvent jainMgcpCommandEvent) {
				logger.error("No response from MGW. Tx timed out for MGCP Tx " + this.tx + " For Command sent "
						+ jainMgcpCommandEvent.toString());
				mgcpWrapper.removeListener(jainMgcpCommandEvent.getTransactionHandle());
				SdpPortManagerEventImpl sdpEvent = new SdpPortManagerEventImpl(this.sdpPortManager, eveType, null,
						false, MediaErr.TIMEOUT, "No response from MGW for modify");
				update(sdpEvent);
			}

			public void processMgcpCommandEvent(JainMgcpCommandEvent arg0) {
			}

			public void processMgcpResponseEvent(JainMgcpResponseEvent jainmgcpresponseevent) {

				switch (jainmgcpresponseevent.getObjectIdentifier()) {
				case Constants.RESP_CREATE_CONNECTION:
					processCreateConnectionResponse((CreateConnectionResponse) jainmgcpresponseevent);
					break;
				default:
					mgcpWrapper.removeListener(jainmgcpresponseevent.getTransactionHandle());
					logger.warn(" This RESPONSE is unexpected " + jainmgcpresponseevent);
					SdpPortManagerEventImpl sdpEvent = new SdpPortManagerEventImpl(this.sdpPortManager, eveType, null,
							false, MediaErr.UNKNOWN_ERROR, "modify failed. Look at logs "
									+ jainmgcpresponseevent.getReturnCode().getComment());
					update(sdpEvent);
					break;

				}
			}

			private void processCreateConnectionResponse(CreateConnectionResponse responseEvent) {
				logger.debug(" processCreateConnectionResponse() ");
				SdpPortManagerEventImpl sdpEvent = null;
				ReturnCode returnCode = responseEvent.getReturnCode();

				switch (returnCode.getValue()) {
				case ReturnCode.TRANSACTION_BEING_EXECUTED:
					// do nothing
					if (logger.isDebugEnabled()) {
						logger.debug("Transaction " + this.tx + "is being executed. Response received = "
								+ responseEvent);
					}
					break;
				case ReturnCode.TRANSACTION_EXECUTED_NORMALLY:
					mgcpWrapper.removeListener(responseEvent.getTransactionHandle());
					connectionIdentifier = responseEvent.getConnectionIdentifier();
					endpoint = responseEvent.getSpecificEndpointIdentifier().getLocalEndpointName();

					if (logger.isDebugEnabled()) {
						logger.debug(" TRANSACTION_EXECUTED_NORMALLY for connectionIdentifier = "
								+ connectionIdentifier + " endpointID = " + endpoint);
					}
					localSessionDescription = (responseEvent.getLocalConnectionDescriptor().toString()).getBytes();

					if (audioJoinableStream == null) {
						audioJoinableStream = new AudioJoinableStream(this.networkConnectionImpl);
					}
					sdpEvent = new SdpPortManagerEventImpl(this.sdpPortManager, eveType, localSessionDescription, true);
					update(sdpEvent);

					break;
				case ReturnCode.ENDPOINT_INSUFFICIENT_RESOURCES:
					mgcpWrapper.removeListener(responseEvent.getTransactionHandle());
					sdpEvent = new SdpPortManagerEventImpl(this.sdpPortManager, eveType, null, false,
							MediaErr.RESOURCE_UNAVAILABLE, returnCode.getComment());
					update(sdpEvent);
					break;
				case ReturnCode.MISSING_REMOTECONNECTIONDESCRIPTOR:
					mgcpWrapper.removeListener(responseEvent.getTransactionHandle());
					sdpEvent = new SdpPortManagerEventImpl(this.sdpPortManager, eveType, null, false,
							SdpPortManagerEvent.SDP_NOT_ACCEPTABLE, returnCode.getComment());
					update(sdpEvent);
					break;
				default:
					logger.error(" SOMETHING IS BROKEN = " + responseEvent);
					mgcpWrapper.removeListener(responseEvent.getTransactionHandle());
					sdpEvent = new SdpPortManagerEventImpl(this.sdpPortManager, eveType, null, false,
							MediaErr.UNKNOWN_ERROR, returnCode.getComment());
					update(sdpEvent);
					break;

				}
			}
		}

		private class ModifyTx implements Runnable, JainMgcpExtendedListener {
			private NetworkConnectionImpl networkConnectionImpl;
			private SdpPortManager sdpPortManager;
			private int tx = -1;
			private EventType eveType = null;

			public ModifyTx(NetworkConnectionImpl networkConnectionImpl, SdpPortManager sdpPortManager,
					boolean processSdpOffer, boolean generateSDPOffer, boolean processSdpAnswer) {
				this.networkConnectionImpl = networkConnectionImpl;
				this.sdpPortManager = sdpPortManager;
				if (processSdpOffer) {
					eveType = SdpPortManagerEvent.ANSWER_GENERATED;
				} else if (generateSDPOffer) {
					eveType = SdpPortManagerEvent.OFFER_GENERATED;
				} else {
					eveType = SdpPortManagerEvent.ANSWER_PROCESSED;
				}
			}

			public void run() {

				try {
					this.tx = mgcpWrapper.getUniqueTransactionHandler();
					mgcpWrapper.addListener(this.tx, this);

					CallIdentifier callId = mediaSession.getCallIdentifier();

					EndpointIdentifier endpointID = new EndpointIdentifier(endpoint, mgcpWrapper.getPeerIp() + ":"
							+ mgcpWrapper.getPeerPort());
					ModifyConnection modifyConnection = new ModifyConnection(this, callId, endpointID,
							connectionIdentifier);

					if (remoteSessionDescription != null) {
						modifyConnection.setRemoteConnectionDescriptor(new ConnectionDescriptor(new String(
								remoteSessionDescription)));
					}

					modifyConnection.setTransactionHandle(tx);
					modifyConnection.setNotifiedEntity(mgcpWrapper.getDefaultNotifiedEntity());
					mgcpWrapper.sendMgcpEvents(new JainMgcpEvent[] { modifyConnection });
				} catch (Exception e) {
					logger.error(e);
				}
			}

			public void transactionEnded(int arg0) {
				// TODO Auto-generated method stub

			}

			public void transactionRxTimedOut(JainMgcpCommandEvent arg0) {
				// TODO Auto-generated method stub

			}

			public void transactionTxTimedOut(JainMgcpCommandEvent jainMgcpCommandEvent) {
				logger.error("No response from MGW. Tx timed out for MGCP Tx " + this.tx + " For Command sent "
						+ jainMgcpCommandEvent.toString());
				mgcpWrapper.removeListener(jainMgcpCommandEvent.getTransactionHandle());
				SdpPortManagerEventImpl sdpEvent = new SdpPortManagerEventImpl(this.sdpPortManager, eveType, null,
						false, MediaErr.TIMEOUT, "No response from MGW for modify");
				update(sdpEvent);

			}

			public void processMgcpCommandEvent(JainMgcpCommandEvent arg0) {
				// TODO Auto-generated method stub

			}

			public void processMgcpResponseEvent(JainMgcpResponseEvent jainmgcpresponseevent) {

				// TODO : Depending on Response we get fire corresponding
				// JSR-309 events here

				switch (jainmgcpresponseevent.getObjectIdentifier()) {

				case Constants.RESP_MODIFY_CONNECTION:
					processMofiyConnectionResponse((ModifyConnectionResponse) jainmgcpresponseevent);
					break;
				default:
					mgcpWrapper.removeListener(jainmgcpresponseevent.getTransactionHandle());
					logger.warn(" This RESPONSE is unexpected " + jainmgcpresponseevent);
					SdpPortManagerEventImpl sdpEvent = new SdpPortManagerEventImpl(this.sdpPortManager, eveType, null,
							false, MediaErr.UNKNOWN_ERROR, "modify failed. Look at logs "
									+ jainmgcpresponseevent.getReturnCode().getComment());
					update(sdpEvent);
					break;
				}
			}

			private void processMofiyConnectionResponse(ModifyConnectionResponse responseEvent) {
				logger.debug(" processMofiyConnectionResponse() ");
				SdpPortManagerEventImpl sdpEvent = null;
				ReturnCode returnCode = responseEvent.getReturnCode();

				switch (returnCode.getValue()) {
				case ReturnCode.TRANSACTION_BEING_EXECUTED:
					// do nothing
					if (logger.isDebugEnabled()) {
						logger.debug("Transaction " + this.tx + "is being executed. Response received = "
								+ responseEvent);
					}
					break;
				case ReturnCode.TRANSACTION_EXECUTED_NORMALLY:
					mgcpWrapper.removeListener(responseEvent.getTransactionHandle());
					if (logger.isDebugEnabled()) {
						logger.debug(" MDCX TRANSACTION_EXECUTED_NORMALLY for connectionIdentifier = "
								+ connectionIdentifier + "endpointID = " + endpoint);
					}

					if (responseEvent.getLocalConnectionDescriptor() != null) {
						localSessionDescription = (responseEvent.getLocalConnectionDescriptor().toString()).getBytes();
					}
					sdpEvent = new SdpPortManagerEventImpl(this.sdpPortManager, eveType, localSessionDescription, true);
					update(sdpEvent);

					break;
				case ReturnCode.ENDPOINT_INSUFFICIENT_RESOURCES:
					mgcpWrapper.removeListener(responseEvent.getTransactionHandle());
					sdpEvent = new SdpPortManagerEventImpl(this.sdpPortManager, eveType, null, false,
							MediaErr.RESOURCE_UNAVAILABLE, returnCode.getComment());
					update(sdpEvent);

					break;
				default:
					mgcpWrapper.removeListener(responseEvent.getTransactionHandle());
					logger.error(" SOMETHING IS BROKEN = " + responseEvent);
					sdpEvent = new SdpPortManagerEventImpl(this.sdpPortManager, eveType, null, false,
							MediaErr.UNKNOWN_ERROR, returnCode.getComment());
					update(sdpEvent);

					break;

				}

			}
		}

	}

	protected class DeleteTx implements Runnable, JainMgcpExtendedListener {

		private NetworkConnectionImpl networkConnectionImpl;
		private int tx = -1;

		public DeleteTx(NetworkConnectionImpl networkConnectionImpl) {
			this.networkConnectionImpl = networkConnectionImpl;
		}

		public void run() {
			try {
				this.tx = mgcpWrapper.getUniqueTransactionHandler();
				// TODO : Do we need to fire event for DLCX?
				mgcpWrapper.addListener(this.tx, this);

				CallIdentifier callId = mediaSession.getCallIdentifier();
				EndpointIdentifier endpointID = new EndpointIdentifier(endpoint, mgcpWrapper.getPeerIp() + ":"
						+ mgcpWrapper.getPeerPort());
				DeleteConnection deleteConnection = new DeleteConnection(this, callId, endpointID, connectionIdentifier);

				deleteConnection.setTransactionHandle(tx);
				mgcpWrapper.sendMgcpEvents(new JainMgcpEvent[] { deleteConnection });
			} catch (Exception e) {
				logger.error(e);
			}
		}

		public void transactionEnded(int arg0) {
			// TODO Auto-generated method stub

		}

		public void transactionRxTimedOut(JainMgcpCommandEvent arg0) {
			// TODO Auto-generated method stub

		}

		public void transactionTxTimedOut(JainMgcpCommandEvent jainMgcpCommandEvent) {

			logger.error("No response from MGW. Tx timed out for MGCP Tx " + this.tx + " For Command sent "
					+ jainMgcpCommandEvent.toString());
			mgcpWrapper.removeListener(jainMgcpCommandEvent.getTransactionHandle());

		}

		public void processMgcpCommandEvent(JainMgcpCommandEvent arg0) {
			// TODO Auto-generated method stub

		}

		public void processMgcpResponseEvent(JainMgcpResponseEvent response) {
			if (response.getTransactionHandle() != this.tx) {
				return;
			}

			switch (response.getObjectIdentifier()) {

			case Constants.RESP_DELETE_CONNECTION:
				processDeleteConnectionResponse((DeleteConnectionResponse) response);
				break;
			default:
				mgcpWrapper.removeListener(response.getTransactionHandle());
				logger.warn(" DLCX of Netwrok connction failed RESPONSE is unexpected " + response);
				break;

			}
		}

		private void processDeleteConnectionResponse(DeleteConnectionResponse responseEvent) {
			ReturnCode returnCode = responseEvent.getReturnCode();
			JoinEvent joinEvent = null;
			switch (returnCode.getValue()) {
			case ReturnCode.TRANSACTION_BEING_EXECUTED:
				// do nothing
				if (logger.isDebugEnabled()) {
					logger.debug("Transaction " + responseEvent.getTransactionHandle()
							+ "is being executed. Response received = " + responseEvent);
				}
				break;
			case ReturnCode.TRANSACTION_EXECUTED_NORMALLY:
				mgcpWrapper.removeListener(responseEvent.getTransactionHandle());
				if (logger.isDebugEnabled()) {
					logger.debug("DLCX executed successfully for Tx = " + responseEvent.getTransactionHandle());
				}
				break;
			default:
				mgcpWrapper.removeListener(responseEvent.getTransactionHandle());
				logger.error(" SOMETHING IS BROKEN = " + responseEvent);
				break;
			}

		}

	}

	public void triggerAction(Action arg0) {
		// TODO Auto-generated method stub

	}

}
