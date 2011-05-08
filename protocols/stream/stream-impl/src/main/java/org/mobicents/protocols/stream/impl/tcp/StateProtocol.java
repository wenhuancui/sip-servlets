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

package org.mobicents.protocols.stream.impl.tcp;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.mobicents.protocols.stream.impl.hdlc.HDLCHandler;
import org.mobicents.protocols.stream.impl.tlv.LinkStatus;
import org.mobicents.protocols.stream.impl.tlv.TLVInputStream;
import org.mobicents.protocols.stream.impl.tlv.TLVOutputStream;
import org.mobicents.protocols.stream.impl.tlv.Tag;

/**
 * Utility class for all logic regarding link state protocol. This class hides
 * TLV details and how messages are encoded. <br>
 * It can be reused by different stream forwarders.
 * 
 * @author baranowb
 * 
 */
public class StateProtocol {

	private static final Logger logger = Logger.getLogger(StateProtocol.class);
	// /////////////////
	// Some statics //
	// ////////////////
	private static final byte[] _LINK_STATE_UP;
	private static final byte[] _LINK_STATE_DOWN;
	private static final byte[] _LINK_STATE_UP_ACK;
	private static final byte[] _LINK_STATE_DOWN_ACK;
	private static final byte[] _LINK_STATE_QUERY;
	static {
		TLVOutputStream tlv = new TLVOutputStream();
		try {
			tlv.writeLinkStatus(LinkStatus.LinkUp);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		_LINK_STATE_UP = tlv.toByteArray();
		tlv.reset();
		try {
			tlv.writeLinkStatus(LinkStatus.LinkDown);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		_LINK_STATE_DOWN = tlv.toByteArray();
		tlv.reset();
		try {
			tlv.writeLinkStatus(LinkStatus.Query);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		_LINK_STATE_QUERY = tlv.toByteArray();

		tlv.reset();
		try {
			LinkStatus status = LinkStatus.StateAck;
			status.setAcked(LinkStatus.LinkUp);
			tlv.writeLinkStatus(status);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		_LINK_STATE_UP_ACK = tlv.toByteArray();
		tlv.reset();
		try {
			LinkStatus status = LinkStatus.StateAck;
			status.setAcked(LinkStatus.LinkDown);
			tlv.writeLinkStatus(status);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		_LINK_STATE_DOWN_ACK = tlv.toByteArray();
	}

	public StateProtocol(AbstractTCPStream abstractStreamer) {
		super();
		this.reset();
		this.abstractStreamer = abstractStreamer;
	}

	private AbstractTCPStream abstractStreamer;
	private HDLCHandler hdlcHandler = new HDLCHandler();
	private ByteBuffer txBuff = ByteBuffer.allocate(8192);

	// /////////////////////////////
	// StateProtocol methods //
	// /////////////////////////////
	public void queryState() throws IOException {
		this.pushData(_LINK_STATE_QUERY);
	}

	public void indicateState(boolean markedReady) throws IOException {

		if (markedReady) {
			pushData(_LINK_STATE_UP);
		} else {
			pushData(_LINK_STATE_DOWN);
		}
	}

	public void acknowledge(LinkStatus ls) throws IOException {
		switch (ls) {
		case LinkUp:
			pushData(_LINK_STATE_UP_ACK);
			break;
		case LinkDown:
			pushData(_LINK_STATE_DOWN_ACK);
			break;
		default:
			logger.error("Tryinkg to ack wrong state: "+ls);
		}

	}

	/**
	 * Method called by stream handlers, Indicates that it received data from
	 * stream(TCP/UDP) and we have to process it
	 * 
	 * @param data
	 */
	public void streamDataReceived(ByteBuffer data) {
		// we received some data from stream
		// this means that we are connected, lets consume
		// consume;

		ByteBuffer[] readResult = null;

		while ((readResult = this.hdlcHandler.processRx(data)) != null) {

			for (ByteBuffer b : readResult) {

				try {
					TLVInputStream tlvInputStream = new TLVInputStream(new ByteArrayInputStream(b.array()));
					int tag = tlvInputStream.readTag();
					if (tag == Tag._TAG_LINK_DATA) {
						byte[] linkData = tlvInputStream.readLinkData();

						// we have some data, lets push this to streamer buffer
						// so it may mark
						// selectors ready to read
						abstractStreamer.dataReceived(linkData);

					} else if (tag == Tag._TAG_LINK_STATUS) {
						LinkStatus ls = tlvInputStream.readLinkStatus();
						switch (ls) {
						case LinkDown:
						case LinkUp:
						case StateAck:
							this.abstractStreamer.receivedState(ls);
							break;
						case Query:

							indicateState(this.abstractStreamer.isReady());
						}
					} else {
						logger.warn("Received weird message! Tag: " + tag);
					}
				} catch (IOException e) {
					//FIXME: close?
					e.printStackTrace();
				}
			}

		}
	}

	/**
	 * This method is called once some entity wants to stream data.
	 * LinkStateProtocol performs following:
	 * <ul>
	 * <li>encode</li> - encode with tlv
	 * <li>pass to streamer</li> - pass encoded data to streamer, which forwards
	 * encoded byte[] to second peer.
	 * </ul>
	 * 
	 * @param data
	 */
	public void streamDataToSend(byte[] data) throws IOException {
		// check some preconditions?
		try {

			// we are on provider side. This means remote is actaull MTP3 layer.

			TLVOutputStream tlo = new TLVOutputStream();
			tlo.writeData(data);

			pushData(tlo.toByteArray());

		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method expects TLV encoded data!
	 * 
	 * @param data
	 * @throws IOException
	 */
	private synchronized void pushData(byte[] data) throws IOException {
		// encode
		// this.txBuffer.add(ByteBuffer.wrap(data));

		ByteBuffer bb = ByteBuffer.allocate(data.length);
		bb.put(data);
		bb.flip();

		this.hdlcHandler.addToTxBuffer(bb);

		// while (!this.hdlcHandler.isTxBufferEmpty()) {
		if (!this.hdlcHandler.isTxBufferEmpty()) {

			// ByteBuffer buf = (ByteBuffer) txBuffer.get(0);
			txBuff.clear();
			try {

				this.hdlcHandler.processTx(txBuff);
			} catch (BufferOverflowException bbbb) {
				bbbb.printStackTrace();
			}

			txBuff.flip();
			abstractStreamer.streamData(txBuff);

			// we get one shot? is that ok?
		}

	}

	public void reset() {
		this.hdlcHandler = new HDLCHandler();
		this.txBuff.clear();
	}

	public static ByteBuffer copyToPosition(ByteBuffer data) {
		byte[] tw = new byte[data.limit()];
		byte[] source = data.array();
		System.arraycopy(source, 0, tw, 0, tw.length);
		return ByteBuffer.wrap(tw);
	}

}
