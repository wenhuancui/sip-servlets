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
public interface IStatistic {

  enum Groups {
    Peer("Peer statistic"),
    PeerFSM("Peer FSM statistic"),
    Network("Network statistic"),
    Concurrent(" Concurrent factory statistics"),
    ScheduledExecService("ScheduledExecutorService statistic");

    private int id;
    private String description;

    Groups(String description) {
      this.id = Counter.staticValue++;
      this.description = description;
    }

    public int getId() {
      return id;
    }

    public String getDescription() {
      return description;
    }
  }

  enum Counters {

    AppGenRequest("Count of app generated requests"),
    AppGenRejectedRequest("Count of rejected app generated requests"),
    AppGenResponse("Count of app generated responses"),
    AppGenRejectedResponse("Count of rejected app generated responses"),
    NetGenRequest("Count of network generated processed requests"),
    NetGenRejectedRequest("Count of network generated rejected requests"),
    NetGenResponse("Count of network generated processed responses"),
    NetGenRejectedResponse("Count of network generated rejected responses"),
    SysGenResponse("Count of platform generated responses"),

    AppGenRequestPerSecond("Count of app generated request per second"),
    AppGenResponsePerSecond("Count of app generated responses per second"),
    NetGenResponsePerSecond("Count of network generated responses per second"),
    NetGenRequestPerSecond("Count of network generated request per second"),

    RequestListenerCount("Count of network request appIdToNetListener"),
    SelectorCount("Count of network request selectorToNetListener"),

    HeapMemory("Heap memory usage"),
    NoHeapMemory("No-heap memory usage"),
    MessageProcessingTime("Average time of processing message"),

    ConcurrentThread("Count thread in default thread group"),
    ConcurrentScheduledExecutedServices("Count of ScheduledExecutorServices"),

    WorkingThread("Count of working thread"),
    CanceledTasks("Count of canceled thread"),
    ExecTimeTask("Average execution time of task"),
    WaitTimeTask("Average waiting time for execution task"),
    BrokenTasks("Count of broken thread"),
    RejectedTasks("Count of rejected tasks"),
    QueueSize("Peer FSM queue size");

    private int id;
    private String description;

    Counters(String description) {
      this.id = Counter.recordValue++;
      this.description = description;
    }

    public int getId() {
      return id;
    }

    public String getDescription() {
      return description;
    }
  }

  /**
   * Merge statistic
   *
   * @param rec external statistic
   */
  public IStatistic appendCounter(IStatisticRecord... rec);

  public IStatisticRecord getRecordByName(String name);

  //
  boolean isEnable();

  IStatisticRecord[] getRecords();

  String getName();

  String getDescription();

  void enable(boolean e);

  void reset();

  static class Counter {
    static int recordValue;
    static int staticValue;
  }

}
