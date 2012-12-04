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

package org.mobicents.media.server.impl;

import java.util.Collection;
import java.util.HashMap;
import org.mobicents.media.MediaSink;
import org.mobicents.media.MediaSource;

/**
 *
 * @author kulikov
 */
public abstract class AbstractSinkSet extends AbstractSink implements MediaSink {

    private HashMap<String, AbstractSink> sinks = new HashMap();
    
    public AbstractSinkSet(String name) {
        super(name);
    }
    
    public Collection<AbstractSink> getStreams() {
        return sinks.values();
    }
    
    @Override
    public boolean isMultipleConnectionsAllowed() {
        return true;
    }
    
    @Override
    public void connect(MediaSource source) {
        if (!source.isMultipleConnectionsAllowed()) {
            AbstractSink sink = createSink(source);
            sink.setEndpoint(getEndpoint());
            sink.setConnection(getConnection());
            sink.connect(source);
        
//            sink.start();
            sinks.put(source.getId(), sink);
        } else {
            throw new IllegalArgumentException(source + 
                    " allows muliple connection and this component also");
        }
    }
    
    @Override
    public void disconnect(MediaSource source) {
        AbstractSink sink = sinks.remove(source.getId());
        if (sink == null) {
            throw new IllegalArgumentException(source + " is not connected to " + this);
        }
        sink.stop();
        sink.disconnect(source);
        destroySink(sink);
        sink.setEndpoint(null);
        sink.setConnection(null);
    }

    
    public int getActiveSinkCount() {
        return sinks.size();
    }

    public abstract AbstractSink createSink(MediaSource otherParty);
    public abstract void destroySink(AbstractSink sink);
}
