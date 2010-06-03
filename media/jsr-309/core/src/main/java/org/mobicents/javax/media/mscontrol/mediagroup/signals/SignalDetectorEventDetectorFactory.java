package org.mobicents.javax.media.mscontrol.mediagroup.signals;

import javax.media.mscontrol.EventType;
import javax.media.mscontrol.MediaEvent;

import org.mobicents.javax.media.mscontrol.EventDetectorFactory;

public class SignalDetectorEventDetectorFactory extends EventDetectorFactory {

	EventType mediaEventType = null;

	public SignalDetectorEventDetectorFactory(String pkgName, String eventName, boolean isOnEndpoint,
			EventType mediaEventType, boolean isSuccessful) {
		super(pkgName, eventName, isOnEndpoint, isSuccessful);
		this.mediaEventType = mediaEventType;
	}

	@Override
	public MediaEvent generateMediaEvent() {
		SignalDetectorEventImpl event = new SignalDetectorEventImpl(this.mediaEventType);
		event.setSuccessful(this.isSuccessful);
		return event;
	}

	public EventType getMediaEventType() {
		return mediaEventType;
	}

}
