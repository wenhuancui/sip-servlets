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

package org.mobicents.media.server.impl.resource.prelay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import org.mobicents.media.Buffer;
import org.mobicents.media.Format;
import org.mobicents.media.MediaSink;
import org.mobicents.media.MediaSource;
import org.mobicents.media.Server;
import org.mobicents.media.server.ConnectionImpl;
import org.mobicents.media.server.impl.AbstractSink;
import org.mobicents.media.server.impl.AbstractSinkSet;
import org.mobicents.media.server.impl.AbstractSource;
import org.mobicents.media.server.impl.AbstractSourceSet;
import org.mobicents.media.server.impl.BaseComponent;
import org.mobicents.media.server.impl.dsp.DspFactory;
import org.mobicents.media.server.spi.Endpoint;
import org.mobicents.media.server.spi.MediaType;
import org.mobicents.media.server.spi.ResourceGroup;

/**
 *
 * @author kulikov
 */
public class Bridge extends BaseComponent implements ResourceGroup {
    
    private AudioChannel[] audioChannels = new AudioChannel[2];
    private Input input;
    private Output output;

    
    private static ArrayList<MediaType> mediaTypes = new ArrayList();
    static {
        mediaTypes.add(MediaType.AUDIO);
    }
    
    public Bridge(String name, DspFactory dspFactory, Endpoint endpoint) {
        super(name);
        
        audioChannels[0] = new AudioChannel(dspFactory);
        audioChannels[1] = new AudioChannel(dspFactory);
        
        input = new Input("PacketRelay[Input]");
        output = new Output("PacketRelay[Output]");
    }

    public Collection<MediaType> getMediaTypes() {
        return mediaTypes;
    }
    
    public MediaSource getSource(MediaType media) {
        return media == MediaType.AUDIO ? output : null;
    }
    
    public MediaSink getSink(MediaType media) {
        return media == MediaType.AUDIO ? input : null;
    }

    public void start() {
//        processors[0].start();
//        processors[1].start();
    }

    public void stop() {
//        processors[0].stop();
//        processors[1].stop();
    }
    
    private class Input extends AbstractSinkSet {

        public Input(String name) {
            super(name);
        }
        
        @Override
        public void start() {
        }

        @Override
        public void stop() {
        }

        public Format[] getFormats() {
            return null;
        }

        public boolean isAcceptable(Format format) {
            return false;
        }

        @Override
        public void onMediaTransfer(Buffer buffer) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public AbstractSink createSink(MediaSource otherParty) {
            ConnectionImpl connection = (ConnectionImpl) otherParty.getConnection();
            int idx = connection.getIndex();
//            return (AbstractSink) processors[idx].getInput();
            return (AbstractSink) audioChannels[idx].getInput();
        }

        @Override
        public void destroySink(AbstractSink sink) {
        }

        
    }
    
    private class Output extends AbstractSourceSet {

        public Output(String name) {
            super(name);
        }
        

        @Override
        public void start() {
        }

        @Override
        public void stop() {
        }

        @Override
        public void evolve(Buffer buffer, long timestamp) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Format[] getFormats() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public AbstractSource createSource(MediaSink otherParty) {
            ConnectionImpl connection = (ConnectionImpl) otherParty.getConnection();
            int idx = Math.abs(connection.getIndex() - 1);
//            return (AbstractSource) processors[idx].getOutput();
            return (AbstractSource) audioChannels[idx].getOutput();
        }

        @Override
        public void destroySource(AbstractSource source) {
        }
        
    }

}
