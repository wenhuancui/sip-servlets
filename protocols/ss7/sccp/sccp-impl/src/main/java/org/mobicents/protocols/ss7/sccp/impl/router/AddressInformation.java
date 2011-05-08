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

package org.mobicents.protocols.ss7.sccp.impl.router;

import java.io.Serializable;

import org.mobicents.protocols.ss7.indicator.NatureOfAddress;
import org.mobicents.protocols.ss7.indicator.NumberingPlan;

/**
 * Defines information required for compare and constrcut SCCP address.
 * 
 * @author kulikov
 */
public class AddressInformation implements Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = -4270636264251165348L;
	private final static String SEPARATOR = "#";
    private final static String NULL = " ";
    
    /** translation type */
    private int tt;    
    /** numbering plan */
    private NumberingPlan np;
    /** nature of address */
    private NatureOfAddress noa;
    /** digits */
    private String digits;    
    /** subsytem number */
    private int ssn;
    
    /**
     * Creates new address information object.
     * 
     * @param tt translation type
     * @param np numbering plan
     * @param noa nature of address
     * @param digits global title mask
     * @param ssn subsytem number.
     */
    public AddressInformation(int tt, NumberingPlan np, NatureOfAddress noa, String digits, int ssn) {
        this.tt = tt;
        this.np = np;
        this.noa = noa;
        this.digits = digits;
        this.ssn = ssn;
    }
    
    public static AddressInformation getInstance(String s) {
        int l = 0;
        int r = s.indexOf('#', l + 1);
        
        String token = s.substring(l, r);
        
        //parse params
        int tt = token.equals(NULL) ? -1 : Integer.parseInt(token);
        
        l = r + 1;
        r = s.indexOf('#', l);
        
        token = s.substring(l, r);
        NumberingPlan np = token.equals(NULL) ? null : NumberingPlan.valueOf(token);

        l = r + 1;
        r = s.indexOf('#', l);
        
        token = s.substring(l, r);
        NatureOfAddress noa = token.equals(NULL) ? null : NatureOfAddress.valueOf(token);

        l = r + 1;
        r = s.indexOf('#', l);
        
        token = s.substring(l, r);        
        String digits = token.equals(NULL) ? null : token;
        
        l = r + 1;
        r = s.length();
        
        token = s.substring(l, r);        
        int ssn = (token.length() == 0 || token.equals(NULL)) ? -1 : Integer.parseInt(token);
        
        return new AddressInformation(tt, np, noa, digits, ssn);
    }
    
    /**
     * Gets the translation type.
     * 
     * @return translation type indcator.
     */
    public int getTranslationType() {
        return tt;
    }
    
    /**
     * Gets the numbering plan indicator.
     * 
     * @return numbering plan
     */
    public NumberingPlan getNumberingPlan() {
        return np;
    }
    
    /**
     * Gets the nature of address indicator.
     * 
     * @return the nature of address 
     */
    public NatureOfAddress getNatureOfAddress() {
        return noa;
    }
    
    /**
     * Global title mask
     * 
     * @return the global title mask
     */
    public String getDigits() {
        return digits;
    }
    
    /**
     * Gets the subsystem number
     * 
     * @return the subsystem number
     */
    public int getSubsystem() {
        return ssn;
    }
    
    @Override
    public String toString() {
        StringBuffer buff = new StringBuffer();
        if (tt != -1) {
            buff.append(tt);
        } else {
            buff.append(NULL);
        }
        
        buff.append(SEPARATOR);
        
        if (np != null) {
            buff.append(np.toString());
        } else {
            buff.append(NULL);
        }
        buff.append(SEPARATOR);

        if (noa != null) {
            buff.append(noa.toString());
        } else {
            buff.append(NULL);
        }
        buff.append(SEPARATOR);
        
        if (digits != null) {
            buff.append(digits);
        } else {
            buff.append(NULL);
        }
        buff.append(SEPARATOR);

        if (ssn != -1) {
            buff.append(ssn);
        } else {
            buff.append(NULL);
        }
        
        return buff.toString();
    }
}
