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
package org.mobicents.media.server.impl.resource.ss7;

import java.io.IOException;
import java.util.List;
import org.apache.log4j.Logger;
import org.mobicents.protocols.ss7.mtp.MTP;
import org.mobicents.protocols.ss7.mtp.Mtp1;
import org.mobicents.protocols.ss7.mtp.MtpUser;
import org.mobicents.protocols.ss7.mtp.SelectorFactory;

/**
 *
 * @author kulikov
 */
public class M3UserAgent implements MtpUser {

    /** The name of the link set */
    private String name;
    
    /** Originated point code */
    private int opc;
    
    /** Destination point code */
    private int dpc;
    
    /** physical channels */
    private List<Mtp1> channels;
    
    private SelectorFactory selectorFactory;
        
    private MTP mtp;
    private Logger logger = Logger.getLogger(M3UserAgent.class);
    /**
     * Sets the name of the linkset.
     * 
     * @param name the alhanumeric name of the linkset.
     */
    public void setName(String name) {
        this.name = name;
    }
    
    public void setSelectorFactory(SelectorFactory selectorFactory) {
	this.selectorFactory = selectorFactory;
    }
    
    /**
     * Assigns originated point code 
     * 
     * @param opc the value of the originated point code in decimal format
     */
    public void setOpc(int opc) {
        this.opc = opc;
    }

    /**
     * Assigns destination point code.
     * 
     * @param dpc the destination point code value in decimal format.
     */
    public void setDpc(int dpc) {
        this.dpc = dpc;
    }

    /**
     * Assigns signalling channels.
     * 
     * @param channels the list of available physical channels.
     */
    public void setChannels(List<Mtp1> channels) {
        this.channels = channels;
    }
    
    public void linkUp() {
    }

    public void linkDown() {
    }

    
    public void receive(byte sls, byte linksetId, int service, int subservice, byte[] msgBuff) {
        System.out.println("Receive message");
    }

    public void start() throws IOException {
        mtp = new MTP();
        mtp.setName(name);
        mtp.setSelectorFactory(selectorFactory);
        mtp.setChannels(channels);
        mtp.setDpc(dpc);
        mtp.setOpc(opc);
        mtp.activate();
        logger.info("Started " + name);
    }
    
    public void stop() throws IOException {
        mtp.deactive();
    }

	public void receive(byte[] arg0) {
		// TODO Auto-generated method stub
		
	}
}
