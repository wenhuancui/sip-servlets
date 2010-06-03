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
package org.mobicents.media.server.impl.resource.cnf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.mobicents.media.Buffer;
import org.mobicents.media.Format;
import org.mobicents.media.server.impl.AbstractSink;

/**
 *
 * @author Oleg Kulikov
 */
public class MixerInputStream extends AbstractSink {

    private List<Buffer> buffers = Collections.synchronizedList(new ArrayList());
    protected AudioMixer mixer;

    /** 
     * Creates new input stream.
     * 
     * @param mixer
     * @param jitter
     */
    public MixerInputStream(AudioMixer mixer) {
        super("MixerInputStream");
        this.mixer = mixer;
    }

    @Override
    public String getId() {
        return mixer.getId();
    }

    /**
     * Informs when input stream has enouph data for mixing.
     * 
     * @return return true when stream has enouph data for mixing.
     */
    public boolean isReady() {
        return !buffers.isEmpty();
    }

    /**
     * (Non Java-doc.)
     * 
     * @see org.mobicents.media.server.impl.AbstractSink#onMediaTransfer(org.mobicents.media.Buffer) 
     */
    public void onMediaTransfer(Buffer buffer) throws IOException {
        if (mixer == null) {
            return;
        }

        Buffer buff = new Buffer();
        buff.copy(buffer);

        if (buffers.size() > 10) {
            return;
        }
        buffers.add(buff);
    }

    /**
     * Reads media buffer from this stream with specified duration.
     * 
     * @param duration the duration of the requested buffer in milliseconds.
     * @return buffer which contains duration ms media for 8000Hz, 16bit, linear audio.
     */
    public byte[] read(int duration) {
        Buffer buffer = buffers.remove(0);
        return buffer.getFlags() == Buffer.FLAG_SILENCE ? new byte[320] : buffer.getData();
    }

    /**
     * (Non Java-doc.)
     * 
     * @see org.mobicents.media.MediaSink.isAcceptable(Format).
     */
    public boolean isAcceptable(Format fmt) {
        return fmt.matches(AudioMixer.LINEAR);
    }

    /**
     * (Non Java-doc.)
     * 
     * @see org.mobicents.media.MediaSink#getFormats() 
     */
    public Format[] getFormats() {
        return AudioMixer.formats;
    }

    @Override
    public String toString() {
        return mixer.toString();
    }
}
