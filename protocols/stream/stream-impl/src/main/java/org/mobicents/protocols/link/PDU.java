/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mobicents.protocols.link;

import java.nio.ByteBuffer;

/**
 *
 * @author kulikov
 */
public class PDU {
    public final static int OS = 0;
    public final static int DATA = 2;
    public final static int ACK = 3;
    public final static int ACTIVATING = 4;
    public final static int TEST = 6;
    
    private int type;
    private int seq;
    private byte[] payload;
    private boolean retransmission = false;
    
    protected PDU(int type, int seq, byte[] payload) {
        this.type = type;
        this.seq = seq;
        this.payload = payload;
    }
    
    protected PDU(ByteBuffer buffer, int len) {
        type = buffer.getInt();
        seq = buffer.getInt();
        
        byte b = buffer.get();
        if (b == 1) {
            this.retransmission = true;
        }
  
        if (buffer.hasRemaining()) {
            payload = new byte[len - 9]; 
            buffer.get(payload);
        }
    }
    
    public void setRetransmission() {
        this.retransmission = true;
    }
    
    public boolean isRTR() {
        return this.retransmission;
    }
    
    public int getType() {
        return type;
    }
    
    public int getSeq() {
        return seq;
    }
    
    public byte[] getPayload() {
        return payload;
    }
    
    public void write(ByteBuffer buffer) {
        buffer.clear();
        buffer.rewind();
        
        buffer.putInt(type);
        buffer.putInt(seq);
        if (this.retransmission) {
            buffer.put((byte)1);
        } else {
            buffer.put((byte)0);
        }
  
        if (payload != null) {
            buffer.put(payload);
        }
        
        buffer.flip();
    }
    
    @Override
    public String toString() {
        return String.format("PDU[type=%d, seq=%d, RTR=%b]", type, seq, retransmission);
    }
}
