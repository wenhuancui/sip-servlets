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
import org.jdiameter.client.impl.helpers.Parameters;
import org.jdiameter.common.api.statistic.IStatistic;
import org.jdiameter.common.api.statistic.IStatisticRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StatisticLogger {

  private static final String STATS_ROOT_LOGGER_NAME = "jdiameter.statistic";
  private static final String STATS_LOGGER_PREFIX = "jdiameter.statistic.";
  
  public StatisticLogger(final StatisticFactory factory, ScheduledExecutorService concurrentFactory, Configuration config) {

    long pause = (Long) Parameters.StatisticLoggerPause.defValue();
    long delay = (Long) Parameters.StatisticLoggerDelay.defValue();
    Configuration[] loggerParams = config.getChildren(Parameters.StatisticLogger.ordinal());
    if (loggerParams != null && loggerParams.length > 0) {
      pause = loggerParams[0].getLongValue(Parameters.StatisticLoggerPause.ordinal(), pause);
      delay = loggerParams[0].getLongValue(Parameters.StatisticLoggerDelay.ordinal(), delay);
    }

    concurrentFactory.scheduleAtFixedRate(new Runnable() {
      
      HashMap<String, Logger> loggers = new HashMap<String, Logger>();

      public void run() {
        boolean oneLine = false;
        for (IStatistic statistic : factory.allStatistic) {
          if (statistic.isEnable()) {
            for (IStatisticRecord record : statistic.getRecords()) {
              oneLine = true;
              String loggerKey = statistic.getName() + "." + record.getName();
              Logger logger = null;
              if((logger = loggers.get(loggerKey)) == null) {
                logger = LoggerFactory.getLogger(STATS_LOGGER_PREFIX + loggerKey);
                loggers.put(loggerKey, logger);
              }
              if(logger.isTraceEnabled()) {
                logger.trace(record.toString());
              }
            }
          }
        }
        if (oneLine) {
          Logger logger = null;
          if((logger = loggers.get(STATS_ROOT_LOGGER_NAME)) == null) {
            logger = LoggerFactory.getLogger(STATS_ROOT_LOGGER_NAME);
            loggers.put(STATS_ROOT_LOGGER_NAME, logger);
          }
          if(logger.isTraceEnabled()) {
            logger.trace("=============================================== Marker ===============================================");
          }
        }
      }
    }, pause, delay, TimeUnit.MILLISECONDS);
  }

}
