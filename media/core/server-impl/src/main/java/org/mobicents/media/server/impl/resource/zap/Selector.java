/*
 * JBoss, Home of Professional Open Source
 * Copyright XXXX, Red Hat Middleware LLC, and individual contributors as indicated
 * by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a full listing
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 */
package org.mobicents.media.server.impl.resource.zap;

import java.io.File;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Map;

import org.mobicents.protocols.ss7.mtp.ChannelSelector;
import org.mobicents.protocols.ss7.mtp.Mtp1;

import org.apache.log4j.Logger;

public class Selector implements ChannelSelector {

    private final static String MMS_HOME = "MMS_HOME";
    private final static String LIB_NAME = "zap-native-linux.so";
    
    public final static int READ = 0x01;
    public final static int WRITE = 0x02;
    
    /** array of selected file descriptors */
    private int fds[] = new int[16];
    
    /** array of registered channels */
    private ArrayList<Mtp1> registered = new ArrayList();    
    
    /** array of selected channels */
    private ArrayList<Mtp1> selected = new ArrayList();
    private static Logger logger = Logger.getLogger(ChannelSelector.class);
    
    static {
	try {
	    Map<String, String> env = System.getenv();
	    if (env.get(MMS_HOME) != null) {
		String path = env.get(MMS_HOME) + File.separator + "native" + File.separator + LIB_NAME;
		System.load(path);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
    
    /**
     * Register channel with this selector.
     *
     * @param channel the channel to register.
     */ 
    public void register(Mtp1 channel) {
	//add channel instance to the collection
	registered.add(channel);
	//perform actual registration
	logger.info("Registering file descriptor:" + ((Channel) channel).fd);
 	doRegister(((Channel)channel).fd);
	
    }
    
    /**
     * Unregister channel.
     *
     * @param channel the channel to unregister.
     */
    public void unregister(Mtp1 channel) {
	registered.remove(channel);
	doUnregister(((Channel)channel).fd);
    }
    
    /**
     * Selects channels wich are ready for specified IO operations.
     *
     * @param key selection key
     * @param timeout the time out for select.
     * @return the list of channel ready for input or output.
     */
    public Collection<Mtp1> select(int key, int timeout) {
	int count = doPoll(fds, key, timeout);
	selected.clear();
	for (int i = 0; i < count; i++) {
	    for (Mtp1 chan : registered) {
		Channel channel = (Channel) chan;
		if (channel.fd == fds[i]) {
		    selected.add(chan);
		}
	    }
	} 
	return selected;
    }
    
    /**
     * Registers pipe for polling.
     *
     *@param fd the file descriptor.
     */
    public native void doRegister(int fd);
    
    /**
     * Unregisters pipe from polling.
     *
     * @param fd the file descriptor.
     */ 
    public native void doUnregister(int fd);
    
    /**
     * Delegates select call to unix poll function.
     *
     * @param fds the list of file descriptors.
     * @param key selection key.
     * @return the number of selected channels.
     */ 
    public native int doPoll(int[] fds, int key, int timeout);
}