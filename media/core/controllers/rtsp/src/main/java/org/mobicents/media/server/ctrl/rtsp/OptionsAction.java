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

import java.util.concurrent.Callable;

import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.rtsp.RtspHeaders;
import org.jboss.netty.handler.codec.rtsp.RtspMethods;
import org.jboss.netty.handler.codec.rtsp.RtspResponseStatuses;
import org.jboss.netty.handler.codec.rtsp.RtspVersions;

/**
 * 
 * @author amit bhayani
 *
 */
public class OptionsAction implements Callable<HttpResponse> {

	private RtspController rtspController = null;
	private HttpRequest request = null;
	public final static String OPTIONS = RtspMethods.DESCRIBE.getName() + ", " + RtspMethods.SETUP.getName() + ", "
			+ RtspMethods.TEARDOWN.getName() + ", " + RtspMethods.PLAY.getName()+ ", " + RtspMethods.OPTIONS.getName();

	public OptionsAction(RtspController rtspController, HttpRequest request) {
		this.rtspController = rtspController;
		this.request = request;
	}

	public HttpResponse call() throws Exception {
		HttpResponse response = new DefaultHttpResponse(RtspVersions.RTSP_1_0, RtspResponseStatuses.OK);
		response.setHeader(HttpHeaders.Names.SERVER, RtspController.SERVER);
		response.setHeader(RtspHeaders.Names.CSEQ, this.request.getHeader(RtspHeaders.Names.CSEQ));
		response.setHeader(RtspHeaders.Names.PUBLIC, OPTIONS);
		return response;
	}
}
