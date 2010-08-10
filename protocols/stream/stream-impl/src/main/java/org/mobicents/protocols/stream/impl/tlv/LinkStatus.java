package org.mobicents.protocols.stream.impl.tlv;

public enum LinkStatus {

	LinkUp((byte) 1), LinkDown((byte) 0), Query((byte) 2), StateAck((byte) 3);

	private byte status;
	private LinkStatus acked;

	LinkStatus(byte b) {
		this.status = b;
	}

	/**
	 * @return the status
	 */
	public byte getStatus() {
		return status;
	}

	public void setAcked(LinkStatus fromByte) {
		this.acked = fromByte;

	}

	public LinkStatus getAcked() {
		return this.acked;

	}

	public LinkStatus getFromByte(byte b) {
		if (b == 1) {
			return LinkUp;

		} else if (b == 0) {
			return LinkDown;
		} else if (b == 2) {
			return Query;
		} else if (b == 3) {
			return StateAck;
		} else {
			throw new IllegalArgumentException("No state associated with: " + b);
		}
	}

}
