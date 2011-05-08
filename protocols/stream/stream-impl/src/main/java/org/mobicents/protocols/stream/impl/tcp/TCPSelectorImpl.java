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

package org.mobicents.protocols.stream.impl.tcp;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.mobicents.protocols.stream.api.SelectorKey;
import org.mobicents.protocols.stream.api.Stream;
import org.mobicents.protocols.stream.impl.SelectorKeyImpl;
import org.mobicents.protocols.stream.impl.StreamSelectorImpl;

/**
 *
 * @author kulikov
 */
public class TCPSelectorImpl extends StreamSelectorImpl {

    private Selector selector;
    private ArrayList<SelectorKey> selection = new ArrayList();
    
    public TCPSelectorImpl() throws IOException {
        selector = Selector.open();
    }
    
    @Override
    public SelectorKey register(Stream s, int ops) throws IOException {
        //deligate call to the actual NIO selector
        SelectionKey key = ((AbstractTCPStream)s).channel.register(selector, ops);
        
        //attach stream
        key.attach(s);
        
        //return selection key
        return new TCPSelectorKey(this, key, s);
    }
    /* (non-Javadoc)
     * @see org.mobicents.protocols.stream.api.StreamSelector#selectNow()
     */
    @Override
    public Collection<SelectorKey> selectNow() throws IOException {
        selection.clear();
        selector.selectNow();
        Iterator<SelectionKey> i = selector.selectedKeys().iterator();
        while (i.hasNext()) {
            SelectionKey key = i.next();
            
            selection.add(new SelectorKeyImpl(this,(Stream) key.attachment()));
            i.remove();
        }
        return  selection;
    }
}
