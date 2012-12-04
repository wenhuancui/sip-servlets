package org.mobicents.javax.media.mscontrol.mediagroup;

import javax.media.mscontrol.EventType;
import javax.media.mscontrol.MediaEvent;

import org.mobicents.javax.media.mscontrol.EventDetectorFactory;

public class PlayerEventDetectorFactory extends EventDetectorFactory {

	EventType mediaEventType = null;

	public PlayerEventDetectorFactory(String pkgName, String eventName, boolean isOnEndpoint, EventType mediaEventType,
			boolean isSuccessful) {
		super(pkgName, eventName, isOnEndpoint, isSuccessful);
		this.mediaEventType = mediaEventType;
	}

	@Override
	public MediaEvent generateMediaEvent() {
		PlayerEventImpl event = new PlayerEventImpl(this.mediaEventType); 
		event.setSuccessful(this.isSuccessful);
		return event;
	}

	public EventType getMediaEventType() {
		return mediaEventType;
	}
	
	@Override
	public String toString() {
		return super.toString() + " PlayerEventDetectorFactory[mediaEventType=" + mediaEventType + "]";
	}

}
