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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mobicents.media.server.impl.resource.zap;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collection;
import org.mobicents.media.Buffer;
import org.mobicents.media.Format;
import org.mobicents.media.MediaSink;
import org.mobicents.media.MediaSource;
import org.mobicents.media.server.impl.AbstractSink;
import org.mobicents.media.server.impl.AbstractSource;
import org.mobicents.media.server.impl.BaseComponent;
import org.mobicents.media.server.spi.MediaType;
import org.mobicents.media.server.spi.ResourceGroup;
import org.mobicents.media.server.spi.rtp.AVProfile;

/**
 *
 * @author kulikov
 */
public class VoiceChannel extends BaseComponent implements ResourceGroup {

    private RandomAccessFile channel;
    
    private VoiceSource source;
    private VoiceSink sink;
    
    private Format[] formats = new Format[]{AVProfile.PCMA};
    
    private static ArrayList<MediaType> mediaTypes = new ArrayList();
    static {
        mediaTypes.add(MediaType.AUDIO);
    }
    
    public VoiceChannel(String path, int cic) {
        super(path);
        
        source = new VoiceSource(path);
        sink = new VoiceSink(path);
    }
    
    public Collection<MediaType> getMediaTypes() {
        return mediaTypes;
    }

    public MediaSink getSink(MediaType media) {
        return sink;
    }

    public MediaSource getSource(MediaType media) {
        return source;
    }

    private class VoiceSource extends AbstractSource {
        
        public VoiceSource(String name) {
            super(name);
        }

        @Override
        public void evolve(Buffer buffer, long timestamp) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Format[] getFormats() {
            return formats;
        }
        
    }
    
    private class VoiceSink extends AbstractSink {
        
        public VoiceSink(String name) {
            super(name);
        }

        @Override
        public void onMediaTransfer(Buffer buffer) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Format[] getFormats() {
            return formats;
        }
        
    }

}
