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

/**
 *
 * @author kulikov
 */
public abstract class AbstractSourceSet extends AbstractSource {

    private HashMap<String, AbstractSource> sources = new HashMap();
    
    public AbstractSourceSet(String name) {
        super(name);
    }
    
    public Collection<AbstractSource> getStreams() {
        return sources.values();
    }

    @Override
    public boolean isMultipleConnectionsAllowed() {
        return true;
    }
    
    @Override
    public void start() {
        Collection<AbstractSource> streams = sources.values();
/*        for (AbstractSource stream : streams) {
            if (stream.isConnected() && stream.getFormat() != null) {
                stream.start();
            }
        }
 */ 
    }
    
    @Override
    public void connect(MediaSink sink) {
        if (sink == null) {
            throw new IllegalArgumentException("Other party can not be nul");
        }
        AbstractSource source = createSource(sink);
        source.connect(sink);
        
//        source.start();
        sources.put(sink.getId(), source);
        
    }

    @Override
    public void disconnect(MediaSink otherParty) {
        AbstractSource source = sources.remove(otherParty.getId());
        if (source == null) {
            throw new IllegalArgumentException(otherParty + " is not connected to " + this);
        }
        source.stop();
        source.disconnect(otherParty);
    }

    public abstract AbstractSource createSource(MediaSink otherParty);
    public abstract void destroySource(AbstractSource source);
    
    public int getActiveSourceCount() {
        return sources.size();
    }
}
