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

package org.mobicents.media.server.ctrl.rtsp;

import java.util.Set;

import org.mobicents.media.server.spi.MediaType;

/**
 * 
 * @author amit bhayani
 *
 */
public class RequestParser {

	private static final String AUDIO_SUFFIX = "/audio";
	private static final String VIDEO_SUFFIX = "/video";

	private String uriPath;
	private MediaType mediaType;
	private String endpointName;
	private String mediaFile;

	public RequestParser(String uriPath, Set<String> endpointNames) {
		this.uriPath = uriPath;

		String mediaPath = uriPath;

		if (mediaPath.endsWith("/audio")) {
			mediaPath = mediaPath.substring(0, mediaPath.indexOf(AUDIO_SUFFIX));
			mediaType = MediaType.AUDIO;
		} else if (mediaPath.endsWith("/video")) {
			mediaPath = mediaPath.substring(0, mediaPath.indexOf(VIDEO_SUFFIX));
			mediaType = MediaType.VIDEO;
		}

		for (String endptNameTmp : endpointNames) {
			if (mediaPath.startsWith(endptNameTmp)) {
				int index = mediaPath.indexOf("/", endptNameTmp.length());
				if (index != -1) {
					endpointName = mediaPath.substring(0, index);
					mediaFile = mediaPath.substring((index + 1), mediaPath.length());
				} else {
					endpointName = mediaPath;
				}
				break;
			}

		}
	}

	public String getUriPath() {
		return uriPath;
	}

	public MediaType getMediaType() {
		return mediaType;
	}

	public String getEndpointName() {
		return endpointName;
	}

	public String getMediaFile() {
		return mediaFile;
	}

}
