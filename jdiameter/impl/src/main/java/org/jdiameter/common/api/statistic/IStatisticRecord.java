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

package org.jdiameter.common.api.statistic;

/**
 * This interface describe extends methods of base class
 */
public interface IStatisticRecord {

  /**
   * Increment counter
   */
  void inc();

  /**
   * Increment counter
   */
  void inc(long value);

  /**
   * Decrement counter
   */
  void dec();

  /**
   * Set value of statistic
   *
   * @param value new value of record
   */
  void setLongValue(long value);

  /**
   * Set value of statistic
   *
   * @param value new value of record
   */
  void setDoubleValue(double value);

  /**
   * Enable/Disable counter
   *
   * @param e on/off parameter
   */
  public void enable(boolean e);

  /**
   * ValueHolder for external statistics
   */
  public static interface ValueHolder {
    String getValueAsString();
  }

  public static interface IntegerValueHolder extends ValueHolder {
    /**
     * Return value of counter as integer
     *
     * @return value of counter
     */
    int getValueAsInt();
  }

  public static interface LongValueHolder extends ValueHolder {
    /**
     * Return value of counter as long
     *
     * @return value of counter
     */
    long getValueAsLong();
  }

  public static interface DoubleValueHolder extends ValueHolder {

    /**
     * Return value of counter as double
     *
     * @return value of counter
     */
    double getValueAsDouble();
  }

  //===========================

  /**
   * Return name of counter
   *
   * @return name of counter
   */
  String getName();

  /**
   * Retrurn description of counter
   *
   * @return description of counter
   */
  String getDescription();

  /**
   * Return value of counter as integer
   *
   * @return value of counter
   */
  int getValueAsInt();

  /**
   * Return value of counter as double
   *
   * @return value of counter
   */
  double getValueAsDouble();

  /**
   * Return value of counter as long
   *
   * @return value of counter
   */
  long getValueAsLong();

  /**
   * Return code of counter
   *
   * @return code of counter
   */
  int getType();

  /**
   * Return childs counters
   *
   * @return array of childs countres
   */
  IStatisticRecord[] getChilds();

  /**
   * Reset counter and all child counters
   */
  void reset();
}
