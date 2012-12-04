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

package org.jboss.web.tomcat.service.session.distributedcache.impl.jbc;

import java.util.Map;

import org.jboss.cache.Fqn;

public class ExposedJBossCacheWrapper {

	private final JBossCacheWrapper cacheWrapper;
	
	public ExposedJBossCacheWrapper(JBossCacheWrapper cacheWrapper) {
		this.cacheWrapper = cacheWrapper;
	}

	public void put(Fqn<String> fqn, Map<Object, Object> marshalled) {
		cacheWrapper.put(fqn, marshalled);
	}

	public Object remove(Fqn<String> fqn, String key) {
		return cacheWrapper.remove(fqn, key);		
	}

	public Object get(Fqn<String> fromString, String key) {
		return cacheWrapper.get(fromString, key);
	}

	public void put(Fqn<String> fromString, String key, Object marshalledValue) {
		cacheWrapper.put(fromString, key, marshalledValue);		
	}

	public Object removeLocal(Fqn<String> fromString, String key) {
		return cacheWrapper.removeLocal(fromString, key);
	}

	public void removeLocal(Fqn<String> fqn) {
		cacheWrapper.removeLocal(fqn);		
	}

	public void evictSubtree(Fqn<String> fqn) {
		cacheWrapper.evictSubtree(fqn);
	}

	public Map<Object, Object> getData(Fqn<String> fqn, boolean b) {
		return cacheWrapper.getData(fqn, b);
	}

	public void remove(Fqn<String> fqn) {
		cacheWrapper.remove(fqn);
	}	

}
