/*
 * Mobicents, Communications Middleware
 * 
 * Copyright (c) 2008, Red Hat Middleware LLC or third-party
 * contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Middleware LLC.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 *
 * Boston, MA  02110-1301  USA
 */
package org.mobicents.media.server.impl.resource.echo;

import java.util.ArrayList;
import java.util.Collection;
import org.mobicents.media.MediaSink;
import org.mobicents.media.MediaSource;
import org.mobicents.media.server.impl.BaseComponent;
import org.mobicents.media.server.impl.resource.Proxy;
import org.mobicents.media.server.spi.ResourceGroup;

/**
 *
 * @author kulikov
 */
public class Echo extends BaseComponent implements ResourceGroup {

    private Proxy audioProxy;
    private Proxy videoProxy;
    private static ArrayList<String> mediaTypes = new ArrayList();
    static {
        mediaTypes.add("audio");
        mediaTypes.add("video");
    }
    
    public Echo(String name) {
        super(name);
        audioProxy = new Proxy(name + ".proxy.audio");
        videoProxy = new Proxy(name + ".proxy.video");
    }
    
    public Collection<String> getMediaTypes() {
        return mediaTypes;
    }
    
    public MediaSink getSink(String media) {
        return media.equals("audio") ? audioProxy.getInput() : videoProxy.getInput();
    }

    public MediaSource getSource(String media) {
        return media.equals("audio") ? audioProxy.getOutput() : videoProxy.getOutput();
    }


    public void start() {
        audioProxy.start();
        videoProxy.start();
    }

    public void stop() {
        audioProxy.stop();
        videoProxy.stop();
    }

}
