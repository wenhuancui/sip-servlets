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

package org.jdiameter.common.impl.concurrent;

import org.jdiameter.common.api.concurrent.IConcurrentEntityFactory;
import org.jdiameter.common.api.statistic.IStatistic;
import org.jdiameter.common.api.statistic.IStatisticRecord;

import java.util.concurrent.Callable;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;

class EntityFactory implements IConcurrentEntityFactory {

  private IStatistic statistic;

  EntityFactory(IStatistic statistic) {
    this.statistic = statistic;
  }

  public ThreadFactory newThreadFactory(String threadPoolName) {
    return new BaseThreadFactory(threadPoolName);
  }

  public RejectedExecutionHandler newRejectedExecutionHandler() {
    return new DefaultRejectedExecutionHandler();
  }

  public <L> Callable<L> newDefaultCallable(Callable<L> runnable, IStatisticRecord... statisticRecords) {
    return new DefaultCallable<L>(runnable, statistic, statisticRecords);
  }

  public Runnable newDefaultRunnable(Runnable runnable, IStatisticRecord... statisticRecords) {
    return new DefaultRunnable(runnable, statistic, statisticRecords);
  }
}
