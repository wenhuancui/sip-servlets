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

package org.mobicents.media.server.impl.resource.prelay;

import org.mobicents.media.MediaSink;
import org.mobicents.media.MediaSource;
import org.mobicents.media.server.impl.dsp.DspFactory;
import org.mobicents.media.server.impl.dsp.Processor;
import org.mobicents.media.server.impl.resource.cnf.AudioMixer;

/**
 *
 * @author kulikov
 */
public class AudioChannel {
    private Processor p1;
    private Processor p2;
    private AudioMixer mixer;
    
    public AudioChannel(DspFactory dspFactory) {
        p1 = (Processor) dspFactory.newInstance(null);
        p2 = (Processor) dspFactory.newInstance(null);
        mixer = new AudioMixer("PacketRelay");
        
        p1.getOutput().connect(mixer);
        p2.getInput().connect(mixer.getOutput());
    }
    
    public MediaSink getInput() {
        return p1.getInput();
    }
    
    public MediaSource getOutput() {
        return p2.getOutput();
    }
    
    public void connect(MediaSource source) {
        source.connect(p1.getInput());
    }
    
    public void disconnect(MediaSource source) {
        source.disconnect(p1.getInput());
    }
    
    public void connect(MediaSink sink) {
        p2.getOutput().connect(sink);
    }
    
    public void disconnect(MediaSink sink) {
        p2.getOutput().disconnect(sink);
    }
    
    public void stop() {
        mixer.stop();
    }
}
