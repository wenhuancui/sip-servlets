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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.media.mscontrol.MediaErr;
import javax.media.mscontrol.MediaEventListener;
import javax.media.mscontrol.MediaSession;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.Parameters;
import javax.media.mscontrol.Qualifier;
import javax.media.mscontrol.Value;
import javax.media.mscontrol.mediagroup.MediaGroup;
import javax.media.mscontrol.mediagroup.Player;
import javax.media.mscontrol.mediagroup.PlayerEvent;
import javax.media.mscontrol.resource.RTC;

import org.apache.log4j.Logger;
import org.mobicents.javax.media.mscontrol.DefaultEventGeneratorFactory;
import org.mobicents.javax.media.mscontrol.EventExecutor;
import org.mobicents.javax.media.mscontrol.MediaObjectState;
import org.mobicents.javax.media.mscontrol.MediaSessionImpl;
import org.mobicents.jsr309.mgcp.MgcpWrapper;
import org.mobicents.jsr309.mgcp.Provider;
import org.mobicents.protocols.mgcp.stack.JainMgcpExtendedListener;

/**
 * b
 * @author amit bhayani
 * 
 */
public class PlayerImpl implements Player {

	private static final String ZERO = "zero";
	private static final String ONE = "one";
	private static final String TWO = "two";
	private static final String THREE = "three";
	private static final String FOUR = "four";
	private static final String FIVE = "five";
	private static final String SIX = "six";
	private static final String SEVEN = "seven";
	private static final String EIGHT = "eight";
	private static final String NINE = "nine";

	// 0.5 sec of tolerance
	static final int PLAY_TOLERANCE = 500;

	private static Logger logger = Logger.getLogger(PlayerImpl.class);
	protected MediaGroupImpl mediaGroup = null;
	protected CopyOnWriteArrayList<MediaEventListener<PlayerEvent>> mediaEventListenerList = new CopyOnWriteArrayList<MediaEventListener<PlayerEvent>>();

	protected final MediaSessionImpl mediaSession;
	protected final MgcpWrapper mgcpWrapper;
	private final MediaGroupConfig medGrpConfig;

	protected volatile PlayerState state = PlayerState.IDLE;

	volatile long startTime = 0l;

	private volatile LinkedList<Runnable> txList = new LinkedList<Runnable>();

	private List<EventName> eveNames = new ArrayList<EventName>();

	protected PlayerImpl(MediaGroupImpl mediaGroup, MgcpWrapper mgcpWrapper, MediaGroupConfig medGrpConfig)
			throws MsControlException {
		this.mediaGroup = mediaGroup;
		this.mediaSession = (MediaSessionImpl) mediaGroup.getMediaSession();
		this.mgcpWrapper = mgcpWrapper;
		this.medGrpConfig = medGrpConfig;

	}

	void updateState() {
		if (txList.size() == 0) {
			this.state = PlayerState.IDLE;
		} else {
			this.state = PlayerState.ACTIVE;
		}
	}

	void executeNextTx() {
		Runnable nextTx = txList.poll();
		if (nextTx != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Executing next Tx in TxList");
			}
			this.state = PlayerState.ACTIVE;
			Provider.submit(nextTx);
		}
	}

	private void checkURI(URI[] uris) throws MsControlException {
		if (uris == null) {
			throw new MsControlException("URI[] cannot be null");
		}

		for (URI uri : uris) {

			if (uri == null) {
				throw new MsControlException("URI cannot be null");
			}
			
			if(uri.getScheme().equalsIgnoreCase("data")) continue;

			InputStream is = null;
			try {
				is = uri.toURL().openStream();
			} catch (MalformedURLException e) {
				logger.error("Cannot play the file", e);
				PlayerEventImpl event = new PlayerEventImpl(this, PlayerEvent.PLAY_COMPLETED, false, MediaErr.BAD_ARG,
						"Error " + e.getMessage());
				this.update(event);
				break;
			} catch (IOException e) {
				logger.error("Cannot play the file", e);

				PlayerEventImpl event = new PlayerEventImpl(this, PlayerEvent.PLAY_COMPLETED, false,
						MediaErr.NOT_FOUND, "Error " + e.getMessage());
				this.update(event);
				break;
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						logger.error("Closing of Stream failed", e);
					}
				}
			}
		}
	}

	// Player methods
	public void play(URI[] uris, RTC[] arg1, Parameters params) throws MsControlException {
		this.checkURI(uris);
		if (MediaObjectState.JOINED.equals(this.mediaGroup.getState())) {
			Parameters p = null;
			if (params != Parameters.NO_PARAMETER) {
				p = this.mediaGroup.createParameters();
				p.putAll(this.medGrpConfig.getParametersImpl());
				p.putAll(params);
			} else {
				p = this.medGrpConfig.getParametersImpl();
			}
			mediaGroup.detector.alreadyListening = true;
			this.executeTx(uris, arg1, p);

		} else {
			throw new MsControlException(this.mediaGroup.getURI() + " Container is not joined to any other container");
		}

	}

	public void play(URI uri, RTC[] arg1, Parameters params) throws MsControlException {
		URI[] uris = new URI[] { uri };
		this.checkURI(uris);
		if (MediaObjectState.JOINED.equals(this.mediaGroup.getState())) {
			Parameters p = null;
			if (params != Parameters.NO_PARAMETER) {
				p = this.mediaGroup.createParameters();
				p.putAll(this.medGrpConfig.getParametersImpl());
				p.putAll(params);
			} else {
				p = this.medGrpConfig.getParametersImpl();
			}
			if(mediaGroup.detector != null) {
				mediaGroup.detector.alreadyListening = true;
			}
			this.executeTx(uris, arg1, p);

		} else {
			throw new MsControlException(this.mediaGroup.getURI() + " Container is not joined to any other container");
		}

	}

	private int getRepeatCount(String repeatCount) {
		if (ZERO.equalsIgnoreCase(repeatCount)) {
			return 0;
		} else if (ONE.equalsIgnoreCase(repeatCount)) {
			return 1;
		} else if (TWO.equalsIgnoreCase(repeatCount)) {
			return 2;
		} else if (THREE.equalsIgnoreCase(repeatCount)) {
			return 3;
		} else if (FOUR.equalsIgnoreCase(repeatCount)) {
			return 4;
		} else if (FIVE.equalsIgnoreCase(repeatCount)) {
			return 5;
		} else if (SIX.equalsIgnoreCase(repeatCount)) {
			return 6;
		} else if (SEVEN.equalsIgnoreCase(repeatCount)) {
			return 7;
		} else if (EIGHT.equalsIgnoreCase(repeatCount)) {
			return 8;
		} else if (NINE.equalsIgnoreCase(repeatCount)) {
			return 9;
		}

		return 0;
	}

	private void executeTx(URI[] uris, RTC[] arg1, Parameters params) throws MsControlException {
		Value action_if_busy = QUEUE_IF_BUSY;
		int repeatCount = 0;
		int interval = 0;
		int startOffset = 0;
		int maxDuration = FOREVER;
		boolean update = true;
		LinkedList<URI> uriList = new LinkedList<URI>();

		for (URI uri : uris) {
			uriList.add(uri);
		}

		action_if_busy = (Value) params.get(BEHAVIOUR_IF_BUSY);
		Object obj = params.get(REPEAT_COUNT);
		if (obj instanceof String) {
			repeatCount = this.getRepeatCount((String) obj);
		} else {
			repeatCount = (Integer) obj;
		}

		if (repeatCount < 1) {
			repeatCount = 1;
		}

		interval = (Integer) params.get(INTERVAL);
		if (interval < 0) {
			interval = 0;
		}
		maxDuration = (Integer) params.get(MAX_DURATION);
		if (maxDuration < 1) {
			maxDuration = FOREVER;
		}
		startOffset = (Integer) params.get(START_OFFSET);
		if (repeatCount > 1) {
			update = false;
		}

		if (this.state == PlayerState.ACTIVE) {

			if (action_if_busy == Player.QUEUE_IF_BUSY) {
				Runnable tx = new MediaGroupRequestTx(mgcpWrapper, mediaGroup, uriList, startOffset, maxDuration, update, this.mediaGroup.detector != null);
				txList.add(tx);
				logger.info("Queuing new player transaction");
			} else if (action_if_busy == Player.STOP_IF_BUSY) {
				logger.info("Stopping current player transaction and starting new one");
				txList.clear();
				Runnable tx = new MediaGroupRequestTx(mgcpWrapper, mediaGroup, uriList, startOffset, maxDuration, update, this.mediaGroup.detector != null);
				txList.add(tx);

				// Stop the Player first (NO NEED TO STOP THE PLAYER, THE MGCP WILL OVERRIDE THE PREVIOUS PLAY)
				//Runnable tx1 = new StopTx(mgcpWrapper, mediaGroup);
				//Provider.submit(tx1);
				executeNextTx();
			} else if (action_if_busy == Player.FAIL_IF_BUSY) {
				throw new MsControlException("Player is busy");
			} else {
				logger
						.error("The Value "
								+ action_if_busy
								+ " is not recognized for Parameter p_IfBusy. It has to be one of Player.v_Queue, Player.v_Stop or Player.v_Fail");
			}

		} else {
			Runnable tx = new MediaGroupRequestTx(mgcpWrapper, mediaGroup, uriList, startOffset, maxDuration, update, this.mediaGroup.detector != null);
			Provider.submit(tx);
		}
		this.state = PlayerState.ACTIVE;

		for (int i = 1; i < repeatCount; i++) {
			if (interval != 0) {
				Runnable tx = new IntervalTx(interval);
				txList.add(tx);
			}
			if (i == (repeatCount - 1)) {
				update = true;
			}
			LinkedList<URI> uriListTemp = new LinkedList<URI>();
			for (URI uri : uris) {
				uriListTemp.add(uri);
			}
			Runnable tx = new MediaGroupRequestTx(mgcpWrapper, mediaGroup, uriListTemp, startOffset, maxDuration, update, this.mediaGroup.detector != null);
			txList.add(tx);
		}
	}

	// Resource Methods
	public MediaGroup getContainer() {
		return this.mediaGroup;
	}

	public void stop(boolean stopAll) {
		txList.clear();
		if (this.state == PlayerState.ACTIVE) {
			Runnable tx = new MediaGroupStopTx(mgcpWrapper,mediaGroup);
			Provider.submit(tx);
		}
	}

	// MediaEventNotifier methods
	public void addListener(MediaEventListener<PlayerEvent> listener) {
		this.mediaEventListenerList.add(listener);
	}

	public void removeListener(MediaEventListener<PlayerEvent> listener) {
		this.mediaEventListenerList.remove(listener);
	}

	public MediaSession getMediaSession() {
		return this.mediaGroup.getMediaSession();
	}

	protected void update(PlayerEvent anEvent) {
		int offset = (int) (System.currentTimeMillis() - this.startTime);
		((PlayerEventImpl) anEvent).setOffset(offset);
		
		//TODO : This is hack. Introduce threads at Listener level rather
		for (MediaEventListener<PlayerEvent> m : mediaEventListenerList) {
			EventExecutor exe = new EventExecutor(m, anEvent);
			Provider.submit(exe);
		}
	}


	private class IntervalTx implements Runnable {

		int interval = 0;

		IntervalTx(int interval) {
			this.interval = interval;
		}

		public void run() {
			try {
				logger.info("The Interval for " + this.interval);
				Thread.sleep(interval);
			} catch (InterruptedException e) {
				// DO we care? No we dont@bb
				logger.warn("Huh", e);
			}
			executeNextTx();
		}

	}

}
