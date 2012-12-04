/*
 * Mobicents Media Gateway
 *
 * The source code contained in this file is in in the public domain.
 * It can be used in any project or product without prior permission,
 * license or royalty payments. There is  NO WARRANTY OF ANY KIND,
 * EXPRESS, IMPLIED OR STATUTORY, INCLUDING, WITHOUT LIMITATION,
 * THE IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE,
 * AND DATA ACCURACY.  We do not warrant or make any representations
 * regarding the use of the software or the  results thereof, including
 * but not limited to the correctness, accuracy, reliability or
 * usefulness of the software.
 */
package org.mobicents.media.server.impl.dsp;

import java.io.IOException;
import java.util.ArrayList;

import java.util.Collection;
import org.mobicents.media.Buffer;
import org.mobicents.media.Component;
import org.mobicents.media.Format;
import org.mobicents.media.MediaSink;
import org.mobicents.media.MediaSource;
import org.mobicents.media.server.impl.AbstractSink;
import org.mobicents.media.server.impl.AbstractSource;
import org.mobicents.media.server.impl.BaseComponent;
import org.mobicents.media.server.spi.SyncSource;
import org.mobicents.media.server.spi.dsp.Codec;
import org.mobicents.media.server.spi.dsp.SignalingProcessor;

/**
 * Implements DSP features.
 * 
 * Processor has input and output and is used to perform required 
 * transcoding if needed for packets followed from source to consumer. 
 * Processor is transparent for packets with format acceptable by consumer 
 * by default. 
 * 
 * @author Oleg Kulikov
 */
public class Processor extends BaseComponent implements SignalingProcessor {

    private Format[] inputFormats = new Format[0];
    private Input input;
    private Output output;
    private transient ArrayList<Codec> codecs = new ArrayList();
    private Codec codec;
    private Buffer buff;
    private long timestamp;

    public Processor(String name) {
        super(name);
        input = new Input(name);
        output = new Output(name);
        output.setSyncSource(input);
    }

    protected void add(Codec codec) {
        codecs.add(codec);
    }

    /**
     * Gets the input for original media
     * 
     * @return media handler for input media.
     */
    public MediaSink getInput() {
        return input;
    }

    /**
     * Gets the output stream with transcoded media.
     * 
     * @return media stream.
     */
    public MediaSource getOutput() {
        return output;
    }

    public void connect(MediaSource source) {
        input.connect(source);
    }

    public void disconnect(MediaSource source) {
        input.disconnect(source);
    }

    public void connect(MediaSink sink) {
        output.connect(sink);
    }

    public void disconnect(MediaSink sink) {
        output.disconnect(sink);
    }

    /**
     * (Non Java-doc).
     * 
     * @see org.mobicents.media.Component#start() 
     */
    public void start() {
        //start input and output channel
        if (!input.isStarted()) {
            input.start();
        }
    }

    /**
     * (Non Java-doc).
     * 
     * @see org.mobicents.media.Component#stop() 
     */
    public void stop() {
        if (input.isStarted()) {
            input.stop();
        }
    }

    /**
     * Implements input of the processor.
     */
    private class Input extends AbstractSink implements SyncSource {

        private Format fmt;
        private boolean isAcceptable;
        private volatile boolean started = false;

        public Input(String name) {
            super(name + ".input");
        }

        @Override
        public Format selectPreffered(Collection<Format> set) {
            if (set == null) {
                codec = null;
                return null;
            }
            
            //input and output should be connected to termine preffred format
            if (!output.isConnected()) {
                return null;
            }
            
            Format outFormat = output.getFormat();
            //the next case when output is connected but format is not selected yet
            //in this case format will be choosen later
            if (outFormat == null) {
                return null;
            }
            
            //possible that output format is presented in suggesed set
            for (Format f : set) {
                if (f.matches(outFormat)) {
                    return f;
                }
            }
            
            //transcoding required. we need select codec.
            codec = null;
            for (Codec c : codecs) {
                if (c.getSupportedOutputFormat().matches(outFormat)) {
                    for (Format f: set) {
                        if (c.getSupportedInputFormat().matches(f)) {
                            codec = c;
                            return f;
                        }
                    }
                }
            }

            return null;
        }
        
        @Override
        public boolean isStarted() {
            return this.started;
        }

        @Override
        public void start() {
            this.started = true;
            if (!output.isStarted()) {
                output.start();
            }
            super.start();
        }

        @Override
        public void stop() {
            this.started = false;
            if (output.isStarted()) {
                output.stop();
            }
            super.stop();
        }

        /**
         * (Non Java-doc.)
         * 
         * @see org.mobicents.media.MediaSink#isAcceptable(org.mobicents.media.Format) 
         */
        public boolean isAcceptable(Format format) {
            if (fmt != null && fmt.matches(format)) {
                return isAcceptable;
            }

            inputFormats = getFormats();

            fmt = format;
            for (Format f : inputFormats) {
                if (f.matches(format)) {
                    this.isAcceptable = true;
                    break;
                }
            }
            return this.isAcceptable;
        }

        /**
         * (Non Java-doc.)
         * 
         * @see org.mobicents.media.server.impl.AbstractSink#onMediaTransfer(org.mobicents.media.Buffer) 
         */
        public void onMediaTransfer(Buffer buffer) throws IOException {
            timestamp = buffer.getTimeStamp();
            output.transmit(buffer);
        }

        /**
         * Gets list of formats supported by connected other party
         * 
         * @return the array of format objects.
         */
        protected Format[] getOtherPartyFormats() {
            return otherParty != null ? otherParty.getFormats() : new Format[0];
        }

        /**
         * (Non Java-doc.)
         * 
         * @see org.mobicents.media.MediaSink#getFormats() 
         */
        public Format[] getFormats() {
            ArrayList<Format> list = new ArrayList();
            if (output.isConnected()) {
                Format[] original = output.getOtherPartyFormats();
                for (Format f : original) {
                    list.add(f);
                    for (Codec codec : codecs) {
                        if (codec.getSupportedOutputFormat().matches(f)) {
                            Format ff = codec.getSupportedInputFormat();
                            if (!list.contains(ff)) {
                                list.add(ff);
                            }
                        }
                    }
                }
            } else {
                for (Codec codec : codecs) {
                    Format ff = codec.getSupportedInputFormat();
                    if (!list.contains(ff)) {
                        list.add(ff);
                    }
                }
            }
            Format[] fmts = new Format[list.size()];
            list.toArray(fmts);

            return fmts;
        }


        @Override
        public String toString() {
            return "Processor.Input[" + getName() + "]";
        }

        public void sync(MediaSource mediaSource) {
        }

        public void unsync(MediaSource mediaSource) {
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    /**
     * Implements output of the processor.
     */
    private class Output extends AbstractSource {
        private volatile boolean started = false;

        /**
         * Creates new instance of processor's output.
         * 
         * @param name - the name of the processor;
         */
        public Output(String name) {
            super(name + ".output");
        }

        /**
         * Gets list of formats supported by connected other party
         * 
         * @return the array of format objects.
         */
        protected Format[] getOtherPartyFormats() {
            return otherParty != null ? otherParty.getFormats() : new Format[0];
        }

        @Override
        public boolean isStarted() {
            return this.started;
        }

        @Override
        public void start() {
            this.started = true;
            if (!input.isStarted()) {
                input.start();
            }
            super.start();
        }

        @Override
        public void stop() {
            this.started = false;
            if (input.isStarted()) {
                input.stop();
            }
            super.stop();
        }

        /**
         * Asks the sink connected to this channel to determine preffred format
         * 
         * @param set the set of format to choose preffred from
         * @return the selected preffred format
         */
        public Format getOtherPartyPreffered(Collection<Format> set) {
            return ((AbstractSink)otherParty).getPreffered(set);
        }
        
        @Override
        public void setPreffered(Format fmt) {
            super.setPreffered(fmt);
            
            //if input is connected we have to update its soource
            if (!input.isConnected()) {
                return;
            }
            
            //if selected format matches to one input format we have to chose it as preffred
            Format[] supported = input.getOtherPartyFormats();
            for (Format f : supported) {
                if (f.matches(fmt)) {
                    input.assignPreffered(fmt);
                    return;
                }
            }
            
            //at this point we have to select codec
            for (Codec c: codecs) {
                if (c.getSupportedOutputFormat().matches(fmt)) {
                    for (Format f : supported) {
                        if (f.matches(c.getSupportedInputFormat())) {
                            codec = c;
                            input.assignPreffered(f);
                            return;
                        }
                    }
                }
            }
            codec = null;
        }

        /**
         * (Non Java-doc.)
         * 
         * @see org.mobicents.media.MediaSource#getFormats() 
         */
        public Format[] getFormats() {
            ArrayList<Format> list = new ArrayList();
            if (input.isConnected()) {
                Format[] original = input.getOtherPartyFormats();
                for (Format f : original) {
                    list.add(f);
                    for (Codec codec : codecs) {
                        if (codec.getSupportedInputFormat().matches(f)) {
                            Format ff = codec.getSupportedOutputFormat();
                            if (!list.contains(ff)) {
                                list.add(ff);
                            }
                        }
                    }
                }
            } else {
                for (Codec codec : codecs) {
                    Format ff = codec.getSupportedOutputFormat();
                    if (!list.contains(ff)) {
                        list.add(ff);
                    }
                }
            }
            Format[] fmts = new Format[list.size()];
            list.toArray(fmts);

            return fmts;
        }

        /**
         * Transmits buffer to the output handler.
         * 
         * @param buffer the buffer to transmit
         */
        protected void transmit(Buffer buffer) {
            if (!started) {
                buffer.dispose();
                return;
            }
            //Here we work in ReceiveStream.run method, which runs in local ReceiveStreamTimer
            // Discard packet silently if output handler is not assigned yet
            if (otherParty == null) {
                buffer.dispose();
                return;
            }

            if (codec != null) {
                codec.process(buffer);
            }
            // Codec can delay media transition if it has not enouph media
            // to perform its job. 
            // It means that Processor should check FLAGS after codec's 
            // work and discard packet if required
            if (buffer.getFlags() == Buffer.FLAG_DISCARD) {
                buffer.dispose();
                return;
            }

            //may be a situation when original format can not be trancoded to 
            //one of the required output. In this case codec map will have no 
            //entry for this format. also codec may has no entry in case of when 
            //transcoding is not required. to differentiate these two cases check
            //if this format is acceptable by the consumer.

            //deliver packet to the consumer
            buff = buffer;
            run();
        }

        @Override
        public void evolve(Buffer buffer, long timestamp, long sequenceNumber) {
            buffer.copy(buff);
        }

        @Override
        public String toString() {
            return "Processor.Output[" + getName() + "]";
        }
    }
}
