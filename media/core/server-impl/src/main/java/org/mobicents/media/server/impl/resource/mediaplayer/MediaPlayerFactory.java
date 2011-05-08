/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.mobicents.media.server.impl.resource.mediaplayer;

import org.mobicents.media.Component;
import org.mobicents.media.ComponentFactory;
import org.mobicents.media.server.impl.resource.mediaplayer.audio.AudioPlayerFactory;
import org.mobicents.media.server.impl.resource.mediaplayer.video.VideoPlayerFactory;
import org.mobicents.media.server.spi.Endpoint;
import org.mobicents.media.server.spi.ResourceUnavailableException;

/**
 * @author baranowb
 * @author kulikov
 */
public class MediaPlayerFactory implements ComponentFactory {

	private String name;

	private AudioPlayerFactory audioPlayerFactory;
	private VideoPlayerFactory videoPlayerFactory;

	public AudioPlayerFactory getAudioPlayerFactory() {
		return audioPlayerFactory;
	}

	public void setAudioPlayerFactory(AudioPlayerFactory audioPlayerFactory) {
		this.audioPlayerFactory = audioPlayerFactory;
	}

	public VideoPlayerFactory getVideoPlayerFactory() {
		return videoPlayerFactory;
	}

	public void setVideoPlayerFactory(VideoPlayerFactory videoPlayerFactory) {
		this.videoPlayerFactory = videoPlayerFactory;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Component newInstance(Endpoint endpoint)
			throws ResourceUnavailableException {
		return new MediaPlayerImpl(name, this.audioPlayerFactory,
				this.videoPlayerFactory);

	}

	public void start() throws IllegalStateException {
		if (audioPlayerFactory == null) {
			throw new IllegalStateException("Audio media factory is not set!");
		}

		if (videoPlayerFactory == null) {
			throw new IllegalStateException("Video media factory is not set!");
		}
	}

}
