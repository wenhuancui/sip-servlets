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

package org.mobicents.media.server.ctrl.mgcp;

import jain.protocol.ip.mgcp.JainMgcpResponseEvent;
import jain.protocol.ip.mgcp.message.NotificationRequest;
import jain.protocol.ip.mgcp.message.NotificationRequestResponse;
import jain.protocol.ip.mgcp.message.parms.ConnectionIdentifier;
import jain.protocol.ip.mgcp.message.parms.EndpointIdentifier;
import jain.protocol.ip.mgcp.message.parms.EventName;
import jain.protocol.ip.mgcp.message.parms.NotifiedEntity;
import jain.protocol.ip.mgcp.message.parms.RequestIdentifier;
import jain.protocol.ip.mgcp.message.parms.RequestedEvent;
import jain.protocol.ip.mgcp.message.parms.ReturnCode;
import jain.protocol.ip.mgcp.pkg.PackageName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.mobicents.media.server.ctrl.mgcp.evt.EventDetector;
import org.mobicents.media.server.ctrl.mgcp.evt.MgcpPackage;
import org.mobicents.media.server.ctrl.mgcp.evt.SignalGenerator;
import org.mobicents.media.server.spi.Connection;
import org.mobicents.media.server.spi.Endpoint;

/**
 * 
 * @author kulikov
 */
public class NotificationRequestAction implements Callable<JainMgcpResponseEvent> {

    private static Logger logger = Logger.getLogger(NotificationRequestAction.class);
    private MgcpController controller;
    private NotificationRequest req;

    public NotificationRequestAction(MgcpController controller, NotificationRequest req) {
        this.controller = controller;
        this.req = req;
    }

    public JainMgcpResponseEvent call() throws Exception {

    	if(logger.isInfoEnabled())
    	{
    		logger.info("Request TX= " + req.getTransactionHandle() + ", Endpoint = " + req.getEndpointIdentifier());
    	}

        NotificationRequestResponse response = null;

        RequestIdentifier reqID = req.getRequestIdentifier();
        EndpointIdentifier endpointID = req.getEndpointIdentifier();

        // request identifier and endpoint identifier are mandatory parameters
        if (reqID == null || endpointID == null) {
            return reject(ReturnCode.Protocol_Error);
        }

        // determine notified entity.
        // use default if not specifie explicit
        NotifiedEntity notifiedEntity = req.getNotifiedEntity();
        if (notifiedEntity == null) {
            notifiedEntity = controller.getNotifiedEntity();
        }

        // notified entity is mandatory
        if (notifiedEntity == null) {
            return reject(ReturnCode.Transient_Error);
        }

        // obatin and check endpoint
        Endpoint endpoint = null;
        try {
            endpoint = controller.getServer().lookup(endpointID.getLocalEndpointName(), true);
        } catch (Exception e) {
        	if(logger.isEnabledFor(Level.ERROR))
        	{
        		logger.error("Failed on endpoint lookup: "+endpointID.getLocalEndpointName(),e);
        	}
            return reject(ReturnCode.Endpoint_Unknown);
        }

        Request request = new Request(controller, reqID, endpointID, endpoint, notifiedEntity);
        // assign event detectors
        RequestedEvent[] events = req.getRequestedEvents();
        
        //here we can only append detectors for dtmf, 
        //thats because they are always of AUDIO type, we know interface for detection, its simple.
        //rest we group by package and couple with signals.
       
        //here we have package-requested events map, this is used with signal requests
        Map<MgcpPackage,List<RequestedEvent>> notMatchedEvents = new HashMap<MgcpPackage,List<RequestedEvent>>();
        //those we use to check if atleast all made it to submission phase.
        Map<MgcpPackage,List<RequestedEvent>> notMatchedVolatileEvents = new HashMap<MgcpPackage,List<RequestedEvent>>();
		if (events != null) {
			for (int i = 0; i < events.length; i++) {
				RequestedEvent event = events[i];
				EventName eventName = event.getEventName();

				PackageName packageName = eventName.getPackageName();

				MgcpPackage pkg = controller.getPackage(packageName.toString());
				if (pkg == null) {
					return reject(ReturnCode.Unsupported_Or_Unknown_Package);
				}
				if (pkg.getMediaType() != null && pkg.getCDetectorInterface()!=null) {
					EventDetector det = pkg.getDetector(event.getEventName()
							.getEventIdentifier(), event.getRequestedActions());

					if (det == null) {
						if(logger.isEnabledFor(Level.WARN))
						{
							logger.warn("Unknow event: " + packageName + "/"
								+ event.getEventName().getEventIdentifier());
						}
						return reject(ReturnCode.Gateway_Cannot_Detect_Requested_Event);
					}

					Connection connection = null;
					ConnectionActivity connectionActivity = null;
					// This is ConnectionActivity id
					ConnectionIdentifier connectionID = eventName
							.getConnectionIdentifier();
					if (connectionID != null) {
						// connection =
						// endpoint.getConnection(connectionID.toString());
						connectionActivity = controller.getActivity(endpoint
								.getLocalName(), connectionID.toString());
						if (connectionActivity == null) {
							return reject(ReturnCode.Connection_Was_Deleted);

						}
						det.setConnectionIdentifier(connectionID);
						connection = connectionActivity.getMediaConnection();
					}
					request.append(det, connection);
				}else
				{
					List<RequestedEvent> requestedEvents = notMatchedEvents.get(pkg);
					List<RequestedEvent> requestedVolatileEvents = notMatchedVolatileEvents.get(pkg);
					if(requestedEvents == null)
					{
						requestedEvents = new ArrayList<RequestedEvent>();
						requestedVolatileEvents =new ArrayList<RequestedEvent>();
						notMatchedEvents.put(pkg, requestedEvents);
						notMatchedVolatileEvents.put(pkg, requestedVolatileEvents);
					}
					requestedEvents.add(events[i]);
					requestedVolatileEvents.add(events[i]);

				}
				
				
			}
		}

        // queue signal
        EventName[] signals = req.getSignalRequests();
		if (signals != null) {
			for (int i = 0; i < signals.length; i++) {
				EventName eventName = signals[i];

				PackageName packageName = eventName.getPackageName();
				MgcpPackage pkg = controller.getPackage(packageName.toString());
				if (pkg == null) {
					if(logger.isEnabledFor(Level.WARN))
					{
						logger.warn("No MgcpPackage found for PackageName "
								+ packageName.toString()
								+ " Sending back"
								+ ReturnCode.Unsupported_Or_Unknown_Package
										.toString());
					}
					return reject(ReturnCode.Unsupported_Or_Unknown_Package);
				}

				SignalGenerator signal = pkg.getGenerator(eventName
						.getEventIdentifier());
				if (signal == null) {
					if(logger.isEnabledFor(Level.WARN))
					{
						logger.warn("No SignalGenerator found for MgcpEvent "
										+ eventName.getEventIdentifier()
												.toString()
										+ " Sending back"
										+ ReturnCode.Gateway_Cannot_Generate_Requested_Signal
												.toString());
					}

					return reject(ReturnCode.Gateway_Cannot_Generate_Requested_Signal);
				}

				Connection connection = null;
				ConnectionActivity connectionActivity = null;
				// This is ConnectionActivity id
				ConnectionIdentifier connectionID = eventName
						.getConnectionIdentifier();

				if (connectionID != null) {
					// connection =
					// endpoint.getConnection(connectionID.toString());
					connectionActivity = controller.getActivity(endpoint
							.getLocalName(), connectionID.toString());
					if (connectionActivity == null) {
						if(logger.isEnabledFor(Level.WARN))
						{
							logger.warn("No Connection found for ConnectionIdentifier "
											+ connectionID
											+ " Sending back"
											+ ReturnCode.Connection_Was_Deleted
													.toString());
						}
						return reject(ReturnCode.Connection_Was_Deleted);
					}
					connection = connectionActivity.getMediaConnection();
				}

				List<RequestedEvent> eventsRequestedForPackage = notMatchedEvents
						.get(pkg);
				List<RequestedEvent> requestedVolatileEvents = notMatchedVolatileEvents.get(pkg);
				if (eventsRequestedForPackage != null) {
					Iterator<RequestedEvent> it = eventsRequestedForPackage.iterator();
					while (it.hasNext()) {
						RequestedEvent event = it.next();
						eventName = event.getEventName();

						EventDetector det = pkg.getDetector(event
								.getEventName().getEventIdentifier(), event
								.getRequestedActions());

						if (det == null) {
							if(logger.isEnabledFor(Level.WARN))
							{
								logger.warn("Unknow event: "
											+ packageName
											+ "/"
											+ event.getEventName()
													.getEventIdentifier());
							}
							return reject(ReturnCode.Gateway_Cannot_Detect_Requested_Event);
						}

						if (connectionID == null
								&& eventName.getConnectionIdentifier() == null) {

							signal.configureDetector(det);
							request.append(det, connection);
							if(requestedVolatileEvents!=null)
							{
								requestedVolatileEvents.remove(event);
								if(requestedVolatileEvents.size() == 0)
								{
									notMatchedVolatileEvents.remove(pkg);
								}
							}
							//RI SUCKS, we need go into string... equals...
						} else if (connectionID != null && eventName.getConnectionIdentifier() !=null
								&& (connectionID.toString().equals(eventName
										.getConnectionIdentifier().toString()))) {
							det.setConnectionIdentifier(connectionID);
							signal.configureDetector(det);
							request.append(det, connection);
							if(requestedVolatileEvents!=null)
							{
								requestedVolatileEvents.remove(event);
								if(requestedVolatileEvents.size() == 0)
								{
									notMatchedVolatileEvents.remove(pkg);
								}
							}
						}else
						{
							//we do nothing
						}

					}

					
				}
				request.append(signal, connection);
			}
		}

        if (!request.verifyDetectors()) {
        	if(logger.isEnabledFor(Level.WARN))
			{
				logger.warn("Verification of detectors failed: ");
			}
            return reject(ReturnCode.Gateway_Cannot_Detect_Requested_Event);
        }

        if (!request.verifyGenerators()) {
            return reject(ReturnCode.Gateway_Cannot_Generate_Requested_Signal);
        }
        if(notMatchedVolatileEvents.size()!=0)
        {
        	if(logger.isEnabledFor(Level.WARN))
			{
				logger.warn("Failed to verify requested events, some are not matched! : " + notMatchedVolatileEvents);
			}
             return reject(ReturnCode.Gateway_Cannot_Detect_Requested_Event);
        }
        // disable previous signal
        Request prev = controller.requests.remove(endpoint.getLocalName());
        if (prev != null) {
            prev.cancel();        // enable current signal
        }
        controller.requests.put(endpoint.getLocalName(), request);
        request.run();

        // send response
        response = new NotificationRequestResponse(this, ReturnCode.Transaction_Executed_Normally);
        response.setTransactionHandle(req.getTransactionHandle());

        if(logger.isInfoEnabled())
        {
        	logger.info("Response TX = " + response.getTransactionHandle() + ", Response: " + response.getReturnCode());
        }
        return response;
    }

    private NotificationRequestResponse reject(ReturnCode code) {
        NotificationRequestResponse response = new NotificationRequestResponse(this, code);
        response.setTransactionHandle(req.getTransactionHandle());
        return response;
    }
}
