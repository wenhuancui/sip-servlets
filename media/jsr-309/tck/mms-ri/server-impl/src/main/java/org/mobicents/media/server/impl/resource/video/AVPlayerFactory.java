package org.mobicents.media.server.impl.resource.video;

import org.mobicents.media.Component;
import org.mobicents.media.ComponentFactory;
import org.mobicents.media.server.spi.Endpoint;
import org.mobicents.media.server.spi.ResourceUnavailableException;

/**
 * 
 * @author amit bhayani
 *
 */
public class AVPlayerFactory implements ComponentFactory {
	
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Component newInstance(Endpoint endpoint) throws ResourceUnavailableException {
		AVPlayer p = new AVPlayer(name, endpoint.getTimer());
		p.setEndpoint(endpoint);
		return p;
	}

}
