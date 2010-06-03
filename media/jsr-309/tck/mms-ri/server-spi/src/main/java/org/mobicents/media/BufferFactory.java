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
