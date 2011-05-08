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

package org.jdiameter.common.impl.controller;

import org.jdiameter.api.Peer;
import org.jdiameter.api.URI;
import org.jdiameter.client.impl.helpers.UIDGenerator;
import org.jdiameter.common.api.statistic.IStatistic;
import org.jdiameter.common.api.statistic.IStatisticFactory;
import org.jdiameter.common.api.statistic.IStatisticRecord;

public class AbstractPeer implements Comparable<Peer> {

  public static final int INT_COMMON_APP_ID = 0xffffffff;
  protected static UIDGenerator uid = new UIDGenerator();
  // Statistic
  protected IStatistic statistic;
  protected URI uri;

  public AbstractPeer(URI uri, IStatisticFactory statisticFactory) {
    this.uri = uri;

    IStatisticRecord appGenRequestCounter = statisticFactory.newCounterRecord(IStatistic.Counters.AppGenRequest);
    IStatisticRecord appGenCPSRequestCounter = statisticFactory.newPerSecondCounterRecord(
        IStatistic.Counters.AppGenRequestPerSecond, appGenRequestCounter);
    IStatisticRecord appGenRejectedRequestCounter = statisticFactory.newCounterRecord(IStatistic.Counters.AppGenRejectedRequest);

    IStatisticRecord appGenResponseCounter = statisticFactory.newCounterRecord(IStatistic.Counters.AppGenResponse);
    IStatisticRecord appGenCPSResponseCounter = statisticFactory.newPerSecondCounterRecord(
        IStatistic.Counters.AppGenResponsePerSecond, appGenResponseCounter);
    IStatisticRecord appGenRejectedResponseCounter = statisticFactory.newCounterRecord(IStatistic.Counters.AppGenRejectedResponse);

    IStatisticRecord netGenRequestCounter = statisticFactory.newCounterRecord(IStatistic.Counters.NetGenRequest);
    IStatisticRecord netGenCPSRequestCounter = statisticFactory.newPerSecondCounterRecord(
        IStatistic.Counters.NetGenRequestPerSecond, netGenRequestCounter);
    IStatisticRecord netGenRejectedRequestCounter = statisticFactory.newCounterRecord(IStatistic.Counters.NetGenRejectedRequest);

    IStatisticRecord netGenResponseCounter = statisticFactory.newCounterRecord(IStatistic.Counters.NetGenResponse);
    IStatisticRecord netGenCPSResponseCounter = statisticFactory.newPerSecondCounterRecord(
        IStatistic.Counters.NetGenResponsePerSecond, netGenResponseCounter);
    IStatisticRecord netGenRejectedResponseCounter = statisticFactory.newCounterRecord(IStatistic.Counters.NetGenRejectedResponse);

    IStatisticRecord sysGenResponseCounter = statisticFactory.newCounterRecord(IStatistic.Counters.SysGenResponse);

    this.statistic = statisticFactory.newStatistic(IStatistic.Groups.Peer,
        appGenRequestCounter, appGenCPSRequestCounter, appGenRejectedRequestCounter,
        appGenResponseCounter, appGenCPSResponseCounter, appGenRejectedResponseCounter,
        netGenRequestCounter, netGenCPSRequestCounter, netGenRejectedRequestCounter,
        netGenResponseCounter, netGenCPSResponseCounter, netGenRejectedResponseCounter,
        sysGenResponseCounter
    );
  }

  public int compareTo(Peer o) {
    return uri.compareTo(o.getUri());
  }
}
