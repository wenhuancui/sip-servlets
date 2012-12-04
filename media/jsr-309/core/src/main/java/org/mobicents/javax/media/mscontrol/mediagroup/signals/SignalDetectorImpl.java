package org.mobicents.javax.media.mscontrol.mediagroup.signals;

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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.media.mscontrol.EventType;
import javax.media.mscontrol.MediaErr;
import javax.media.mscontrol.MediaEventListener;
import javax.media.mscontrol.MediaSession;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.Parameter;
import javax.media.mscontrol.Parameters;
import javax.media.mscontrol.mediagroup.MediaGroup;
import javax.media.mscontrol.mediagroup.signals.SignalDetector;
import javax.media.mscontrol.mediagroup.signals.SignalDetectorEvent;
import javax.media.mscontrol.resource.RTC;
import javax.media.mscontrol.resource.enums.EventTypeEnum;
import javax.media.mscontrol.resource.enums.ParameterEnum;

import org.apache.log4j.Logger;
import org.mobicents.javax.media.mscontrol.MediaConfigImpl;
import org.mobicents.javax.media.mscontrol.MediaObjectState;
import org.mobicents.javax.media.mscontrol.MediaSessionImpl;
import org.mobicents.javax.media.mscontrol.SupportedFeaturesImpl;
import org.mobicents.javax.media.mscontrol.mediagroup.MediaGroupConfig;
import org.mobicents.javax.media.mscontrol.mediagroup.MediaGroupImpl;
import org.mobicents.javax.media.mscontrol.mediagroup.MediaGroupRequestTx;
import org.mobicents.jsr309.mgcp.MgcpWrapper;
import org.mobicents.jsr309.mgcp.Provider;
import org.mobicents.protocols.mgcp.stack.JainMgcpExtendedListener;


/**
 * 
 * @author amit bhayani
 * @author vralev
 * 
 */
public class SignalDetectorImpl implements SignalDetector {

	private static Logger logger = Logger.getLogger(SignalDetectorImpl.class);
	protected CopyOnWriteArrayList<MediaEventListener<SignalDetectorEvent>> mediaEventListenerList = new CopyOnWriteArrayList<MediaEventListener<SignalDetectorEvent>>();
	protected MediaGroupImpl mediaGroup = null;
	protected final MediaSessionImpl mediaSession;
	protected final MgcpWrapper mgcpWrapper;
	// If receive method has been called and we are waiting for a good state when we will send RECEIVED
	public boolean receiving;
	public String numSignalsCache = new String();
	public int numSignalsLeft;
	public Parameter[] patterns;
	public Parameters optionlArgs;
	
	// If this detector is still technically listening for MGCP events from preious play commands, etc
	public boolean alreadyListening;

	private final MediaGroupConfig medGrpConfig;
	private List<EventName> eveNames = new ArrayList<EventName>();

	protected volatile SignalDetectorState state = SignalDetectorState.IDLE;

	public List<String> buffer = new LinkedList<String>();

	public SignalDetectorImpl(MediaGroupImpl mediaGroup, MgcpWrapper mgcpWrapper, MediaGroupConfig medGrpConfig) {
		this.mediaGroup = mediaGroup;
		this.mgcpWrapper = mgcpWrapper;
		this.medGrpConfig = medGrpConfig;
		this.mediaSession = (MediaSessionImpl) mediaGroup.getMediaSession();

		this.buffer = new ArrayList<String>();
	}

	public void flushBuffer() throws MsControlException {
		this.buffer.clear();
	}

	public void receiveSignals(int numSignals, Parameter[] patterns, RTC[] rtc, Parameters optargs)
			throws MsControlException {

		if (MediaObjectState.JOINED.equals(this.mediaGroup.getState())) {
			
			this.numSignalsLeft = numSignals;
			if(logger.isDebugEnabled()) {
				logger.debug("Receive numSignals:     " + numSignals + " already listeneing is " + alreadyListening);
			}
			this.patterns = patterns;
			this.optionlArgs = optargs;
			if(!this.alreadyListening) {
				// Only start new transactions if we are not detecting from previous commands
				Runnable tx = new MediaGroupRequestTx(mgcpWrapper, mediaGroup, null, 0, 0, false, true);
				Provider.submit(tx);
			}
			this.state = SignalDetectorState.DETECTING;
			this.receiving = true;

		} else {
			throw new MsControlException(this.mediaGroup.getURI() + " Container is not joined to any other container");
		}
	}

	public MediaGroup getContainer() {
		return this.mediaGroup;
	}

	public void stop() {
		if (this.state == SignalDetectorState.DETECTING) {
			//Runnable tx = new StopTx(this);
			//Provider.submit(tx);
			this.state = SignalDetectorState.IDLE;
			this.receiving = false;
		}
	}

	public void addListener(MediaEventListener<SignalDetectorEvent> listener) {
		if(logger.isDebugEnabled()) {
			logger.debug("LISTERNER    add : " + mediaEventListenerList.size());
		}
		this.mediaEventListenerList.add(listener);
	}

	public MediaSession getMediaSession() {
		return this.mediaSession;
	}

	public void removeListener(MediaEventListener<SignalDetectorEvent> listener) {
		if(logger.isDebugEnabled()) {
			logger.debug("LISTERNER    remove : " + mediaEventListenerList.size());
		}
		this.mediaEventListenerList.remove(listener);
	}

	public void update(SignalDetectorEvent anEvent) {
		if(logger.isDebugEnabled()) {
			logger.info("LISTERNER    update : " + mediaEventListenerList.size());
		}
		for (MediaEventListener<SignalDetectorEvent> m : mediaEventListenerList) {
			m.onEvent(anEvent);
		}
	}

	public static int paramEnumToIndex(ParameterEnum pE) {
		switch (pE) {
		case SD_PATTERN_0:
			return 0;
		case SD_PATTERN_1:
			return 1;
		case SD_PATTERN_2:
			return 2;
		case SD_PATTERN_3:
			return 3;
		case SD_PATTERN_4:
			return 4;
		case SD_PATTERN_5:
			return 5;
		case SD_PATTERN_6:
			return 6;
		case SD_PATTERN_7:
			return 7;
		case SD_PATTERN_8:
			return 8;
		case SD_PATTERN_9:
			return 9;
		case SD_PATTERN_10:
			return 10;
		case SD_PATTERN_11:
			return 11;
		case SD_PATTERN_12:
			return 12;
		case SD_PATTERN_13:
			return 13;
		case SD_PATTERN_14:
			return 14;
		case SD_PATTERN_15:
			return 15;
		case SD_PATTERN_16:
			return 16;
		case SD_PATTERN_17:
			return 17;
		case SD_PATTERN_18:
			return 18;
		case SD_PATTERN_19:
			return 19;
		case SD_PATTERN_20:
			return 20;
		case SD_PATTERN_21:
			return 21;
		case SD_PATTERN_22:
			return 22;
		case SD_PATTERN_23:
			return 23;
		case SD_PATTERN_24:
			return 24;
		case SD_PATTERN_25:
			return 25;
		case SD_PATTERN_26:
			return 26;
		case SD_PATTERN_27:
			return 27;
		case SD_PATTERN_28:
			return 28;
		case SD_PATTERN_29:
			return 29;
		case SD_PATTERN_30:
			return 30;
		case SD_PATTERN_31:
			return 21;

		}
		
		return -911;
	}
	
	public static String  mgcpEventToDTMF(MgcpEvent event) {
		switch (event.intValue()) {
		case MgcpEvent.DTMF_0:
			return "0";
		case MgcpEvent.DTMF_1:
			return "1";
		case MgcpEvent.DTMF_2:
			return "2";
		case MgcpEvent.DTMF_3:
			return "3";
		case MgcpEvent.DTMF_4:
			return "4";
		case MgcpEvent.DTMF_5:
			return "5";
		case MgcpEvent.DTMF_6:
			return "6";
		case MgcpEvent.DTMF_7:
			return "7";
		case MgcpEvent.DTMF_8:
			return "8";
		case MgcpEvent.DTMF_9:
			return "9";
		case MgcpEvent.DTMF_A:
			return "A";
		case MgcpEvent.DTMF_B:
			return "B";
		case MgcpEvent.DTMF_C:
			return "C";
		case MgcpEvent.DTMF_D:
			return "D";
		case MgcpEvent.DTMF_HASH:
			return "#";
		case MgcpEvent.DTMF_STAR:
			return "*";

		default:
			return null;

		}
	}

}
