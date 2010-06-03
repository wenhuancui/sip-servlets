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
 */package org.mobicents.media.server;

import java.util.Map;
import org.mobicents.media.server.resource.ChannelFactory;

/**
 *
 * @author kulikov
 */
public class ConnectionFactory {
    private Map<String, ChannelFactory> rxChannelFactory;
    private Map<String, ChannelFactory> txChannelFactory;

    public Map<String, ChannelFactory> getRxChannelFactory() {
        return rxChannelFactory;
    }

    public void setRxChannelFactory(Map<String, ChannelFactory> rxChannelFactory) {
        this.rxChannelFactory = rxChannelFactory;
    }
    
    public Map<String, ChannelFactory> getTxChannelFactory() {
        return txChannelFactory;
    }

    public void setTxChannelFactory(Map<String, ChannelFactory> txChannelFactory) {
        this.txChannelFactory = txChannelFactory;
    }
    
}
