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
package org.mobicents.media.server.impl.resource.prelay;

import org.mobicents.media.server.impl.resource.*;
import java.io.IOException;
import org.mobicents.media.Buffer;
import org.mobicents.media.Format;
import org.mobicents.media.Inlet;
import org.mobicents.media.MediaSink;
import org.mobicents.media.MediaSource;
import org.mobicents.media.Outlet;
import org.mobicents.media.server.impl.AbstractSink;
import org.mobicents.media.server.impl.AbstractSource;
import org.mobicents.media.server.impl.BaseComponent;
import org.mobicents.media.server.spi.SyncSource;
import org.mobicents.media.server.spi.clock.Task;
import org.mobicents.media.server.spi.clock.TimerTask;

/**
 *
 * @author kulikov
 */
public class PacketRelay extends BaseComponent implements Inlet, Outlet {

    private final static Format[] FORMATS = new Format[]{Format.ANY};
    
    private Input input;
    private Output output;
    
    private Buffer buff;
    private long timestamp;
    
    public PacketRelay(String name) {
        super(name);
        input = new Input(name);
        output = new Output(name);
        output.setSyncSource(input);
    }
    
    public void start() {
        input.start();
        output.start();
    }

    public void stop() {
        input.stop();
        output.stop();
    }

    public MediaSink getInput() {
        return input;
    }

    public MediaSource getOutput() {
        return output;
    }

    public void connect(MediaSource source) {
        input.connect(source);
    }

    public void disconnect(MediaSource source) {
        input.disconnect(source);
    }


    public void connect(MediaSink sink) {
        output.connect(sink);
    }

    public void disconnect(MediaSink sink) {
        output.disconnect(sink);
    }

    private class Input extends AbstractSink implements SyncSource {
        
        public Input(String name) {
            super(name + "[input]");
        }
        
        @Override
        public void onMediaTransfer(Buffer buffer) throws IOException {
            buff = buffer;
            timestamp = buffer.getTimeStamp();
            output.perform();
        }

        public Format[] getFormats() {            
            return output.isConnected() ? output.getOtherPartyFormats() : FORMATS;
        }
        
        public Format[] getOtherPartyFormats() {
            return otherParty != null ? otherParty.getFormats() : new Format[0];
        }

        public void sync(MediaSource mediaSource) {
        }

        public void unsync(MediaSource mediaSource) {
        }

        public long getTimestamp() {
            return timestamp;
        }

        public TimerTask sync(Task task) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
        
    private class Output extends AbstractSource {

        public Output(String name) {
            super(name + "[output]");
        }
        
        @Override
        public void start() {
        }
        
        @Override
        public void stop() {
        }
        
        @Override
        public void evolve(Buffer buffer, long timestamp) {
            buffer.copy(buff);
        }

        public Format[] getOtherPartyFormats() {
            return otherParty != null ? otherParty.getFormats() : new Format[0];
        }
        
        public Format[] getFormats() {
            return input.isConnected() ? input.getOtherPartyFormats() : new Format[0];
        }
     
    }
}
