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

package org.mobicents.protocols.ss7.isup.impl;

import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.mobicents.protocols.ConfigurationException;
import org.mobicents.protocols.StartFailedException;
import org.mobicents.protocols.ss7.isup.ISUPProvider;
import org.mobicents.protocols.ss7.isup.ISUPStack;
import org.mobicents.protocols.ss7.mtp.provider.MtpProvider;

/**
 * Start time:12:14:57 2009-09-04<br>
 * Project: mobicents-isup-stack<br>
 * 
 * @author <a href="mailto:baranowb@gmail.com">Bartosz Baranowski </a>
 */
public class ISUPStackImpl implements ISUPStack {

	public static final String PROPERTY_CLIENT_T = "isup.client.timeout";
	public static final String PROPERTY_GENERAL_T = "isup.general.timeout";
	
	private State state = State.IDLE;
	private ISUPMtpProviderImpl isupMtpProvider;

	private ScheduledExecutorService executor = Executors.newScheduledThreadPool(8);
	private long _GENERAL_TRANSACTION_TIMEOUT = 40 * 1000;
	private long _CLIENT_TRANSACTION_ANSWER_TIMEOUT = 30 * 1000;

	public ISUPStackImpl() {
		super();

	}
	//for tests only!
	public ISUPStackImpl(MtpProvider provider1, Properties props1) {
		this.isupMtpProvider = new ISUPMtpProviderImpl(provider1,this, props1);
		this.state = State.CONFIGURED;
	}

	public ISUPProvider getIsupProvider() {

		return isupMtpProvider;

	}

	public void start() throws IllegalStateException, StartFailedException {
		if (state != State.CONFIGURED) {
			throw new IllegalStateException("Stack has not been configured or is already running!");
		}

		this.isupMtpProvider.start();

		this.state = State.RUNNING;

	}

	public void stop() {
		if (state != State.RUNNING) {
			throw new IllegalStateException("Stack is not running!");
		}
		this.isupMtpProvider.stop();
		terminate();
		this.state = State.CONFIGURED;
	}

	// ///////////////
	// CONF METHOD //
	// ///////////////
	/**
     *
     */
	public void configure(Properties props) throws ConfigurationException{
		if (state != State.IDLE) {
			throw new IllegalStateException("Stack already been configured or is already running!");
		}
		this._CLIENT_TRANSACTION_ANSWER_TIMEOUT = Integer.parseInt(props.getProperty(PROPERTY_CLIENT_T,""+_CLIENT_TRANSACTION_ANSWER_TIMEOUT));
		this._GENERAL_TRANSACTION_TIMEOUT = Integer.parseInt(props.getProperty(PROPERTY_GENERAL_T,""+_GENERAL_TRANSACTION_TIMEOUT));
		this.isupMtpProvider = new ISUPMtpProviderImpl(this, props);
		this.executor = Executors.newScheduledThreadPool(2); //MAKE THIS configurable
		this.state = State.CONFIGURED;
	}

	/**
     *
     */
	private void terminate() {
		this.executor.shutdownNow();
	}

	// possibly something similar as in MGCP

	ScheduledExecutorService getExecutors() {
		return this.executor;
	}

	/**
	 * @return
	 */
	public long getTransactionGeneralTimeout() {
		return _GENERAL_TRANSACTION_TIMEOUT;
	}

	/**
	 * @return
	 */
	public long getClientTransactionAnswerTimeout() {
		return _CLIENT_TRANSACTION_ANSWER_TIMEOUT;
	}

	private enum State {
		IDLE, CONFIGURED, RUNNING;
	}
}
