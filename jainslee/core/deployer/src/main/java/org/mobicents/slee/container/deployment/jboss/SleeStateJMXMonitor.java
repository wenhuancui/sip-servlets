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

package org.mobicents.slee.container.deployment.jboss;

import javax.management.InstanceNotFoundException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MBeanServerNotification;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.slee.management.SleeManagementMBean;
import javax.slee.management.SleeState;
import javax.slee.management.SleeStateChangeNotification;

import org.apache.log4j.Logger;
import org.jboss.mx.util.MBeanServerLocator;

/**
 * JMX Client that, through JMX subscriptions, knows if the SLEE container is in running state.
 * 
 * @author martins
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 */
public class SleeStateJMXMonitor implements NotificationListener {

  private static final Logger logger = Logger.getLogger(SleeStateJMXMonitor.class);

  private SLEEDeployer sleeDeployer;

  // public static final int SLEE_STOPPED  = 0;
  // public static final int SLEE_STARTING = 1;
  // public static final int SLEE_RUNNING  = 2;
  // public static final int SLEE_STOPPING = 3;

  private int sleeState = 0;

  public SleeStateJMXMonitor(SLEEDeployer sleeDeployer) {
    this.sleeDeployer = sleeDeployer;
  }

  private final static Object[] EMPTY_OBJECT_ARRAY = new Object[0];
  private final static String[] EMPTY_STRING_ARRAY = new String[0];

  private boolean registredSleeManagementMBeanNotificationListener = false;

  private boolean isSleeInitialized = false;

  /**
   * 
   * @return
   */
  public SleeState getSleeState() {
    return SleeState.fromInt(getSleeStateAsInt());
  }

  public int getSleeStateAsInt() {
    synchronized (this) {
      if(!registredSleeManagementMBeanNotificationListener) {
        try {
          // find mbean server
          final MBeanServer server = MBeanServerLocator.locateJBoss();
          // register for notifications when an mbean is registered on server
          server.addNotificationListener(ObjectName.getInstance("JMImplementation:type=MBeanServerDelegate"), this, null, null);
          // check slee mngmnt mbean is registred
          final ObjectName sleeManagementMbeanObjectName = ObjectName.getInstance(SleeManagementMBean.OBJECT_NAME);
          if (server.isRegistered(sleeManagementMbeanObjectName) && !registredSleeManagementMBeanNotificationListener) {
            registerSleeManagementMBeanNotificationListener(server, sleeManagementMbeanObjectName);           
          }
          // else we wait for notification regarding its registration         
        } catch (Throwable e) {
          logger.error(e.getMessage(),e);
        }
      }
      return sleeState;
    }
  }

  private void registerSleeManagementMBeanNotificationListener(MBeanServer server, ObjectName sleeManagementMbeanObjectName) throws ListenerNotFoundException, InstanceNotFoundException, MalformedObjectNameException, NullPointerException, ReflectionException, MBeanException {
    if(!registredSleeManagementMBeanNotificationListener) {
      // ok, we don't need to know when an mbean registers in server anymore
      try {
        server.removeNotificationListener(ObjectName.getInstance("JMImplementation:type=MBeanServerDelegate"), this);
      }
      catch (Throwable e) {
        logger.warn(e.getMessage(),e);
      }
      // but we need to learn when slee state changes
      server.addNotificationListener(sleeManagementMbeanObjectName, this, null, null);
      registredSleeManagementMBeanNotificationListener = true;
      // check if slee state is running
      SleeState sleeState = (SleeState) server.invoke(sleeManagementMbeanObjectName,"getState",EMPTY_OBJECT_ARRAY,EMPTY_STRING_ARRAY);
      setSleeState(sleeState);
    }
  }		

  private void setSleeState(SleeState newSleeState) {
    if (logger.isDebugEnabled()) {
      logger.debug("setSleeState: value = "+newSleeState.toString());
    }
    if (sleeState != newSleeState.toInt()) {
      sleeState = newSleeState.toInt();
      if (newSleeState == SleeState.RUNNING) {
        sleeDeployer.sleeIsRunning();
      }
    }
  }

  /* (non-Javadoc)
   * @see javax.management.NotificationListener#handleNotification(javax.management.Notification, java.lang.Object)
   */
  public void handleNotification(final Notification notification, Object handback) {

    // do in a new thread to avoid txs from mbean server
    Runnable runnable = new Runnable() {			
      public void run() {
        synchronized (SleeStateJMXMonitor.this) {
          if (isSleeInitialized  && notification instanceof SleeStateChangeNotification) {
            if (logger.isDebugEnabled()) {
              logger.debug("received slee state change jmx notification "+notification);
            }
            SleeStateChangeNotification sscn = (SleeStateChangeNotification) notification;
            setSleeState(sscn.getNewState());
          }	
          else if (notification instanceof MBeanServerNotification) {
            MBeanServerNotification mbsn = (MBeanServerNotification) notification;
            try {
              final ObjectName sleeManagementMbeanObjectName = ObjectName.getInstance(SleeManagementMBean.OBJECT_NAME);
              if (mbsn.getType().equals(MBeanServerNotification.REGISTRATION_NOTIFICATION) && mbsn.getMBeanName().equals(sleeManagementMbeanObjectName)) {
                if (logger.isDebugEnabled()) {
                  logger.debug("received slee management mbean registration jmx notification "+notification);
                }
                registerSleeManagementMBeanNotificationListener(MBeanServerLocator.locateJBoss(),sleeManagementMbeanObjectName);
                isSleeInitialized = true;
              }
            }
            catch (Throwable e) {
              logger.error(e.getMessage(),e);
            }
          }				
        }
      }
    };
    new Thread(runnable).start();
  }

  public boolean isSleeRunning() {
    return sleeState == SleeState.RUNNING.toInt();
  }
}
