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
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.mobicents.protocols.stream.api.SelectorKey;
import org.mobicents.protocols.stream.api.StreamSelector;
import org.mobicents.protocols.stream.api.tcp.StreamState;

/**
 * @author baranowb
 * 
 */
public class ServerTCPStreamImpl extends AbstractTCPStream {

	private static final Logger logger = Logger.getLogger(ServerTCPStreamImpl.class);

	protected ServerTCPStreamImpl(InetSocketAddress address) {
		super(address);
		// TODO Auto-generated constructor stub
	}

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.protocols.stream.impl.AbstractStreamer#cleanSocket()
	 */
	@Override
	protected void cleanSocket() {
		// TODO Auto-generated method stub
		super.cleanSocket();
		if (this.serverSocketChannel != null) {
			try {
				this.serverSocketChannel.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.serverSocketChannel = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.protocols.stream.impl.AbstractStreamer#close()
	 */
	@Override
	public void close() {
		// TODO Auto-generated method stub
		super.close();
		cleanSocket();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.protocols.stream.impl.AbstractStreamer#open()
	 */
	public void open() throws IOException {
		// TODO Auto-generated method stub
		super.open(serverSocketChannel);
		initSocket();
	}

	// ///////////////////////////
	// Non common NIO Part o_0 //
	// ///////////////////////////
	private ServerSocketChannel serverSocketChannel;

	private void initSocket() throws IOException {
		this.connectSelector = SelectorProvider.provider().openSelector();
		this.serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.configureBlocking(false);

		// Bind the server socket to the specified address and port

		serverSocketChannel.socket().bind(super.address);

		// Register the server socket channel, indicating an interest in
		// accepting new connections
		serverSocketChannel.register(this.connectSelector, SelectionKey.OP_ACCEPT);
		if (logger.isInfoEnabled()) {
			logger.info("Initiaited server on: " + super.address);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.protocols.stream.impl.AbstractStreamer#tryConnect()
	 */
	@Override
	protected void tryConnect() throws IOException {
		if (this.serverSocketChannel == null) {
			initSocket();
		}
		if (state == StreamState.OPEN) {
			setState(StreamState.CONNECTING);
		} else if (state != StreamState.CONNECTING) {
			logger.warn("Wrong state, on tryConnect: " + this.state);
		}
		// block till we have someone subscribing for data.
		if (this.connectSelector.selectNow() > 0) {

			Iterator<SelectionKey> selectedKeys = this.connectSelector.selectedKeys().iterator();
			// operate on keys set
			try {

				performKeyConnectOperations(selectedKeys);

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
			this.accept(key);
		}

	}

	private void accept(SelectionKey key) throws IOException {
		if (connected) {
			if (logger.isInfoEnabled()) {
				logger.info("Second client not supported yet.");
			}

			return;
		}
		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

		super.socketChannel = serverSocketChannel.accept();
		this.writeSelector = SelectorProvider.provider().openSelector();
		this.readSelector = SelectorProvider.provider().openSelector();
		Socket socket = socketChannel.socket();

		this.socketChannel.configureBlocking(false);
		this.socketChannel.register(this.readSelector, SelectionKey.OP_READ);
		this.socketChannel.register(this.writeSelector, SelectionKey.OP_WRITE);
		connected = true;
		setState(StreamState.CONNECTED);

		if (logger.isInfoEnabled()) {
			logger.info("Estabilished connection with: " + socket.getInetAddress() + ":" + socket.getPort());

		}

	}

    public org.mobicents.protocols.stream.api.SelectorProvider provider() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public SelectorKey register(StreamSelector selector) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
