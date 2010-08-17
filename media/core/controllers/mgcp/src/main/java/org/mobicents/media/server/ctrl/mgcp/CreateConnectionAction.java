/*
 * JBoss, Home of Professional Open Source
 * Copyright XXXX, Red Hat Middleware LLC, and individual contributors as indicated
 * by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a full listing
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 */
package org.mobicents.media.server.ctrl.mgcp;

import jain.protocol.ip.mgcp.message.CreateConnection;
import jain.protocol.ip.mgcp.JainMgcpResponseEvent;
import jain.protocol.ip.mgcp.message.CreateConnectionResponse;
import jain.protocol.ip.mgcp.message.parms.ConnectionDescriptor;
import jain.protocol.ip.mgcp.message.parms.ConnectionIdentifier;
import jain.protocol.ip.mgcp.message.parms.EndpointIdentifier;
import jain.protocol.ip.mgcp.message.parms.ReturnCode;
import java.io.IOException;
import java.util.concurrent.Callable;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.mobicents.media.server.spi.Connection;
import org.mobicents.media.server.spi.ConnectionMode;
import org.mobicents.media.server.spi.Endpoint;
import org.mobicents.media.server.spi.ResourceUnavailableException;

/**
 * 
 * @author kulikov
 * @author amit bhayani
 */
public class CreateConnectionAction implements Callable {

	private static final ConnectionIdentifier ERROR_CONNID = new ConnectionIdentifier("0");
	private CreateConnection crcx;
	private MgcpController controller;
	private MgcpUtils utils = new MgcpUtils();
	private static Logger logger = Logger.getLogger(CreateConnectionAction.class);

	protected CreateConnectionAction(MgcpController controller, CreateConnection req) {
		this.controller = controller;
		this.crcx = req;
	}

	/**
	 * Creates RTP connection.
	 * 
	 * @param crcx
	 *            parameters of the connection to be created incapsulated
	 *            withing MGCP CRCX message.
	 * @return response with result of the parameters of the actualy created
	 *         connection
	 */
	private JainMgcpResponseEvent createRtpConnection(CreateConnection crcx) {
		int txID = crcx.getTransactionHandle();

		// TODO : ConnectionIdentifier cannot be null when sending CRCX Response
		// in Exception blocks

		// reading connection mode
		ConnectionMode mode = utils.getMode(crcx.getMode());
		if (mode == null) {
            if (logger.isEnabledFor(Level.WARN)) {
                logger.warn("TX = " + txID + ", Mode " + crcx.getMode() + " is not supported, Response code: " + ReturnCode.UNSUPPORTED_OR_INVALID_MODE);
			}
			return new CreateConnectionResponse(crcx.getSource(), ReturnCode.Unsupported_Or_Invalid_Mode, ERROR_CONNID);
		}

		// reading endpoint identiier
		String localName = crcx.getEndpointIdentifier().getLocalEndpointName();
		if (localName.contains("*")) {
            if (logger.isEnabledFor(Level.WARN)) {
                logger.warn("TX = " + txID + ", The endpoint name is underspecified with 'all off' wildcard, Response code: " + ReturnCode.ENDPOINT_UNKNOWN);
			}
			return new CreateConnectionResponse(crcx.getSource(), ReturnCode.Endpoint_Unknown, ERROR_CONNID);
		}

		// lookup endpoint
		Endpoint endpoint = null;
		try {
			endpoint = controller.getServer().lookup(localName, false);
			if (logger.isDebugEnabled()) {
				logger.debug("TX=" + txID + ", Allocated endpoint: " + endpoint.getLocalName());
			}
		} catch (ResourceUnavailableException e) {
            if (logger.isEnabledFor(Level.ERROR)) {
                logger.error("TX = " + txID + ", There is no free endpoint: " + localName + ", ResponseCode: " + ReturnCode.ENDPOINT_UNKNOWN);
			}
			return new CreateConnectionResponse(crcx.getSource(), ReturnCode.Endpoint_Unknown, ERROR_CONNID);
		}

		Connection connection = null;
		try {
			connection = endpoint.createConnection();
			connection.setMode(mode);
			if (logger.isDebugEnabled()) {
				logger.debug("TX=" + txID + ", Endpoint: " + endpoint.getLocalName() + ", Created connection ");
			}
		} catch (Exception e) {
            if (logger.isEnabledFor(Level.ERROR)) {
                logger.error("", e);
			}
			return new CreateConnectionResponse(crcx.getSource(), ReturnCode.Endpoint_Insufficient_Resources,
					ERROR_CONNID);
		}

		// try to assign the session desriptor if present
		ConnectionDescriptor remoteSdp = crcx.getRemoteConnectionDescriptor();
		if (remoteSdp != null) {
			try {
				connection.setRemoteDescriptor(remoteSdp.toString());
			} catch (Exception e) {
                //delete current connection
                endpoint.deleteConnection(connection.getId());
                if (logger.isEnabledFor(Level.ERROR)) {
                    logger.error("Could not apply remote descriptor", e);
				}
				return new CreateConnectionResponse(crcx.getSource(), ReturnCode.Missing_RemoteConnectionDescriptor,
						ERROR_CONNID);
			}
		}
		
		String callID = crcx.getCallIdentifier().toString();
		Call call = controller.getCall(callID);

		boolean isNewCall = call == null;
		if (isNewCall) {
			call = new Call(callID, controller);
			if (logger.isDebugEnabled()) {
				logger.debug("TX = " + txID + ", Created new call instance, ID = " + call.getID());
			}
		} else if (logger.isDebugEnabled()) {
			logger.debug("TX = " + txID + ", Using existing call instance, ID = " + call.getID());
		}
		ConnectionActivity connectionActivity = null;
		try{
			connectionActivity = call.addConnection(connection);
			if (logger.isDebugEnabled()) {
				logger.debug("Created connection activity=" + connectionActivity.getID() + ", origin connection ID ="
						+ connection.getId());
			}
		}catch(IllegalStateException e){
			try{
				if(logger.isEnabledFor(Level.ERROR))
				{
					logger.error("TX=" + txID + " Failed to create ConnectionActivity for connection: "+connection.getId()+", from endpoint: "+endpoint.getLocalName(),e);
				}
				endpoint.deleteConnection(connection.getId());
			}finally{
				return new CreateConnectionResponse(crcx.getSource(), ReturnCode.Endpoint_Insufficient_Resources,
					ERROR_CONNID);
			}
		}		

		ConnectionDescriptor localSDP = new ConnectionDescriptor(connection.getLocalDescriptor());
		ConnectionIdentifier connectionID = new ConnectionIdentifier(connectionActivity.getID());

		// Prepearing response
		CreateConnectionResponse response = new CreateConnectionResponse(crcx.getSource(),
				ReturnCode.Transaction_Executed_Normally, connectionID);
        response.setSpecificEndpointIdentifier(new EndpointIdentifier(endpoint.getLocalName(), crcx.getEndpointIdentifier().getDomainName()));
		response.setConnectionIdentifier(connectionID);
		response.setLocalConnectionDescriptor(localSDP);
		response.setTransactionHandle(crcx.getTransactionHandle());

		// Save ref to a new call
		if (isNewCall) {
			try{
				controller.addCall(call);
				if (logger.isDebugEnabled()) {
					logger.debug("Save reference to the callID=" + call.getID());
				}
			}catch(IllegalStateException e){
				try{
					if(logger.isEnabledFor(Level.ERROR))
					{
						logger.error("TX=" + txID + " Failed to add Call : "+call.getID()+", from endpoint: "+endpoint.getLocalName(),e);
					}
					endpoint.deleteConnection(connection.getId());
				}finally{
					return new CreateConnectionResponse(crcx.getSource(), ReturnCode.Endpoint_Insufficient_Resources,
						ERROR_CONNID);
				}
			}				
		}

		return response;
	}

	private JainMgcpResponseEvent createLink(CreateConnection crcx) {
		int txID = crcx.getTransactionHandle();

		// reading connection mode
		ConnectionMode mode = utils.getMode(crcx.getMode());
		if (mode == null) {
            if (logger.isEnabledFor(Level.WARN)) {
                logger.warn("TX = " + txID + ", Mode " + crcx.getMode() + " is not supported, Response code: " + ReturnCode.UNSUPPORTED_OR_INVALID_MODE);
			}
			return new CreateConnectionResponse(crcx.getSource(), ReturnCode.Unsupported_Or_Invalid_Mode, ERROR_CONNID);
		}

		// reading endpoint identiier
		String localName = crcx.getEndpointIdentifier().getLocalEndpointName();
		if (localName.contains("*")) {
            if (logger.isEnabledFor(Level.WARN)) {
                logger.warn("TX = " + txID + ", The endpoint name is underspecified with 'all off' wildcard, Response code: " + ReturnCode.ENDPOINT_UNKNOWN);
			}
			return new CreateConnectionResponse(crcx.getSource(), ReturnCode.Endpoint_Unknown, ERROR_CONNID);
		}

		// lookup endpoint
		Endpoint endpoint = null;
		try {
			endpoint = controller.getServer().lookup(localName, true);
		} catch (ResourceUnavailableException e) {
            if (logger.isEnabledFor(Level.ERROR)) {
                logger.error("TX = " + txID + ", There is no free endpoint: " + localName + ", ResponseCode: " + ReturnCode.ENDPOINT_UNKNOWN);
			}
			return new CreateConnectionResponse(crcx.getSource(), ReturnCode.Endpoint_Unknown, ERROR_CONNID);
		}

		String callID = crcx.getCallIdentifier().toString();
		Call call = controller.getCall(callID);

		boolean isNewCall = call == null;
		if (isNewCall) {
			call = new Call(callID, controller);
			if (logger.isDebugEnabled()) {
				logger.debug("TX = " + txID + ", Created new call instance, ID = " + call.getID());
			}
		} else if (logger.isDebugEnabled()) {
			logger.debug("TX = " + txID + ", Using existing call instance, ID = " + call.getID());
		}

		Connection connection = null;
		try {
			connection = endpoint.createLocalConnection();
			connection.setMode(mode);
			if (logger.isDebugEnabled()) {
				logger.debug("TX=" + txID + ", Endpoint: " + endpoint.getLocalName() + ", Created connection ");
			}
		} catch (Exception e) {
			return new CreateConnectionResponse(crcx.getSource(), ReturnCode.Endpoint_Insufficient_Resources,
					ERROR_CONNID);
		}

		// creating second connection now
		String localName2 = crcx.getSecondEndpointIdentifier().getLocalEndpointName();
		if (localName2.contains("*")) {
			// deleting first connection also
			endpoint.deleteConnection(connection.getId());
            if (logger.isEnabledFor(Level.WARN)) {
                logger.warn("TX = " + txID + ", The endpoint name is underspecified with 'all off' wildcard, Response code: " + ReturnCode.ENDPOINT_UNKNOWN);
			}
			return new CreateConnectionResponse(crcx.getSource(), ReturnCode.Endpoint_Unknown, ERROR_CONNID);
		}

		// lookup endpoint 2
		Endpoint endpoint2 = null;
		try {
			endpoint2 = controller.getServer().lookup(localName2, true);
		} catch (ResourceUnavailableException e) {
            if (logger.isEnabledFor(Level.ERROR)) {
                logger.error("TX = " + txID + ", There is no free endpoint: " + localName2 + ", ResponseCode: " + ReturnCode.ENDPOINT_UNKNOWN);
			}
			return new CreateConnectionResponse(crcx.getSource(), ReturnCode.Endpoint_Unknown, ERROR_CONNID);
		}

		// The mode of second connection is always send/recv as specified in
		// RFC3435
		Connection connection2 = null;
		try {
			connection2 = endpoint2.createLocalConnection();
			connection2.setMode(ConnectionMode.SEND_RECV);
			if (logger.isDebugEnabled()) {
				logger.debug("TX=" + txID + ", Endpoint: " + endpoint2.getLocalName() + ", Created connection ");
			}
		} catch (Exception e) {
            try {
                if (logger.isEnabledFor(Level.ERROR)) {
                    logger.error("TX=" + txID + ", Endpoint: " + endpoint2.getLocalName() + ", Created connection , failed to create connection. ", e);
				}
				endpoint.deleteConnection(connection.getId());
            } finally {
				return new CreateConnectionResponse(crcx.getSource(), ReturnCode.Endpoint_Insufficient_Resources,
					ERROR_CONNID);
			}
		}

		try {
			connection.setOtherParty(connection2);
                        connection2.setOtherParty(connection);
		} catch (IOException e) {
            try {
                if (logger.isEnabledFor(Level.ERROR)) {
                    logger.error("TX=" + txID + " Failed to link connection: " + connection.getId() + ", from endpoint: " + endpoint.getLocalName() + ", with connection: " +
                            connection2.getId() + ", from endpoint:  " + endpoint2.getLocalName(), e);
				}
				endpoint.deleteConnection(connection.getId());
				endpoint2.deleteConnection(connection2.getId());
            } finally {
				return new CreateConnectionResponse(crcx.getSource(), ReturnCode.Endpoint_Insufficient_Resources,
					ERROR_CONNID);
			}
		}

		ConnectionActivity connectionActivity = null;
		try{
			connectionActivity = call.addConnection(connection);
			if (logger.isDebugEnabled()) {
				logger.debug("Created connection activity=" + connectionActivity.getID() + ", origin connection ID ="
						+ connection.getId());
			}
		}catch(IllegalStateException e){
			try{
				if(logger.isEnabledFor(Level.ERROR))
				{
					logger.error("TX=" + txID + " Failed to create ConnectionActivity for connection: "+connection.getId()+", from endpoint: "+endpoint.getLocalName(),e);
				}
				endpoint.deleteConnection(connection.getId());
				endpoint2.deleteConnection(connection2.getId());
			}finally{
				return new CreateConnectionResponse(crcx.getSource(), ReturnCode.Endpoint_Insufficient_Resources,
					ERROR_CONNID);
			}
		}

		ConnectionActivity connectionActivity2 = null;
		try{		
			connectionActivity2 = call.addConnection(connection2);
			if (logger.isDebugEnabled()) {
				logger.debug("Created connection activity=" + connectionActivity2.getID() + ", origin connection ID ="
						+ connection2.getId());
			}
		}catch(IllegalStateException e){
			try{
				if(logger.isEnabledFor(Level.ERROR))
				{
					logger.error("TX=" + txID + " Failed to create ConnectionActivity for connection: "+connection2.getId()+", from endpoint:  "+endpoint2.getLocalName(),e);
				}
				endpoint.deleteConnection(connection.getId());
				endpoint2.deleteConnection(connection2.getId());
			}finally{
				return new CreateConnectionResponse(crcx.getSource(), ReturnCode.Endpoint_Insufficient_Resources,
					ERROR_CONNID);
			}
		}		

		ConnectionIdentifier connectionID = new ConnectionIdentifier(connectionActivity.getID());
		ConnectionIdentifier connectionID2 = new ConnectionIdentifier(connectionActivity2.getID());

		// Sending response
		CreateConnectionResponse response = new CreateConnectionResponse(crcx.getSource(),
				ReturnCode.Transaction_Executed_Normally, connectionID);
		response.setSpecificEndpointIdentifier(new EndpointIdentifier(endpoint.getLocalName(), crcx
				.getEndpointIdentifier().getDomainName()));
		response.setSecondEndpointIdentifier(new EndpointIdentifier(endpoint2.getLocalName(), crcx
				.getEndpointIdentifier().getDomainName()));
		response.setSecondConnectionIdentifier(connectionID2);
		response.setTransactionHandle(crcx.getTransactionHandle());
		
		// Save ref to a new call
		if (isNewCall) {
			try{
				controller.addCall(call);
				if (logger.isDebugEnabled()) {
					logger.debug("Save reference to the callID=" + call.getID());
				}
			}catch(IllegalStateException e){
				try{
					if(logger.isEnabledFor(Level.ERROR))
					{
						logger.error("TX=" + txID + " Failed to add Call : "+call.getID()+", from endpoint: "+endpoint.getLocalName(),e);
					}
					endpoint.deleteConnection(connection.getId());
					endpoint2.deleteConnection(connection2.getId());
				}finally{
					return new CreateConnectionResponse(crcx.getSource(), ReturnCode.Endpoint_Insufficient_Resources,
						ERROR_CONNID);
				}
			}		
		}

        // Save ref to a new call
        if (isNewCall) {
            controller.addCall(call);
            if (logger.isDebugEnabled()) {
                logger.debug("Save reference to the callID=" + call.getID());
            }
        }

		return response;
	}

	public JainMgcpResponseEvent call() throws Exception {
		if (logger.isDebugEnabled()) {
            logger.debug("Request TX= " + crcx.getTransactionHandle() + ", CallID = " + crcx.getCallIdentifier() + ", Mode=" + crcx.getMode() + ", Endpoint = " + crcx.getEndpointIdentifier() + ", Endpoint2 = " + crcx.getSecondEndpointIdentifier() + ", SDP " + (crcx.getRemoteConnectionDescriptor()));
		}

		// CreateConnection may be used to create either an RTP connection or
		// a pair of local connections. If SecondEndpoint is not specified then
		// RTP connection will be created or two local connections other way.
		JainMgcpResponseEvent response = crcx.getSecondEndpointIdentifier() == null ? createRtpConnection(crcx)
				: createLink(crcx);
		response.setTransactionHandle(crcx.getTransactionHandle());
		logger.info("Response TX = " + response.getTransactionHandle() + ", Response: " + response.getReturnCode());
		return response;
	}
}
