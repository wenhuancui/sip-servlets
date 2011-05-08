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

package org.jdiameter.server.impl.io;

import org.jdiameter.api.Configuration;
import org.jdiameter.client.api.io.TransportError;
import org.jdiameter.client.api.io.TransportException;
import org.jdiameter.client.api.parser.IMessageParser;
import org.jdiameter.common.api.concurrent.IConcurrentFactory;
import org.jdiameter.server.api.io.INetworkConnectionListener;
import org.jdiameter.server.api.io.INetworkGuard;
import org.jdiameter.server.api.io.ITransportLayerFactory;
import org.jdiameter.server.impl.io.tcp.NetworkGuard;

import java.net.InetAddress;

public class TransportLayerFactory extends org.jdiameter.client.impl.transport.TransportLayerFactory implements ITransportLayerFactory {

  private IConcurrentFactory concurrentFactory;

  public TransportLayerFactory(Configuration conf, IConcurrentFactory concurrentFactory, IMessageParser parser) throws TransportException {
    super(conf, parser);
    this.concurrentFactory = concurrentFactory;
  }

  public INetworkGuard createNetworkGuard(InetAddress inetAddress, int port) throws TransportException {
    INetworkGuard guard;
    try {
      guard = new NetworkGuard(inetAddress, port, concurrentFactory, parser);
    }
    catch (Exception e) {
      throw new TransportException(TransportError.NetWorkError, e);
    }
    return guard;
  }

  public INetworkGuard createNetworkGuard(InetAddress inetAddress, final int port, final INetworkConnectionListener listener) throws TransportException {
    INetworkGuard guard;
    try {
      guard = new NetworkGuard(inetAddress, port, concurrentFactory, parser);
    }
    catch (Exception e) {
      throw new TransportException(TransportError.NetWorkError, e);
    }
    guard.addListener(listener);
    return guard;
  }
}
