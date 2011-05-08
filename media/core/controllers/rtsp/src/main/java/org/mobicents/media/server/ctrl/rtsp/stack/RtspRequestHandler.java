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

package org.mobicents.media.server.ctrl.rtsp.stack;

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.base64.Base64Decoder;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;

/**
 * 
 * @author amit bhayani
 * 
 */
@ChannelPipelineCoverage("one")
public class RtspRequestHandler extends SimpleChannelUpstreamHandler {

	Logger logger = Logger.getLogger(RtspRequestHandler.class);

	private final RtspServerStackImpl rtspServerStackImpl;

	protected RtspRequestHandler(RtspServerStackImpl rtspServerStackImpl) {
		this.rtspServerStackImpl = rtspServerStackImpl;
	}

	@Override
	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) {
		RtspServerStackImpl.allChannels.add(e.getChannel());
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {

		Object o = e.getMessage();
		if (o instanceof ChannelBuffer) {
			ChannelBuffer c = (ChannelBuffer)o;
			byte[] b = new byte[c.capacity()];
			c.getBytes(0, b);
			logger.info("RTSP Request \n" + new String(b));
			
			return;
		}

		HttpRequest rtspRequest = (HttpRequest) e.getMessage();

		if (rtspRequest.getMethod().equals(HttpMethod.POST)) {
			if (logger.isInfoEnabled()) {
				logger.info("Received the POST Request. Changing the PipeLine");
			}

			ChannelPipeline p = ctx.getChannel().getPipeline();
			// p.replace("decoder", "frameDecoder",
			// new DelimiterBasedFrameDecoder(80, Delimiters
			// .nulDelimiter()));
			p.replace("decoder", "base64Decoder", new Base64Decoder());

		}

		rtspServerStackImpl.processRtspRequest(rtspRequest, e.getChannel());

	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		e.getCause().printStackTrace();
		e.getChannel().close();
	}
}
