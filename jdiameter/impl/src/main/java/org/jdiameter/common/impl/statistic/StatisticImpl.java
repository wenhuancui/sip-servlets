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

import org.jdiameter.api.InternalException;
import org.jdiameter.common.api.statistic.IStatistic;
import org.jdiameter.common.api.statistic.IStatisticRecord;

import java.util.concurrent.ConcurrentLinkedQueue;

class StatisticImpl implements IStatistic {

  protected boolean enable = true;
  protected ConcurrentLinkedQueue<IStatisticRecord> records = new ConcurrentLinkedQueue<IStatisticRecord>();

  private String name;
  private String description;

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public StatisticImpl(String name, String desctiprion, IStatisticRecord... rec) {
    this.name = name;
    this.description = desctiprion;
    for (IStatisticRecord r : rec) {
      records.add((IStatisticRecord) r);
    }
  }

  public IStatistic appendCounter(IStatisticRecord... rec) {
    for (IStatisticRecord r : rec) {
      records.add(r);
    }
    return this;
  }

  public IStatisticRecord getRecordByName(String name) {
    for (IStatisticRecord r : records) {
      if (r.getName().equals(name)) {
        return r;
      }
    }
    return null;
  }

  public void enable(boolean e) {
    for (IStatisticRecord r : records) {
      r.enable(e);
    }
    enable = e;
  }

  public boolean isEnable() {
    return enable;
  }

  public void reset() {
    for (IStatisticRecord r : records) {
      r.reset();
    }
  }

  public IStatisticRecord[] getRecords() {
    return records.toArray(new IStatisticRecord[0]);
  }

  public String toString() {
    return "Statistic{" + " records=" + records + " }";
  }

  public boolean isWrapperFor(Class<?> aClass) throws InternalException {
    return false;
  }

  public <T> T unwrap(Class<T> aClass) throws InternalException {
    return null;
  }
}
