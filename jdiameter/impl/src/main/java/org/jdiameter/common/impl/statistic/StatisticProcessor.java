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

package org.jdiameter.common.impl.statistic;

import org.jdiameter.api.Configuration;
import org.jdiameter.common.api.concurrent.IConcurrentFactory;
import static org.jdiameter.common.api.concurrent.IConcurrentFactory.ScheduledExecServices.StatisticTimer;
import org.jdiameter.common.api.statistic.IStatisticFactory;
import org.jdiameter.common.api.statistic.IStatisticProcessor;
import org.jdiameter.common.api.statistic.IStatisticRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StatisticProcessor implements IStatisticProcessor {

  private static final Logger log = LoggerFactory.getLogger(StatisticProcessor.class);

  private ScheduledExecutorService executorService;
  private IConcurrentFactory concurrentFactory;
  private IStatisticFactory statisticFactory;
  private Configuration config;

  public StatisticProcessor(Configuration config, IConcurrentFactory concurrentFactory, final IStatisticFactory statisticFactory) {
    this.config = config;
    this.statisticFactory = statisticFactory;
    this.concurrentFactory = concurrentFactory;
  }

  public void start() {
    this.executorService = concurrentFactory.getScheduledExecutorService(StatisticTimer.name());
    new StatisticLogger((StatisticFactory) statisticFactory, executorService, config);

    executorService.scheduleAtFixedRate(new Runnable() {
      public void run() {
        try {
          for (IStatisticRecord r : ((StatisticFactory) statisticFactory).allPSStatisticRecord) {
            r.setLongValue(r.getChilds()[0].getValueAsLong() - r.getChilds()[1].getValueAsLong());
            ((IStatisticRecord) r.getChilds()[1]).setLongValue(r.getChilds()[0].getValueAsLong());
          }
        }
        catch (Exception e) {
          log.warn("Can not start persecond statistic", e);
        }
      }
    }, 0, 1, TimeUnit.SECONDS);
  }

  public void stop() {
    concurrentFactory.shutdownNow(executorService);
  }
}
