package org.mobicents.javax.media.mscontrol.mediagroup;

import javax.media.mscontrol.EventType;
import javax.media.mscontrol.MediaEvent;

import org.mobicents.javax.media.mscontrol.EventDetectorFactory;

public class RecorderEventDetectorFactory extends EventDetectorFactory {

	EventType mediaEventType = null;

	public RecorderEventDetectorFactory(String pkgName, String eventName, boolean isOnEndpoint, EventType mediaEventType, boolean isSuccessful) {
		super(pkgName, eventName, isOnEndpoint, isSuccessful);
		this.mediaEventType = mediaEventType;
	}

	@Override
	public MediaEvent generateMediaEvent() {
		RecorderEventImpl event = new RecorderEventImpl(this.mediaEventType);
		event.setSuccessful(this.isSuccessful);
		return event;
	}

	public EventType getMediaEventType() {
		return mediaEventType;
	}

}
