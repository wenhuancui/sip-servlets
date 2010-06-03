package org.mobicents.javax.media.mscontrol.mediagroup;

import jain.protocol.ip.mgcp.JainMgcpCommandEvent;
import jain.protocol.ip.mgcp.JainMgcpEvent;
import jain.protocol.ip.mgcp.JainMgcpResponseEvent;
import jain.protocol.ip.mgcp.message.Constants;
import jain.protocol.ip.mgcp.message.NotificationRequest;
import jain.protocol.ip.mgcp.message.NotificationRequestResponse;
import jain.protocol.ip.mgcp.message.Notify;
import jain.protocol.ip.mgcp.message.NotifyResponse;
import jain.protocol.ip.mgcp.message.parms.ConnectionIdentifier;
import jain.protocol.ip.mgcp.message.parms.EndpointIdentifier;
import jain.protocol.ip.mgcp.message.parms.EventName;
import jain.protocol.ip.mgcp.message.parms.RequestIdentifier;
import jain.protocol.ip.mgcp.message.parms.RequestedAction;
import jain.protocol.ip.mgcp.message.parms.RequestedEvent;
import jain.protocol.ip.mgcp.message.parms.ReturnCode;
import jain.protocol.ip.mgcp.pkg.MgcpEvent;
import jain.protocol.ip.mgcp.pkg.PackageName;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.media.mscontrol.EventType;
import javax.media.mscontrol.MediaErr;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.Parameter;
import javax.media.mscontrol.Parameters;
import javax.media.mscontrol.Qualifier;
import javax.media.mscontrol.mediagroup.MediaGroup;
import javax.media.mscontrol.mediagroup.Player;
import javax.media.mscontrol.mediagroup.PlayerEvent;
import javax.media.mscontrol.mediagroup.signals.SignalDetectorEvent;
import javax.media.mscontrol.resource.enums.EventTypeEnum;
import javax.media.mscontrol.resource.enums.ParameterEnum;

import org.apache.log4j.Logger;
import org.mobicents.javax.media.mscontrol.DefaultEventGeneratorFactory;
import org.mobicents.javax.media.mscontrol.mediagroup.signals.SignalDetectorEventDetectorFactory;
import org.mobicents.javax.media.mscontrol.mediagroup.signals.SignalDetectorEventImpl;
import org.mobicents.javax.media.mscontrol.mediagroup.signals.SignalDetectorImpl;
import org.mobicents.jsr309.mgcp.MgcpWrapper;
import org.mobicents.protocols.mgcp.stack.JainMgcpExtendedListener;

public class MediaGroupRequestTx implements Runnable, JainMgcpExtendedListener {
	private static Logger logger = Logger.getLogger(MediaGroupRequestTx.class);
	private int tx = -1;

	// private URI file = null;
	private int startOffset = 0;
	private int maxDuration = 0;
	private Qualifier qualifier = PlayerEvent.END_OF_PLAY_LIST;
	private boolean update = true;
	private final LinkedList<URI> uriList;
	private MediaGroupImpl mediaGroup;
	private MgcpWrapper mgcpWrapper;
	private boolean dtmf;

	public MediaGroupRequestTx(MgcpWrapper mgcpWrapper, MediaGroupImpl mg, LinkedList<URI> uriList, int startOffset, int maxDuration, boolean update, boolean dtmf) {
		this.mediaGroup = mg;
		this.startOffset = startOffset;
		this.maxDuration = maxDuration;
		this.update = update;
		this.uriList = uriList;
		this.mgcpWrapper = mgcpWrapper;
		this.dtmf = dtmf;
	}

	private void playNextFile(PlayerEvent anEvent) {
		URI uri = this.uriList.peek();
		if (uri != null) {
			this.run();
		} else {
			if (this.update) {
				((MediaGroupImpl)mediaGroup).getPlayerExt().updateState();
				((MediaGroupImpl)mediaGroup).getPlayerExt().update(anEvent);

			}
			((MediaGroupImpl)mediaGroup).getPlayerExt().executeNextTx();
		}
	}

	public void run() {
		
		this.tx = mgcpWrapper.getUniqueTransactionHandler();
		try {
			if(mediaGroup.detector != null) {
				mediaGroup.detector.alreadyListening = dtmf;
			}
			
			mgcpWrapper.addListener(this.tx, this);
			
			List<EventName> eveNames = new ArrayList<EventName>();
			List<EventName> eveNamesDetector = new ArrayList<EventName>();

			EndpointIdentifier endpointID = new EndpointIdentifier(mediaGroup.getEndpoint(), mgcpWrapper
					.getPeerIp()
					+ ":" + mgcpWrapper.getPeerPort());

			mediaGroup.reqId = mgcpWrapper.getUniqueRequestIdentifier();
			mgcpWrapper.addListener(mediaGroup.reqId, this);

			NotificationRequest notificationRequest = new NotificationRequest(this, endpointID, mediaGroup.reqId);
			ConnectionIdentifier connId = mediaGroup.thisConnId;
			
			RequestedAction[] actions = new RequestedAction[] { RequestedAction.NotifyImmediately };
			
			boolean isTTS = false;
			EventName[] signalRequests = null;
			PackageName auPackageName=PackageName.factory("AU");
			
			// ANN/PLAYBACK
			if(uriList != null) {
				URI uri = uriList.poll();
				for (DefaultEventGeneratorFactory genfact : mediaGroup.medGrpConfig.getPlayerConfig().getEventGenFctList()) {
					if(logger.isDebugEnabled()) {
						logger.debug("We are doing media playback " + uri + " for genfact = " + genfact);
					}
					if(uri.getScheme().equalsIgnoreCase("data")) {
						// this will give something like: "AU/pa(ts("+textToPlay+"))", AU and pa are configured in mgcp controller.
						isTTS = true;
						MgcpEvent e = MgcpEvent.factory("ann");

						String text = uri.getSchemeSpecificPart().replace('+', ' ');
						eveNames.add(new EventName(auPackageName, e.withParm(text), connId));
					} else {
						if (genfact.getEventName().compareTo(MgcpEvent.ann.getName()) == 0) {
							String parameter = uri.toString();

							if (this.startOffset != 0) {
								parameter = parameter + " SO=" + startOffset;
							}

							if (this.maxDuration != Player.FOREVER) {
								parameter = parameter + " MD=" + maxDuration;
							}

							eveNames.add(genfact.generateMgcpEvent(parameter, connId));
						}
					}
				}
			}
			
			// DTMF detector
			if(dtmf && mediaGroup.getSignalDetectorExt() != null) {
				for (SignalDetectorEventDetectorFactory detfact : mediaGroup.medGrpConfig.getSigDetConfig().getEventDetFctList()) {
					eveNamesDetector.add(detfact.generateMgcpEvent(null, connId));
				}
			}

			signalRequests = new EventName[eveNames.size()];
			eveNames.toArray(signalRequests);

			eveNames.clear();

			if(signalRequests.length>0) {
				// .. because empty signal requests make MMS fail
				notificationRequest.setSignalRequests(signalRequests);
			}

			// in case it is ANN/PLAYBACK again we want the ANN events
			if(uriList!= null && !isTTS) {
				for (PlayerEventDetectorFactory detfact : mediaGroup.medGrpConfig.getPlayerConfig().getEventDetFctList()) {
					eveNames.add(detfact.generateMgcpEvent(null, connId));
				}
			}

			eveNames.addAll(eveNamesDetector);
			
			// in case it is ANN/PLAYBACK again we want the TTS events, not the ANN
			
			if(isTTS) {
				eveNames.add(new EventName(auPackageName, MgcpEvent.oc, connId));
				eveNames.add(new EventName(auPackageName, MgcpEvent.of, connId));
			}

			RequestedEvent[] requestedEvents = new RequestedEvent[eveNames.size()];
			for (int i = 0; i < requestedEvents.length; i++) {
				requestedEvents[i] = new RequestedEvent(eveNames.get(i), actions);
			}

			notificationRequest.setRequestedEvents(requestedEvents);
			notificationRequest.setTransactionHandle(this.tx);
			notificationRequest.setNotifiedEntity(mgcpWrapper.getDefaultNotifiedEntity());

			mediaGroup.getPlayerExt().startTime = System.currentTimeMillis();
			mgcpWrapper.sendMgcpEvents(new JainMgcpEvent[] { notificationRequest });

		} catch (Exception e) {
			logger.error("Uncought error", e);

			mgcpWrapper.removeListener(this.tx);
			mgcpWrapper.removeListener(mediaGroup.reqId);

			PlayerEventImpl event = new PlayerEventImpl(mediaGroup.player, PlayerEvent.PLAY_COMPLETED, false,
					MediaErr.UNKNOWN_ERROR, "Error while sending RQNt " + e.getMessage());
			
			//TODO: DTMF error report
			playNextFile(event);
		}

	}

	public void transactionEnded(int arg0) {
		if (logger.isDebugEnabled()) {
			logger.debug("Successfully completed Tx = " + arg0);
		}
	}

	public void transactionRxTimedOut(JainMgcpCommandEvent arg0) {
		if (logger.isDebugEnabled()) {
			logger.debug("Couldn't send the Tx = " + arg0);
		}
	}

	public void transactionTxTimedOut(JainMgcpCommandEvent cmdEvent) {
		try {
			logger.error("No response from MGW. Tx timed out for RQNT Tx " + this.tx + " For Command sent "
					+ cmdEvent.toString());
			mgcpWrapper.removeListener(cmdEvent.getTransactionHandle());
			mgcpWrapper.removeListener(mediaGroup.reqId);
			if(dtmf && mediaGroup.detector != null) {
				SignalDetectorEventImpl event2 = new SignalDetectorEventImpl(mediaGroup.detector,
						SignalDetectorEvent.SIGNAL_DETECTED, false, MediaErr.UNKNOWN_ERROR,
						"RQNT Failed.  Look at logs " + cmdEvent.toString());
				mediaGroup.detector.update(event2);
			}

			if(uriList != null) {
				PlayerEventImpl event = new PlayerEventImpl(mediaGroup.player, PlayerEvent.PLAY_COMPLETED, false,
						MediaErr.UNKNOWN_ERROR, "No response from MGW for RQNT");
				playNextFile(event);
			}
		} catch (Exception e) {
			logger.error("Uncought error", e);
		}
	}

	public void processMgcpCommandEvent(JainMgcpCommandEvent command) {
		logger.error(" The NTFY received " + command.toString());

		try {
			PlayerEventImpl event = null;
			Notify notify = (Notify) command;
			//mgcpWrapper.removeListener(notify.getRequestIdentifier());

			NotifyResponse response = new NotifyResponse(notify.getSource(), ReturnCode.Transaction_Executed_Normally);
			response.setTransactionHandle(notify.getTransactionHandle());

			mgcpWrapper.sendMgcpEvents(new JainMgcpEvent[] { response });

			switch (command.getObjectIdentifier()) {
			case Constants.CMD_NOTIFY:

				EventName[] observedEvents = notify.getObservedEvents();

				// TODO : Do we care for Multiple Events? This will only fire
				// last event
				for (EventName observedEvent : observedEvents) {
					if(mediaGroup.medGrpConfig.getPlayerConfig() != null) {
						for (PlayerEventDetectorFactory detfact : mediaGroup.medGrpConfig.getPlayerConfig().getEventDetFctListALL()) {
							if ((detfact.getPkgName().compareTo(observedEvent.getPackageName().toString()) == 0)
									&& (detfact.getEventName().compareTo(observedEvent.getEventIdentifier().getName()) == 0)) {
								logger.info(" The NTFY is player " + command.toString());
								event = (PlayerEventImpl) detfact.generateMediaEvent();
								event.setPlayer(mediaGroup.player);
								event.setSuccessful(true);
								long playTime = System.currentTimeMillis() - mediaGroup.getPlayerExt().startTime;
								if (this.maxDuration != Player.FOREVER && ((this.maxDuration - playTime) < PlayerImpl.PLAY_TOLERANCE)) {
									this.qualifier = PlayerEvent.DURATION_EXCEEDED;
								}

								event.setQualifier(qualifier);

								logger.debug(" event is successful =  " + event.isSuccessful());
								playNextFile(event);
								break;
							}
						}
					}

					if( mediaGroup.detector != null ) {
						for (SignalDetectorEventDetectorFactory detfact : mediaGroup.medGrpConfig.getSigDetConfig().getEventDetFctList()) {
							if ((detfact.getPkgName().compareTo(observedEvent.getPackageName().toString()) == 0)
									&& (detfact.getEventName().compareTo(observedEvent.getEventIdentifier().getName()) == 0)) {
								logger.info(" The NTFY is DTMF " + command.toString() + " receiving:" + mediaGroup.detector.receiving 
										+ " singlasLeft:" + mediaGroup.detector.numSignalsLeft);
								String digitDetected = SignalDetectorImpl.mgcpEventToDTMF(observedEvent.getEventIdentifier());
								logger.error("Received " + command);

								/*
								 * When a signal is passed to a pattern matcher, this procedure is followed:
							if BUFFERING is true, then the signal is added to the signal buffer.
							DETECTION_OF_ONE_SIGNAL triggers any applicable RTCs.
							if ENABLED_EVENTS contains SIGNAL_DETECTED, then MediaEventListener.onEvent(event) is invoked
							if the signal buffer overflows then overflow processing is done.
							if pattern matchers are enabled, then pattern matching is done.
							receiveSignals may be terminated by a pattern match.
							the NumSignals signal counter is incremented and receiveSignals may be terminated with NUM_SIGNALS_DETECTED.
								 */
								SignalDetectorImpl detector = mediaGroup.getSignalDetectorExt();
								Parameters params = mediaGroup.getParameters(null);
								if(Boolean.TRUE.equals(params.get(SignalDetectorImpl.BUFFERING))) {
									mediaGroup.getSignalDetectorExt().buffer.add(digitDetected);
								}
								SignalDetectorEventImpl detectorEvent = null;
								EventType[] eventTypes = (EventType[])params.get(SignalDetectorImpl.ENABLED_EVENTS);
								if(eventTypes != null) for(EventType et : eventTypes) {
									if(et.equals(EventTypeEnum.SIGNAL_DETECTED)) {
										detectorEvent = (SignalDetectorEventImpl) detfact.generateMediaEvent();
										detectorEvent.setDetector(detector);
										detectorEvent.setSuccessful(true);
										detectorEvent.setSignal(digitDetected);
										detectorEvent.setQualifier(SignalDetectorEvent.NO_QUALIFIER);
										detector.update(detectorEvent);
									}
								}
								if(detector.receiving) {
									logger.info("We are receiving DTMF. We are waiting for numSingals=" + 
											detector.numSignalsLeft + 
											" so far in cache are: " + 
											detector.numSignalsCache);
									
									detector.numSignalsLeft--;
									detector.numSignalsCache += digitDetected;
									int patternIndex = -1;
									if(detector.patterns != null) {
										for(int q=0; q<detector.patterns.length; q++) {
											Parameter p = detector.patterns[q];
											String patt = (String) detector.optionlArgs.get(p);
											if(logger.isDebugEnabled()) {
												logger.debug("COMAPRING ----------------------------::::;" + patt 
														+ "  " + detector.numSignalsCache + " digit " + digitDetected);
											}
											if(detector.numSignalsCache.endsWith(patt)) {
												patternIndex = detector.paramEnumToIndex((ParameterEnum)p);
												detector.numSignalsCache = patt;
												if(logger.isDebugEnabled()) {
													logger.debug("Matched " + p + "  " + patternIndex);
												}
												break;
											}
										}
									}
									if(patternIndex == -1) {
										if(logger.isDebugEnabled()) {
											logger.debug("Not matched" + detector.numSignalsCache);
										}
									}
									if(detector.numSignalsLeft == 0 || patternIndex>=0) {
										
										detector.receiving = false;
										detectorEvent = new SignalDetectorEventImpl(detector, EventTypeEnum.RECEIVE_SIGNALS_COMPLETED, true);
										if(patternIndex<0) {
											detectorEvent.setQualifier(SignalDetectorEvent.NUM_SIGNALS_DETECTED);
										} else {
											detectorEvent.setQualifier(SignalDetectorEvent.PATTERN_MATCHING[patternIndex]);
											detectorEvent.setPatterIndex(patternIndex);
										}
										detectorEvent.setSuccessful(true);
										detectorEvent.setSignal(detector.numSignalsCache);
										logger.info("delivering eevent" + detectorEvent);
										detector.update(detectorEvent);
										detector.numSignalsCache = new String();
									}
									
									if(detector.numSignalsLeft<0) {
										if(logger.isInfoEnabled()) {
											logger.info("NEGATIVE NUMSIGNALS, a skipped digit? cache=" + detector.numSignalsCache);
										}
									}
								}
							}
						}
					}
				}

				break;

			default:
				logger.error("Expected NTFY cmd. Received " + command);

				event = new PlayerEventImpl(mediaGroup.player, PlayerEvent.PLAY_COMPLETED, false, MediaErr.UNKNOWN_ERROR,
						"Player failed on Server");
				playNextFile(event);
				break;
			}
		} catch (IllegalArgumentException e) {
			logger.error("Uncought error", e);
		}

	}

	public void processMgcpResponseEvent(JainMgcpResponseEvent respEvent) {
		try {
			switch (respEvent.getObjectIdentifier()) {
			case Constants.RESP_NOTIFICATION_REQUEST:
				processReqNotificationResponse((NotificationRequestResponse) respEvent);
				break;
			default:
				mgcpWrapper.removeListener(respEvent.getTransactionHandle());
				mgcpWrapper.removeListener(mediaGroup.reqId);

				logger.warn(" This RESPONSE is unexpected " + respEvent);
				if(dtmf && mediaGroup.detector != null) {
					SignalDetectorEventImpl event2 = new SignalDetectorEventImpl(mediaGroup.detector,
							SignalDetectorEvent.SIGNAL_DETECTED, false, MediaErr.UNKNOWN_ERROR,
							"RQNT Failed.  Look at logs " + respEvent.getReturnCode().getComment());
					mediaGroup.detector.update(event2);
				}

				if(this.uriList != null) {
					PlayerEventImpl event = new PlayerEventImpl(mediaGroup.player, PlayerEvent.PLAY_COMPLETED, false,
							MediaErr.UNKNOWN_ERROR, "RQNT Failed.  Look at logs " + respEvent.getReturnCode().getComment());
					playNextFile(event);
				}
				break;

			}
		} catch (Exception e) {
			logger.error("Uncought error", e);
		}
	}

	private void processReqNotificationResponse(NotificationRequestResponse responseEvent) {
		PlayerEvent event = null;
		ReturnCode returnCode = responseEvent.getReturnCode();

		switch (returnCode.getValue()) {
		case ReturnCode.TRANSACTION_BEING_EXECUTED:
			// do nothing
			if (logger.isDebugEnabled()) {
				logger.debug("Transaction " + this.tx + "is being executed. Response received = " + responseEvent);
			}
			break;
		case ReturnCode.TRANSACTION_EXECUTED_NORMALLY:
			mgcpWrapper.removeListener(responseEvent.getTransactionHandle());

			break;
		case ReturnCode.ENDPOINT_INSUFFICIENT_RESOURCES:
			mgcpWrapper.removeListener(responseEvent.getTransactionHandle());
			mgcpWrapper.removeListener(mediaGroup.reqId);
			logger.error(" ENDPOINT_INSUFFICIENT_RESOURCES for req id = " + mediaGroup.reqId);
			
			if(dtmf && mediaGroup.detector != null) {
				SignalDetectorEventImpl event2 = new SignalDetectorEventImpl(mediaGroup.detector,
						SignalDetectorEvent.SIGNAL_DETECTED, false, MediaErr.RESOURCE_UNAVAILABLE,
						"RQNT Failed.  Look at logs " + responseEvent.getReturnCode().getComment());
				mediaGroup.detector.update(event2);
			}
			if(uriList != null) {
				event = new PlayerEventImpl(mediaGroup.player, PlayerEvent.PLAY_COMPLETED, false,
						MediaErr.RESOURCE_UNAVAILABLE, "RQNT Failed.  Look at logs "
						+ responseEvent.getReturnCode().getComment());
				playNextFile(event);
			}
			break;
		default:
			logger.error(" SOMETHING IS BROKEN = " + responseEvent);
			mgcpWrapper.removeListener(responseEvent.getTransactionHandle());
			mgcpWrapper.removeListener(mediaGroup.reqId);
			if(dtmf && mediaGroup.detector != null) {
				SignalDetectorEventImpl event2 = new SignalDetectorEventImpl(mediaGroup.detector,
						SignalDetectorEvent.SIGNAL_DETECTED, false, MediaErr.UNKNOWN_ERROR,
						"RQNT Failed.  Look at logs " + responseEvent.getReturnCode().getComment());
				mediaGroup.detector.update(event2);
			}
			if(uriList != null) {
				event = new PlayerEventImpl(mediaGroup.player, PlayerEvent.PLAY_COMPLETED, false, MediaErr.UNKNOWN_ERROR,
						"RQNT Failed.  Look at logs " + responseEvent.getReturnCode().getComment());
				playNextFile(event);
			}

			break;

		}
	}
}