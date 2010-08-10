package org.mobicents.protocols.stream.impl.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.mobicents.protocols.stream.api.StreamSelector;
import org.mobicents.protocols.stream.api.tcp.StreamState;
import org.mobicents.protocols.stream.api.tcp.TCPStream;
import org.mobicents.protocols.stream.impl.AbstractStream;
import org.mobicents.protocols.stream.impl.tlv.LinkStatus;

public abstract class AbstractTCPStream extends AbstractStream implements TCPStream {

    private static final Logger logger = Logger.getLogger(AbstractTCPStream.class);
    protected InetSocketAddress address,  remoteAddress;    // Data is stored here, since in one go stream handler can receive more than
    // one MSU, depends on net congestion.
    // FIXME: Impl it on array as ring buffer, just like MTP2 has it.
    protected LinkedList<byte[]> linkData = new LinkedList<byte[]>();
    protected boolean markedReady = false;
    // what has been sent
    protected boolean lastMarketRemote = false;
    protected StateProtocol stateProtocol = new StateProtocol(this);
    protected StreamState state = StreamState.CLOSED;
    protected SelectableChannel channel;

    protected AbstractTCPStream(InetSocketAddress address) {
        super();
        this.address = address;
    }

    protected AbstractTCPStream(InetSocketAddress remoteAddress, InetSocketAddress address) {
        super();
        this.remoteAddress = remoteAddress;
        this.address = address;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mobicents.protocols.stream.api.tcp.TCPStream#getState()
     */
    public StreamState getState() {
        return state;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mobicents.protocols.stream.api.Streamer#getAddress()
     */
    public InetSocketAddress getAddress() {
        // TODO Auto-generated method stub
        return address;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mobicents.protocols.stream.api.Streamer#getRemoteAddress()
     */
    public InetSocketAddress getRemoteAddress() {
        // TODO Auto-generated method stub
        return remoteAddress;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mobicents.protocols.stream.api.Streamer#close()
     */
    public void close() {
        // dont use that
        if (state == StreamState.CLOSED) {
            return;
        }
        setState(StreamState.CLOSED);
        // for (AbstractStreamSelector ass : this.selectors) {
        while (this.selectors.size() > 0) {
            StreamSelector ass = this.selectors.remove(0);
            //ass.close();
            //deregister, since selector can be used for more than one stream.
//            ass.deregister(this);

        }

        //cleanSocket();
        linkData.clear();
        this.markedReady = false;
        this.lastMarketRemote = false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mobicents.protocols.stream.api.Streamer#open()
     */
    public void open(SelectableChannel channel) throws IOException {
        //assign channel 
        this.channel = channel;
        
        //prepare states
        if (state != StreamState.CLOSED) {
            throw new IllegalStateException("Wrong state: " + state);
        }
        //
        stateProtocol.reset();
        setState(StreamState.OPEN);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mobicents.protocols.stream.api.Streamer#ready(boolean)
     */
    public void ready(boolean b) {
        this.markedReady = b;
        // check this
        if (connected) {
            switch (state) {
                case CONNECTED:
                    if (lastMarketRemote != this.markedReady) {
                        try {
                            stateProtocol.indicateState(this.markedReady);
                            this.lastMarketRemote = this.markedReady;
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                    }
                    if (this.markedReady) {
                        setState(StreamState.ACTIVATING);
                    }
                    break;
                case ACTIVATING:
                case INSERVICE:
                    if (lastMarketRemote != this.markedReady) {
                        try {
                            stateProtocol.indicateState(this.markedReady);
                            this.lastMarketRemote = this.markedReady;
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        if (!this.markedReady) {
                            setState(StreamState.CONNECTED);
                        }
                    }
                    break;

            }

        }

    }

    /**
     * Sends passed data to remote end. It creates copy of passed arg so buffer
     * can be safely reused.
     * 
     * @param txBuff
     * @throws IOException
     */
    void streamData(ByteBuffer txBuff) throws IOException {
        // callback from link state :)
        // FIXME: make this local buff.
        ByteBuffer toSendData = StateProtocol.copyToPosition(txBuff);

        while (toSendData.remaining() > 0) {
            this.socketChannel.write(toSendData);
        }

    }

    /**
     * Passed arg is data received and decoded by StateProtocol.
     * 
     * @param linkData
     */
    public void dataReceived(byte[] linkData) {

        boolean receivable = false;
        for (StreamSelector ass : selectors) {
            super.markOp(ass.OP_READ, this, ass);
            // ass.markOp(ass.OP_READ,this);
//            if (ass.isReadOperation()) {
//                receivable = true;
//            }
        }
        if (receivable) {
            this.linkData.add(linkData);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mobicents.protocols.stream.api.Streamer#read(byte[])
     */
    public int read(byte[] b) throws IOException {
        // Oleg did not want to listen to me.

        // FIXME: add check on state here.
        if (this.linkData.size() == 0) {
            return 0;
        }
        byte[] d = this.linkData.getFirst();
        if (d.length > b.length) {
            throw new IOException();
        } else {
            linkData.removeFirst();
        }

        System.arraycopy(d, 0, b, 0, d.length);

        return d.length;
    }    // ////////////////////////////
    // Selector invoked methods //
    // ////////////////////////////

    // ///////////////////
    // NIO common part //
    // ///////////////////
    protected Selector writeSelector;
    protected Selector readSelector;
    protected Selector connectSelector;
    private ByteBuffer readBuff = ByteBuffer.allocate(8192);    // simple bool, its easier to check.
    protected boolean connected = false;
    protected SocketChannel socketChannel;

    protected void impSelectNow() throws IOException {
        if (!connected) {
            tryConnect();
        } else {
            doRWOperations();
        }

        // for(StreamSelector ass:this.selectors)
        // {
        // if(ass.isReadOperation()||ass.isWriteOperation())
        // {
        // return;
        // }
        // }

        return;
    }

    protected void doRWOperations() throws IOException {
        Iterator selectedKeys = null;

        // else we try I/O ops.

        if (this.readSelector.selectNow() > 0) {
            selectedKeys = this.readSelector.selectedKeys().iterator();
            // operate on keys set

            performKeyReadOperations(selectedKeys);

        } else {
            // check for local data
            if (this.linkData.size() > 0) {
                for (StreamSelector ass : this.selectors) {
                    super.markOp(ass.OP_READ, this, ass);
                // ass.markOp(ass.OP_READ,this);

                }
            }
        }

        if (this.writeSelector.selectNow() > 0) {

            selectedKeys = this.writeSelector.selectedKeys().iterator();
            // operate on keys set

            performKeyWriteOperations(selectedKeys);

        }
    }

    protected abstract void tryConnect() throws IOException;

    private void performKeyReadOperations(Iterator<SelectionKey> selectedKeys) throws IOException {
        while (selectedKeys.hasNext()) {
            SelectionKey key = selectedKeys.next();
            // THIS MUST BE PRESENT!
            selectedKeys.remove();

            if (!key.isValid()) {
                // handle disconnect here?
                logger.error("Key has become invalid: " + key);
                continue;
            }

            // Check what event is available and deal with i
            this.read(key);

        }

    }

    private void performKeyWriteOperations(Iterator<SelectionKey> selectedKeys) throws IOException {
        while (selectedKeys.hasNext()) {
            SelectionKey key = selectedKeys.next();
            // THIS MUST BE PRESENT!
            selectedKeys.remove();

            if (!key.isValid()) {
                // handle disconnect here?
                logger.error("Key has become invalid: " + key);
                continue;
            }

            for (StreamSelector ass : super.selectors) {
                super.markOp(ass.OP_WRITE, this, ass);
            // ass.markOp(ass.OP_WRITE,this);
            }
        }

    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();

        // FIXME: we must ensure that we have whole frame here?
        // Clear out our read buffer so it's ready for new data
        this.readBuff.clear();

        // Attempt to read off the channel
        int numRead = -1;
        try {
            numRead = socketChannel.read(this.readBuff);

        } catch (IOException e) {
            // The remote forcibly closed the connection, cancel
            // the selection key and close the channel.
            // if(logger.isDebugEnabled())
            // {
            e.printStackTrace();
            // }
            handleClose(key);
            return;
        }

        if (numRead == -1) {
            // Remote entity shut the socket down cleanly. Do the
            // same from our end and cancel the channel.
            handleClose(key);
            return;
        } else if (numRead == this.readBuff.capacity()) {
            return;
        }

        this.readBuff.flip();
        if (logger.isDebugEnabled()) {
            logger.debug("Received data: " + this.readBuff);
        }
        try {
            this.stateProtocol.streamDataReceived(readBuff);
        } catch (Exception b) {
            b.printStackTrace();

        }

    }

    private void handleClose(SelectionKey key) {
        if (logger.isDebugEnabled()) {
            logger.debug("Handling key close operations: " + key);
        }

        try {

            cleanSocket();
        } finally {
            // linkDown();
            // connected = false;
            // synchronized (this.hdlcHandler) {
            synchronized (this.writeSelector) {
                // this is to ensure buffer does not have any bad data.
                // this.txBuffer.clear();
            }
        }
        return;
    }

    protected void cleanSocket() {
        this.stateProtocol.reset();
        this.connected = false;
        // this.txBuff.clear();
        // this.txBuff.limit(0);
        this.linkData.clear();
        if (this.socketChannel != null) {
            try {
                this.socketChannel.close();
                this.socketChannel = null;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        this.socketChannel = null;

        if (this.connectSelector != null) {
            try {
                this.connectSelector.close();
                this.connectSelector = null;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        if (this.readSelector != null) {
            try {
                this.readSelector.close();
                this.readSelector = null;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        if (this.writeSelector != null) {
            try {
                this.writeSelector.close();
                this.writeSelector = null;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }


    }

    // method called from StateProtocol as result of receiving status frame
    public void receivedState(LinkStatus ls) throws IOException {
        // this propably will have to be synced!
        // one big family!! LoL
        switch (ls) {
            case LinkUp:
                switch (state) {
                    case ACTIVATING:
                    case CONNECTED:
                        this.setState(StreamState.INSERVICE);
                        this.markedReady = true;
                        this.lastMarketRemote = true;//sync value, so if we set to fale it will be sent to other side!
                        //send ack?
                        this.stateProtocol.acknowledge(ls);
                        break;

                    default:
                        logger.error("Received: " + ls + " while in wrong state: " + this.state);
                        break;
                }
                break;
            case LinkDown:
                switch (state) {
                    case INSERVICE:
                    case ACTIVATING:

                        this.setState(StreamState.CONNECTED);
                        this.markedReady = false;
                        this.lastMarketRemote = false;//sync value, so if we set to fale it will be sent to other side!
                        //send ack?
                        this.stateProtocol.acknowledge(ls);
                        break;
                    default:
                        logger.error("Received: " + ls + " while in wrong state: " + this.state);
                        break;
                }
                break;
            case StateAck:
                switch (this.state) {
                    case ACTIVATING:
                        if (ls.getAcked() == LinkStatus.LinkUp) {
                            this.setState(StreamState.INSERVICE);
                            this.markedReady = true;
                        } else {
                            this.setState(StreamState.CONNECTED);
                            this.markedReady = false;
                        }
                }
                break;
            default:
                logger.warn("Received not defined call for link status: " + ls);
        }
    }

    protected void setState(StreamState state) {
        switch (this.state) {
            case CLOSED:
                switch (state) {
                    case OPEN:
                        this.state = StreamState.OPEN;
                        break;
                    default:
                        logger.warn("Wrong state transition, current: " + this.state + ", target: " + state);
                }
                break;
            case OPEN:
                switch (state) {
                    case CONNECTING:
                        this.state = StreamState.CONNECTING;
                        break;
                    case CLOSED:
                        this.state = StreamState.CLOSED;
                        break;
                    // FIXME: add stiff here to cleanup?
                    default:
                        logger.warn("Wrong state transition, current: " + this.state + ", target: " + state);
                }
                break;
            case CONNECTING:
                switch (state) {
                    case OPEN:
                        this.state = StreamState.OPEN;
                        break;

                    case CONNECTED:
                        this.state = StreamState.CONNECTED;
                        break;
                    case CLOSED:
                        this.state = StreamState.CLOSED;
                    // FIXME: add stiff here to cleanup?
                    default:
                        logger.warn("Wrong state transition, current: " + this.state + ", target: " + state);
                }
                break;
            case CONNECTED:
                switch (state) {
                    case ACTIVATING:
                        this.state = StreamState.ACTIVATING;
                        break;
                    case INSERVICE:
                        this.state = StreamState.INSERVICE;
                        break;
                    case OPEN:
                        this.state = StreamState.OPEN;
                        break;
                    case CLOSED:
                        this.state = StreamState.CLOSED;
                    // FIXME: add stiff here to cleanup?
                    default:
                        logger.warn("Wrong state transition, current: " + this.state + ", target: " + state);
                }
                break;
            case ACTIVATING:
                switch (state) {
                    case INSERVICE:
                        this.state = StreamState.INSERVICE;
                        break;

                    case CONNECTED:
                        this.state = StreamState.CONNECTED;
                        break;
                    // FIXME: add stiff here to cleanup?
                    case OPEN:
                        this.state = StreamState.OPEN;
                        break;
                    case CLOSED:
                        this.state = StreamState.CLOSED;
                    default:
                        logger.warn("Wrong state transition, current: " + this.state + ", target: " + state);
                }
                break;
            case INSERVICE:
                switch (state) {
                    case CONNECTED:
                        this.state = StreamState.CONNECTED;
                        break;

                    case OPEN:
                        this.state = StreamState.OPEN;
                        break;
                    case CLOSED:
                        this.state = StreamState.CLOSED;
                        break;
                    default:
                        logger.warn("Wrong state transition, current: " + this.state + ", target: " + state);
                }
                break;

        }
    }

    // different bodies for those methods?
	/*
     * (non-Javadoc)
     * 
     * @see org.mobicents.protocols.stream.api.Stream#isReadable()
     */
    public boolean isReadable() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mobicents.protocols.stream.api.Stream#isWriteable()
     */
    public boolean isWriteable() {
        // TODO Auto-generated method stub
        return false;
    }
}
