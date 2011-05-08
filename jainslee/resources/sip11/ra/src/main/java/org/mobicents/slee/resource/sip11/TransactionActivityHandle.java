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

package org.mobicents.slee.resource.sip11;

import java.io.Serializable;

import javax.sip.Transaction;

/**
 * The {@link SipActivityHandle} for {@link Transaction} activity.
 * 
 * @author martins
 * 
 */
public abstract class TransactionActivityHandle extends SipActivityHandle implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * the transaction's branch
	 */
	private final String branchId;
	
	private final String method;
	
	/**
	 * 
	 * @param branchId
	 * @param method
	 */
	public TransactionActivityHandle(String branchId,String method) {
		if (branchId == null) {
			throw new NullPointerException("null branch id");
		}
		if (method == null) {
			throw new NullPointerException("null method");
		}
		this.branchId = branchId;
		this.method = method;
	}

	/**
	 * Retrieves the transaction's branch id
	 * 
	 * @return
	 */
	public String getBranchId() {
		return branchId;
	}

	/**
	 * 
	 * @return
	 */
	public String getMethod() {
		return method;
	}

	@Override
	public int hashCode() {
		return branchId.hashCode()*31+method.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final TransactionActivityHandle other = (TransactionActivityHandle) obj;
		if (!branchId.equals(other.branchId))
			return false;
		if (!method.equals(other.method))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return new StringBuilder(branchId).append(':').append(method).toString();
	}

}
