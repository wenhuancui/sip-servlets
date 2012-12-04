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

package org.mobicents.media.server.impl.rtp.clock;

import org.mobicents.media.Format;
import org.mobicents.media.format.AudioFormat;
import org.mobicents.media.server.impl.rtp.RtpClock;

/**
 *
 * @author kulikov
 */
public class AudioClock extends RtpClock {

    private double sampleRate;
    
    @Override
    public void setFormat(Format format) {
        super.setFormat(format);
        AudioFormat fmt = (AudioFormat) format;
        this.sampleRate = 1000/fmt.getSampleRate();
    }
    
    @Override
    public long getTime(long timestamp) {
        return (long) (sampleRate * timestamp);
    }

    @Override
    public long getTimestamp(long time) {
        return (long) (time/sampleRate);
    }

}
