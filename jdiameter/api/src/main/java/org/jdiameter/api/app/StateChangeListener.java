/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a full listing
 * of individual contributors.
 * 
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free 
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 */
package org.jdiameter.api.app;

/**
 * Interface used to inform about changes in the state for a FSM.
 * 
 * @author erick.svenson@yahoo.com
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 * @author <a href="mailto:baranowb@gmail.com"> Bartosz Baranowski </a>
 */
public interface StateChangeListener<T> {

  /**
   * @deprecated
   * A change of state has occurred for a FSM.
   * @param oldState Old state of FSM
   * @param newState New state of FSM
   */
  @SuppressWarnings("unchecked")
  void stateChanged(Enum oldState, Enum newState);

  /**
   * A change of state has occurred for a FSM.
   * 
   * @param source the App Session that generated the change. 
   * @param oldState Old state of FSM
   * @param newState New state of FSM
   */
  @SuppressWarnings("unchecked")
  void stateChanged(T source, Enum oldState, Enum newState);
}
