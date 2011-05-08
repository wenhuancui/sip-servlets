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

package org.mobicents.media.server.spi.events;

import java.io.Serializable;
import org.mobicents.media.Component;

/**
 *
 * @author Oleg Kulikov
 */
public interface NotifyEvent extends Serializable {
    public final static int STARTED = 10000;
    public final static int COMPLETED = 20000;
    public final static int STOPPED = 30000;

    public final static int START_FAILED = 10001;
    public final static int TX_FAILED = 10002;
    public final static int RX_FAILED = 10003;
    
    public Component getSource();
    public int getEventID();
}
