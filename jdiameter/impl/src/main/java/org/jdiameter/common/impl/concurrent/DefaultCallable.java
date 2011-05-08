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

import org.jdiameter.common.api.statistic.IStatistic;
import static org.jdiameter.common.api.statistic.IStatistic.Counters.*;
import org.jdiameter.common.api.statistic.IStatisticRecord;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;

class DefaultCallable<L> extends AbstractTask<Callable<L>> implements Callable<L> {

  public DefaultCallable(Callable<L> task, IStatistic statistic, IStatisticRecord... statisticRecords) {
    super(task, statistic, statisticRecords);
  }

  public L call() throws Exception {
    getCounter(WorkingThread).inc();
    long time = System.nanoTime();
    try {
      return parentTask.call();
    }
    catch (CancellationException e) {
      getCounter(CanceledTasks).inc();
      throw e;
    }
    catch (Exception e) {
      getCounter(BrokenTasks).inc();
      throw e;
    }
    finally {
      updateTimeStatistic(time, time - createdTime);
      getCounter(WorkingThread).dec();
    }
  }
}
