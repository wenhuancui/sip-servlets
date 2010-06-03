/*
 * JBoss, Home of Professional Open Source
 * Copyright XXXX, Red Hat Middleware LLC, and individual contributors as indicated
 * by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a full listing
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 */

package org.mobicents.media.server.impl.resource.mediaplayer;

import java.net.URL;

import org.mobicents.media.Component;
import org.mobicents.media.ComponentFactory;
import org.mobicents.media.server.spi.Endpoint;
import org.mobicents.media.server.spi.ResourceUnavailableException;

/**
 * @author baranowb
 * @author kulikov
 */
public class MediaPlayerFactory implements ComponentFactory {

	private String name;
	private String audioMediaDirectory;
	private String videoMediaDirectory;

	/**
	 * @return the audioMediaDirectory
	 */
	public String getAudioMediaDirectory() {
		return audioMediaDirectory;
	}

	/**
	 * @param audioMediaDirectory
	 *            the audioMediaDirectory to set
	 */
	public void setAudioMediaDirectory(String audioMediaDirectory) {
		this.audioMediaDirectory = audioMediaDirectory;
	}

	/**
	 * @return the videoMediaDirectory
	 */
	public String getVideoMediaDirectory() {
		return videoMediaDirectory;
	}

	/**
	 * @param videoMediaDirectory
	 *            the videoMediaDirectory to set
	 */
	public void setVideoMediaDirectory(String videoMediaDirectory) {
		this.videoMediaDirectory = videoMediaDirectory;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Component newInstance(Endpoint endpoint)
			throws ResourceUnavailableException {
		return new MediaPlayerImpl(name, endpoint.getTimer(),
				audioMediaDirectory, videoMediaDirectory);

	}

	public void start() throws IllegalStateException {
		if (audioMediaDirectory == null) {
			throw new IllegalStateException("Audio media directory is not set!");
		}

		if (videoMediaDirectory == null) {
			throw new IllegalStateException("Video media directory is not set!");
		}
	}

}
