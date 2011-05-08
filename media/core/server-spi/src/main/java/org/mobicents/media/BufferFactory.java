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

package org.mobicents.media;

import java.util.ArrayList;

public class BufferFactory {

    private int BUFF_SIZE = 8192;
    private ArrayList<Buffer> list = new ArrayList<Buffer>();
    private int size;

    public BufferFactory(int size) {
        this.size = size;
        init();
    }    
    
    public BufferFactory(int size, int buffSize) {
        this.size = size;
        this.BUFF_SIZE = buffSize;
        init();
    }

    private void init() {
        for (int i = 0; i < size; i++) {
            Buffer buffer = new Buffer();
            buffer.setFactory(this);
            list.add(buffer);
        }
    }
    public Buffer allocate() {
        Buffer buffer = null;
        if (!list.isEmpty()) {
            buffer = list.remove(0);
        }

        if (buffer != null) {
            return buffer;
        }

        buffer = new Buffer();
        buffer.setFactory(this);

        return buffer;
    }

    public void deallocate(Buffer buffer) {
        if (list.size() < size && buffer != null ) {
            buffer.setDiscard(false);
            buffer.setHeader(null);
            list.add(buffer);
        }
    }
}
