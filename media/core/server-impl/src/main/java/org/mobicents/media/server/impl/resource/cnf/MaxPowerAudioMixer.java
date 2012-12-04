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

package org.mobicents.media.server.impl.resource.cnf;

/**
 * Approximate loudest speaker mixer, all other speakers are removed
 * 
 * @author Oleg Kulikov
 * @author Vladimir Ralev
 */
public class MaxPowerAudioMixer extends AudioMixer {

	private int lastChannel = 0;
	private static int takeoverRatio = 2; // Change channel only if the new signal is 2x stronger
	
    /**
     * Creates a new instance of AudioMixer.
     * 
     * @param packetPeriod
     *            packetization period in milliseconds.
     * @param fmt
     *            format of the output stream.
     */
    public MaxPowerAudioMixer(String name) {
        super(name);
    }
    
    @Override
    public byte[] mix(byte[][] input) {
        int numChannels = input.length;
        short[][] inputs = new short[input.length][];

        long[] powerPerChannel = new long[numChannels];
        long[] sumPerChannel = new long[numChannels];
        
        for (int q = 0; q < numChannels; q++) {
            inputs[q] = byteToShortArray(input[q]);
            for(int w = 0; w < inputs[q].length; w++) {
            	powerPerChannel[q] += Math.abs(inputs[q][w]);
            	sumPerChannel[q] += inputs[q][w];
            	// power should be sqrt(a*a) but want to keep it fixed point
            }
        }
        
        int maxPowerChannel = 0;
        long maxPower = 0;
        
        for(int q = 0; q < numChannels; q++) {
        	powerPerChannel[q] -= sumPerChannel[q];
        }
        
        for(int q = 0; q < numChannels; q++) {
        	if(powerPerChannel[q]>maxPower) {
        		// we should also subtract the mean instead of sum
        		maxPowerChannel = q;
        		maxPower = powerPerChannel[q];
        	}
        }
        
        if(maxPower/takeoverRatio<powerPerChannel[lastChannel]) {
        	maxPowerChannel = lastChannel; // new speaker only takes over if he has a lot more power
        }

        return input[maxPowerChannel];
    }
}
