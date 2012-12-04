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
package org.mobicents.slee.enabler.xdmc;

import java.net.URI;

/**
 * @author martins
 * 
 */
public interface XDMClientParent {

	/**
	 * Provides the response for an XML resource GET request.
	 * 
	 * @param uri
	 * @param responseCode
	 * @param mimetype
	 * @param content
	 * @param eTag
	 */
	public void getResponse(URI uri, int responseCode, String mimetype,
			String content, String eTag);

	/**
	 * Provides the response for an XML resource PUT request.
	 * 
	 * @param uri
	 * @param responseCode
	 * @param eTag
	 * @param responseContent
	 */
	public void putResponse(URI uri, int responseCode, String responseContent,
			String eTag);

	/**
	 * Provides the response for an XML resource DELETE request.
	 * 
	 * @param uri
	 * @param responseCode
	 * @param eTag
	 * @param responseContent
	 */
	public void deleteResponse(URI uri, int responseCode,
			String responseContent, String eTag);

	/**
	 * Callback that indicates a document subscribed in the XDM client was
	 * updated
	 */
	// public void documentUpdated(DocumentSelector documentSelector,
	// String oldETag, String newETag, String documentAsString);

	/**
	 * Callback that indicates a element in a subscribed document was updated
	 */
	// public void elementUpdated(DocumentSelector documentSelector,
	// NodeSelector nodeSelector, Map<String, String> namespaces,
	// String oldETag, String newETag, String documentAsString,
	// String elementAsString);

	/**
	 * Callback that indicates a attribute in a subscribed document was updated
	 */
	// public void attributeUpdated(DocumentSelector documentSelector,
	// NodeSelector nodeSelector, AttributeSelector attributeSelector,
	// Map<String, String> namespaces, String oldETag, String newETag,
	// String documentAsString, String attributeValue);

}
