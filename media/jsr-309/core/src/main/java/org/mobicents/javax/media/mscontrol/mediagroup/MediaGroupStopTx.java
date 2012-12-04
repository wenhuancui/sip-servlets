package org.mobicents.javax.media.mscontrol.mediagroup;

import jain.protocol.ip.mgcp.JainMgcpCommandEvent;
import jain.protocol.ip.mgcp.JainMgcpEvent;
import jain.protocol.ip.mgcp.JainMgcpResponseEvent;
import jain.protocol.ip.mgcp.message.Constants;
import jain.protocol.ip.mgcp.message.NotificationRequest;
import jain.protocol.ip.mgcp.message.NotificationRequestResponse;
import jain.protocol.ip.mgcp.message.parms.EndpointIdentifier;
import jain.protocol.ip.mgcp.message.parms.ReturnCode;

import javax.media.mscontrol.MediaErr;
import javax.media.mscontrol.mediagroup.PlayerEvent;
import javax.media.mscontrol.mediagroup.signals.SignalDetectorEvent;

import org.apache.log4j.Logger;
import org.mobicents.javax.media.mscontrol.mediagroup.signals.SignalDetectorEventImpl;
import org.mobicents.jsr309.mgcp.MgcpWrapper;
import org.mobicents.protocols.mgcp.stack.JainMgcpExtendedListener;

public class MediaGroupStopTx implements Runnable, JainMgcpExtendedListener {
	private static final Logger logger = Logger.getLogger(MediaGroupStopTx.class);
	private int tx = -1;
	MgcpWrapper mgcpWrapper;
	MediaGroupImpl mediaGroup;

	MediaGroupStopTx(MgcpWrapper mgcpWrapper, MediaGroupImpl mediaGroup) {
		this.mgcpWrapper = mgcpWrapper;
		this.mediaGroup = mediaGroup;
	}

	// TODO : This will stop all the active Signals and Event Detection! We
	// just want Player to Stop
	public void run() {
		try {
			this.tx = mgcpWrapper.getUniqueTransactionHandler();
			mgcpWrapper.addListener(this.tx, this);
			mgcpWrapper.addListener(mediaGroup.reqId, this);

			EndpointIdentifier endpointID = new EndpointIdentifier(mediaGroup.getEndpoint(), mgcpWrapper
					.getPeerIp()
					+ ":" + mgcpWrapper.getPeerPort());
			NotificationRequest notificationRequest = new NotificationRequest(this, endpointID, mediaGroup.reqId);

			notificationRequest.setTransactionHandle(this.tx);
			notificationRequest.setNotifiedEntity(mgcpWrapper.getDefaultNotifiedEntity());
			mgcpWrapper.sendMgcpEvents(new JainMgcpEvent[] { notificationRequest });

		} catch (Exception e) {
			logger.error("Error", e);

			mgcpWrapper.removeListener(this.tx);
			mgcpWrapper.removeListener(mediaGroup.reqId);

			if(mediaGroup.player != null) {
				mediaGroup.player.updateState();

				PlayerEventImpl event = new PlayerEventImpl(mediaGroup.player, PlayerEvent.PLAY_COMPLETED, false,
						MediaErr.UNKNOWN_ERROR, "Error " + e.getMessage());
				mediaGroup.player.update(event);
				mediaGroup.player.executeNextTx();
			}
			
			if(mediaGroup.detector != null) {
				SignalDetectorEventImpl event = new SignalDetectorEventImpl(mediaGroup.detector, SignalDetectorEventImpl.SIGNAL_DETECTED, false, MediaErr.UNKNOWN_ERROR, "Error " + e.getMessage());
				mediaGroup.detector.update(event);
			}
		}
	}

	public void transactionEnded(int arg0) {
		// TODO Auto-generated method stub

	}

	public void transactionRxTimedOut(JainMgcpCommandEvent cmdEvent) {

	}

	public void transactionTxTimedOut(JainMgcpCommandEvent cmdEvent) {
		logger.error("No response from MGW. Tx timed out for RQNT Tx " + this.tx + " For Command sent "
				+ cmdEvent.toString());
		mgcpWrapper.removeListener(cmdEvent.getTransactionHandle());
		mgcpWrapper.removeListener(mediaGroup.reqId);

		if(mediaGroup.player != null) {

			PlayerEventImpl event = new PlayerEventImpl(mediaGroup.player, PlayerEvent.PLAY_COMPLETED, false,
					MediaErr.UNKNOWN_ERROR, "Error ");
			event.setQualifier(PlayerEvent.STOPPED);
			mediaGroup.player.update(event);
			mediaGroup.player.executeNextTx();
		}
		
		if(mediaGroup.detector != null) {
			SignalDetectorEventImpl event = new SignalDetectorEventImpl(mediaGroup.detector, SignalDetectorEventImpl.SIGNAL_DETECTED, false, MediaErr.UNKNOWN_ERROR, "Error ");
			mediaGroup.detector.update(event);
		}
	}

	public void processMgcpCommandEvent(JainMgcpCommandEvent arg0) {
		if(logger.isDebugEnabled()) {
			logger.debug(arg0);
		}

	}

	public void processMgcpResponseEvent(JainMgcpResponseEvent respEvent) {
		switch (respEvent.getObjectIdentifier()) {
		case Constants.RESP_NOTIFICATION_REQUEST:
			processReqNotificationResponse((NotificationRequestResponse) respEvent);
			break;
		default:
			mgcpWrapper.removeListener(respEvent.getTransactionHandle());
			mgcpWrapper.removeListener(mediaGroup.reqId);
			logger.warn(" This RESPONSE is unexpected " + respEvent);

			if(mediaGroup.player != null) {

				PlayerEventImpl event = new PlayerEventImpl(mediaGroup.player, PlayerEvent.PLAY_COMPLETED, false,
						MediaErr.UNKNOWN_ERROR, "Error ");
				event.setQualifier(PlayerEvent.STOPPED);
				mediaGroup.player.update(event);
				mediaGroup.player.executeNextTx();
			}
			
			if(mediaGroup.detector != null) {
				SignalDetectorEventImpl event = new SignalDetectorEventImpl(mediaGroup.detector, SignalDetectorEventImpl.SIGNAL_DETECTED, false, MediaErr.UNKNOWN_ERROR, "Error ");
				mediaGroup.detector.update(event);
			}
			break;

		}
	}

	private void processReqNotificationResponse(NotificationRequestResponse responseEvent) {
	
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
			mgcpWrapper.removeListener(mediaGroup.reqId);

			if(mediaGroup.player != null) {

				PlayerEventImpl event = new PlayerEventImpl(mediaGroup.player, PlayerEvent.PLAY_COMPLETED, true, PlayerEvent.STOPPED, null);
				event.setQualifier(PlayerEvent.STOPPED);
				mediaGroup.player.update(event);
				mediaGroup.player.executeNextTx();
			}
			
			if(mediaGroup.detector != null) {
				SignalDetectorEventImpl event = new SignalDetectorEventImpl(mediaGroup.detector, SignalDetectorEvent.SIGNAL_DETECTED, true);
				event.setQualifier(SignalDetectorEvent.STOPPED);
				mediaGroup.detector.update(event);
			}

			break;
		default:
			logger.error(" SOMETHING IS BROKEN = " + responseEvent);
			mgcpWrapper.removeListener(responseEvent.getTransactionHandle());
			mgcpWrapper.removeListener(mediaGroup.reqId);

			if(mediaGroup.player != null) {

				PlayerEventImpl event = new PlayerEventImpl(mediaGroup.player, PlayerEvent.PLAY_COMPLETED, false,
						MediaErr.UNKNOWN_ERROR, "Error ");
				event.setQualifier(PlayerEvent.STOPPED);
				mediaGroup.player.update(event);
				mediaGroup.player.executeNextTx();
			}
			
			if(mediaGroup.detector != null) {
				SignalDetectorEventImpl event = new SignalDetectorEventImpl(mediaGroup.detector, SignalDetectorEventImpl.SIGNAL_DETECTED, false, MediaErr.UNKNOWN_ERROR, "Error ");
				mediaGroup.detector.update(event);
			}
			break;

		}
	}

}