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

import org.jdiameter.common.api.statistic.IStatistic;
import org.jdiameter.common.api.statistic.IStatisticFactory;
import org.jdiameter.common.api.statistic.IStatisticRecord;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class StatisticFactory implements IStatisticFactory {

  List<IStatistic> allStatistic = new CopyOnWriteArrayList<IStatistic>();
  List<IStatisticRecord> allPSStatisticRecord = new CopyOnWriteArrayList<IStatisticRecord>();

  public IStatisticRecord newCounterRecord(IStatistic.Counters recordDescription) {
    return new StatisticRecordImpl(recordDescription.name(), recordDescription.getDescription(), recordDescription.getId());
  }

  public IStatisticRecord newCounterRecord(IStatistic.Counters recordDescription, IStatisticRecord.ValueHolder counters) {
    return new StatisticRecordImpl(recordDescription.name(), recordDescription.getDescription(), recordDescription.getId(), counters);
  }

  public IStatisticRecord newCounterRecord(IStatistic.Counters recordDescription, IStatisticRecord.ValueHolder counter, IStatisticRecord... rec) {
    return new StatisticRecordImpl(recordDescription.name(), recordDescription.getDescription(), recordDescription.getId(), counter, rec);
  }

  public IStatisticRecord newCounterRecord(String name, String description, int id) {
    return new StatisticRecordImpl(name, description, id);
  }

  public IStatisticRecord newCounterRecord(String name, String description, int id, IStatisticRecord.ValueHolder counters) {
    return new StatisticRecordImpl(name, description, id, counters);
  }

  public IStatisticRecord newPerSecondCounterRecord(IStatistic.Counters recordDescription, IStatisticRecord child) {
    IStatisticRecord prevValue = new StatisticRecordImpl(recordDescription.name(), recordDescription.getDescription(), recordDescription.getId());
    IStatisticRecord psStatistic = new StatisticRecordImpl(recordDescription.name(), recordDescription.getDescription(), recordDescription.getId(), child, prevValue);
    allPSStatisticRecord.add(psStatistic);
    return prevValue;
  }

  public IStatistic newStatistic(IStatistic.Groups group, IStatisticRecord... rec) {
    IStatistic statistic = new StatisticImpl(group.name(), group.getDescription(), rec);
    allStatistic.add(statistic);
    return statistic;
  }

  public IStatistic newStatistic(String name, String description, IStatisticRecord... rec) {
    IStatistic statistic = new StatisticImpl(name, description, rec);
    allStatistic.add(statistic);
    return statistic;
  }
}
