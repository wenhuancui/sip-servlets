/*
 * JBoss, Home of Professional Open Source
 * Copyright XXXX, Red Hat Middleware LLC, and individual contributors as indicated
 * by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a full listing
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 */
package org.mobicents.media.server.impl.rtp;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import org.apache.log4j.Logger;
import org.mobicents.media.server.spi.clock.Task;
import org.mobicents.media.server.spi.clock.TimerTask;

/**
 *
 * @author kulikov
 */
public class Receiver implements Task {

    private Selector selector;
    private int bufferSize = 8196;
    private ByteBuffer readBuffer = ByteBuffer.allocateDirect(bufferSize);
    private volatile boolean started;
    private RtpFactory factory;
    private Logger logger = Logger.getLogger(Receiver.class);

    private TimerTask worker;
    
    public Receiver(RtpFactory factory) throws IOException {
        this.factory = factory;
        this.selector = SelectorProvider.provider().openSelector();
    }

    public Selector getSelector() {
        return selector;
    }

    public void start() {
        started = true;
        worker = factory.getTimer().sync(this);
    }

    public void stop() {
        worker.cancel();
        started = false;
        try {
            selector.close();
        } catch (IOException e) {
        }
    }

    
    public void cancel() {
        stop();
    }

    public boolean isActive() {
        return started;
    }

    public int perform() {
        try {
            selector.selectNow();   
            Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
            while (keys.hasNext()) {
                SelectionKey key = keys.next();
                keys.remove();
                DatagramChannel channel = (DatagramChannel) key.channel();
                RtpSocket socket = (RtpSocket) key.attachment();
                if (key.isReadable()) {
                    int len = 1;
                    while (len > 0 && !socket.isClosed()) {
                        len = channel.read(readBuffer);
                        readBuffer.flip();
                        if (len > 0) {
                            RtpPacket rtpPacket = new RtpPacket(readBuffer);
                            socket.receive(rtpPacket);
                        }
                        readBuffer.clear();
                    }
                }
            }
            factory.register();
        } catch (IOException e) {
//            e.printStackTrace();
        } finally {
            return 20;
        }
    }
    
    private String convert(String msg) {
        try {
            byte[] data = msg.getBytes("Cp1251");
            return new String(data, "Cp866");
        } catch (Exception e) {
            return null;
        }
    }
}
