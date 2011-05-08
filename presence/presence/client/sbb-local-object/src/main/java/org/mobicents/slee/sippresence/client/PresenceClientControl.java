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

/**
 * 
 */
package org.mobicents.slee.sippresence.client;

/**
 * @author martins
 * 
 */
public interface PresenceClientControl {

	/**
	 * Used to set the call back sbb local object in the sbb implementing this
	 * interface. Must be used whenever a new object of this interface is
	 * created.
	 * 
	 * An example:
	 * 
	 * ChildRelation childRelation = getChildRelation();
	 * PresenceClientControlSbbLocalObject childSbb =
	 * (PresenceClientControlSbbLocalObject) childRelation.create();
	 * childSbb.setParentSbb(
	 * (PresenceClientControlParentSbbLocalObject)this.getSbbContext
	 * ().getSbbLocalObject());
	 * 
	 * 
	 * @param parent
	 */
	public void setParentSbb(PresenceClientControlParentSbbLocalObject parentSbb);

	/**
	 * Creates a new publication for the specified Entity.
	 * 
	 * @param requestId
	 *            an object that identifies the request, the child sbb will
	 *            return it when providing the response
	 * @param entity
	 * @param document
	 * @param contentType
	 * @param contentSubType
	 * @param expires
	 *            the time in seconds, which the publication is valid
	 */
	public void newPublication(Object requestId, String entity,
			String document, String contentType, String contentSubType,
			int expires);

	/**
	 * Refreshes the publication identified by the specified Entity and ETag.
	 * 
	 * @param requestId
	 *            an object that identifies the request, the child sbb will
	 *            return it when providing the response
	 * @param entity
	 * @param eTag
	 * @param expires
	 *            the time in seconds, which the publication is valid
	 */
	public void refreshPublication(Object requestId, String entity,
			String eTag, int expires);

	/**
	 * Modifies the publication identified by the specified Entity and ETag.
	 * 
	 * @param requestId
	 *            an object that identifies the request, the child sbb will
	 *            return it when providing the response
	 * @param entity
	 * @param eTag
	 * @param document
	 * @param contentType
	 * @param contentSubType
	 * @param expires
	 *            the time in seconds, which the publication is valid
	 */
	public void modifyPublication(Object requestId, String entity, String eTag,
			String document, String contentType, String contentSubType,
			int expires);

	/**
	 * Removes the publication identified by the specified Entity and ETag.
	 * 
	 * @param requestId
	 *            an object that identifies the request, the child sbb will
	 *            return it when providing the response
	 * @param entity
	 * @param eventPackage
	 * @param eTag
	 */
	public void removePublication(Object requestId, String entity, String eTag);

	/**
	 * Creates a subscription
	 * 
	 * @param subscriber
	 * @param notifier
	 * @param eventPackage
	 *            only event packages "presence" and "presence.winfo" may be
	 *            supported by the presence server
	 * @param subscriptionId
	 * @param expires
	 */
	public void newSubscription(String subscriber,
			String subscriberdisplayName, String notifier, String eventPackage,
			String subscriptionId, int expires);

	/**
	 * Refreshes a subscription
	 * 
	 * @param subscriber
	 * @param notifier
	 * @param eventPackage
	 *            only event packages "presence" and "presence.winfo" may be
	 *            supported by the presence server
	 * @param subscriptionId
	 * @param expires
	 */
	public void refreshSubscription(String subscriber, String notifier,
			String eventPackage, String subscriptionId, int expires);

	/**
	 * Terminates a subscription.
	 * 
	 * @param subscriber
	 * @param notifier
	 * @param eventPackage
	 *            only event packages "presence" and "presence.winfo" may be
	 *            supported by the presence server
	 * @param subscriptionId
	 */
	public void removeSubscription(String subscriber, String notifier,
			String eventPackage, String subscriptionId);

}
