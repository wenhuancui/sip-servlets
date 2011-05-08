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

/**
 * 
 */
package org.mobicents.protocols.stream.impl.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;
import org.mobicents.protocols.stream.api.SelectorKey;
import org.mobicents.protocols.stream.api.StreamSelector;
import org.mobicents.protocols.stream.api.tcp.StreamState;

/**
 * @author baranowb
 * 
 */
public class ClientTCPStreamImpl extends AbstractTCPStream {

	private static final Logger logger = Logger.getLogger(ClientTCPStreamImpl.class);

	protected ClientTCPStreamImpl(InetSocketAddress remoteAddress, InetSocketAddress address) {
		super(remoteAddress, address);
		// initSocket();

	}

	private void initSocket() {

		try {
			super.socketChannel = SocketChannel.open();
			super.socketChannel.configureBlocking(false);
			super.socketChannel.socket().bind(this.address);
			super.connectSelector = SelectorProvider.provider().openSelector();

			this.socketChannel.register(this.connectSelector, SelectionKey.OP_CONNECT);
			if (logger.isInfoEnabled()) {
				logger.info("Trying connection to: " + super.remoteAddress);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// /*
	// * (non-Javadoc)
	// *
	// * @see
	// * org.mobicents.protocols.stream.impl.AbstractStreamer#streamData(java.
	// * nio.ByteBuffer)
	// */
	// @Override
	// void streamData(ByteBuffer txBuff) throws IOException {
	// //callback from link state :)
	// //FIXME: make this local buff.
	// ByteBuffer toSendData = StateProtocol.copyToPosition(txBuff);
	//
	// while (toSendData.remaining() > 0)
	// {
	// super.socketChannel.write(toSendData);
	// }
	//
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.protocols.stream.api.Streamer#isReady()
	 */
	public boolean isReady() {
		// TODO Auto-generated method stub
		return connected && markedReady;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.protocols.stream.api.Streamer#open()
	 */
	public void open() throws IOException {
		super.open(socketChannel);
		initSocket();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.protocols.stream.impl.AbstractStreamer#close()
	 */
	@Override
	public void close() {

		super.close();
		// let it rip here.
		// we dont need to extend this method in client mode.
		this.cleanSocket();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.protocols.stream.api.Streamer#write(byte[])
	 */
	public int write(byte[] d) {
		if (!connected) {
			throw new IllegalStateException("Stream handlers are not connected!");
		}

		if (!markedReady) {
			throw new IllegalStateException("Stream handlers are not ready!");
		}

		try {
			this.stateProtocol.streamDataToSend(d);
			return d.length;
		} catch (IOException e) {
			// FIXME: handle this
			e.printStackTrace();
		}
		return -1;

	}

	
	
	/* (non-Javadoc)
	 * @see org.mobicents.protocols.stream.impl.tcp.AbstractTCPStream#cleanSocket()
	 */
	@Override
	protected void cleanSocket() {
		this.markedReady  = false; 
		super.cleanSocket();
	}

	@Override
	protected void tryConnect() throws IOException {
		// do some checks;
		if (state == StreamState.OPEN) {
			setState(StreamState.CONNECTING);
		} else if (state != StreamState.CONNECTING) {
			logger.warn("Wrong state, on tryConnect: " + this.state);
		}
		if (super.socketChannel == null) {
			initSocket();
		}
		super.socketChannel.connect(super.remoteAddress);

		if (this.connectSelector.select() > 0) {
			Set<SelectionKey> selectedKeys = this.connectSelector.selectedKeys();
			try {

				performKeyConnectOperations(selectedKeys.iterator());

			} catch (java.net.ConnectException ce) {
				// this is ok
				if (logger.isDebugEnabled()) {
					logger.debug("Connection failure:", ce);
				}
				// propably fail to connect, lets wait 5s
				return;
			} catch (IOException ioe) {
				ioe.printStackTrace();
				// propably fail to connect, lets wait 5s
				cleanSocket();
				setState(StreamState.OPEN);
				return;
			}
		}
	}

	private void performKeyConnectOperations(Iterator<SelectionKey> selectedKeys) throws IOException {
		while (selectedKeys.hasNext()) {
			SelectionKey key = selectedKeys.next();
			// THIS MUST BE PRESENT!
			selectedKeys.remove();

			if (!key.isValid()) {
				// handle disconnect here?
				logger.error("Key has become invalid: " + key);
				continue;
			}
			this.connect(key);
		}

	}

	private void connect(SelectionKey key) throws IOException {
		// here socket wants to connect
		SocketChannel socketChannel = (SocketChannel) key.channel();
		// this will throw exception if fail happens
		if (!socketChannel.finishConnect()) {
			throw new IOException("Not in correct time, will retry connection shortly");
		}

		this.writeSelector = SelectorProvider.provider().openSelector();
		this.readSelector = SelectorProvider.provider().openSelector();
		this.socketChannel.register(this.readSelector, SelectionKey.OP_READ);
		this.socketChannel.register(this.writeSelector, SelectionKey.OP_WRITE);
		connected = true;
		setState(StreamState.CONNECTED);
		if (logger.isDebugEnabled()) {
			logger.debug("Connected to server,  " + this.socketChannel.socket().getRemoteSocketAddress() + ", local connection "
					+ this.socketChannel.socket().getLocalAddress() + ":" + this.socketChannel.socket().getLocalPort());
		}

	}

    public org.mobicents.protocols.stream.api.SelectorProvider provider() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public SelectorKey register(StreamSelector selector) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

	
}
