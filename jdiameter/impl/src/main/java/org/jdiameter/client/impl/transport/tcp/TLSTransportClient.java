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

package org.jdiameter.client.impl.transport.tcp;

import org.jdiameter.client.api.io.NotInitializedException;
import static org.jdiameter.client.impl.helpers.Parameters.*;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;

/*
 * Copyright (c) 2006 jDiameter.
 * https://jdiameter.dev.java.net/
 *
 * License: GPL v3
 *
 * e-mail: erick.svenson@yahoo.com
 *
 */
class TLSTransportClient extends TCPTransportClient {
    
    private TLSClientConnection parentConnection;

    /**
     * Default constructor
     *
     * @param parenConnection connection created this transport
     */
    TLSTransportClient(TLSClientConnection parenConnection) {
        this.parentConnection = parenConnection;
    }

    public void initialize() throws IOException, NotInitializedException {
        if (destAddress == null)
            throw new NotInitializedException("Destination address is not set");

        SSLSocketFactory cltFct = parentConnection.getSSLFactory();
        SSLSocket sck = (SSLSocket) cltFct.createSocket(destAddress.getAddress(), destAddress.getPort()); 
        sck.setEnableSessionCreation(parentConnection.getSSLConfig().getBooleanValue(SDEnableSessionCreation.ordinal(), true));
        sck.setUseClientMode(!parentConnection.getSSLConfig().getBooleanValue(SDUseClientMode.ordinal(), true));
        if (parentConnection.getSSLConfig().getStringValue(CipherSuites.ordinal(), "") != null) {
            sck.setEnabledCipherSuites(parentConnection.getSSLConfig().getStringValue(CipherSuites.ordinal(), "").split(","));
        }

        socketChannel = sck.getChannel();
        socketChannel.connect(destAddress);
        socketChannel.configureBlocking(true);
        parentConnection.onConnected();
    }

    public TCPClientConnection getParent() {
        return parentConnection;
    }    

}