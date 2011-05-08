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

/**
 * 
 */
package org.mobicents.protocols.stream.impl;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.mobicents.protocols.stream.api.SelectorKey;
import org.mobicents.protocols.stream.api.Stream;
import org.mobicents.protocols.stream.api.StreamSelector;

/**
 * Base class for stream impl.
 * @author baranowb
 *
 */
public abstract class AbstractStream implements Stream {

    protected List<StreamSelector> selectors = new LinkedList<StreamSelector>();

    public SelectorKey register(StreamSelector selector, int op) throws IOException {
        return ((StreamSelectorImpl)selector).register(this, op);
    }

    protected void doRegister(StreamSelector selector) {
        selectors.add(selector);
    }

    protected void doUnregister(StreamSelector selector) {
        selectors.remove(selector);
    }

    protected abstract void impSelectNow() throws IOException;

    protected void markOp(int op, AbstractStream abstractStream, StreamSelector sel) {
        ((StreamSelectorImpl) sel).markOp(op, abstractStream);

    }
}
