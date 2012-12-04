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
package org.mobicents.media.server.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.jboss.util.id.UID;
import org.mobicents.media.Component;
import org.mobicents.media.Format;
import org.mobicents.media.server.ConnectionImpl;
import org.mobicents.media.server.spi.Connection;
import org.mobicents.media.server.spi.Endpoint;
import org.mobicents.media.server.spi.NotificationListener;
import org.mobicents.media.server.spi.events.NotifyEvent;

/**
 *
 * @author kulikov
 */
public abstract class BaseComponent implements Component {

    private String id = null;
    private String name = null;
    private int resourceType;
    
    private Endpoint endpoint;
    private Connection connection;
    
    protected Format format;    
    private List<NotificationListener> listeners = new CopyOnWriteArrayList();

    public BaseComponent(String name) {
        this.id = (new UID()).toString();
        this.name = name;
    }
    
    public Format getFormat() {
        return format;
    }
    
    public int getResourceType() {
        return resourceType;
    }
    
    public void setResourceType(int resourceType) {
        this.resourceType = resourceType;
    }
    
    public String getId() {
        return this.id;
    }

    public String getName() {
        return name;
    }
    
    public Endpoint getEndpoint() {
        return this.endpoint;
    }
    
    public void setEndpoint(Endpoint endpoint) {
        this.endpoint = endpoint;
    }
    
    public Connection getConnection() {
        return connection;
    }
    
    public void setConnection(Connection connection) {
        //diable all listeners if this component was associated with connection
        //and it is disconnected.
        if (this.connection != null && connection == null) {
            listeners.clear();
        }
        this.connection = connection;
    }
    
    protected void sendEvent(NotifyEvent evt) {
        for (NotificationListener listener : listeners) {
            try {
                listener.update(evt);
            } catch (Exception e) {
            }
        }
    }
    
    public void addListener(NotificationListener listener) {
        listeners.add(listener);
    }

    public void removeListener(NotificationListener listener) {
        listeners.remove(listener);
    }
    
    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        
        buffer.append(getName());
        buffer.append(" (endpoint=");
        
        if (getEndpoint() != null) {
            buffer.append(getEndpoint().getLocalName());
        } else {
            buffer.append("unknown");
        }
                
        if (getConnection() != null) {
            buffer.append(", connection=");
            buffer.append(((ConnectionImpl)getConnection()).getIndex());
        } 
        buffer.append(")");
        
        return buffer.toString();
    }
    
    public void resetStats() {
    }
    
    protected Collection<Format> subset(Format[] set1, Format[] set2) {
        ArrayList<Format> subset = new ArrayList();
        for (int i = 0; i < set1.length; i++) {
                for (int j = 0; j < set2.length; j++) {
                    if (set1[i].matches(set2[j])) {
                        String e1 = set1[i].getEncoding();
                        String e2 = set2[j].getEncoding();
                                                
                        if (!e1.equalsIgnoreCase("ANY")) {
                            subset.add(set1[i]);
                            continue;
                        }
                        
                        if (!e2.equalsIgnoreCase("ANY")) {
                            subset.add(set2[j]);
                            continue;
                        }
                        
                        if (e1.equalsIgnoreCase("ANY") && e2.equalsIgnoreCase("ANY") && !subset.contains(Format.ANY)) {
                            subset.add(set2[j]);
                            continue;
                        }
                    }
                }                
        }
        return subset;        
    }
    
}
