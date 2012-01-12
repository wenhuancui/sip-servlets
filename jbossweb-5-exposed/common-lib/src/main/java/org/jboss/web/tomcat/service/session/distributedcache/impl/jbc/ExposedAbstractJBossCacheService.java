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
import java.util.Set;

import org.jboss.cache.Cache;
import org.jboss.cache.Fqn;
import org.jboss.web.tomcat.service.session.distributedcache.spi.DistributableSessionMetadata;
import org.jboss.web.tomcat.service.session.distributedcache.spi.IncomingDistributableSessionData;
import org.jboss.web.tomcat.service.session.distributedcache.spi.OutgoingDistributableSessionData;

public class ExposedAbstractJBossCacheService {

	private final AbstractJBossCacheService<OutgoingDistributableSessionData> jBossCacheService;
	private final ExposedJBossCacheWrapper cacheWrapper;
	
	public ExposedAbstractJBossCacheService(AbstractJBossCacheService<OutgoingDistributableSessionData> jBossCacheService) {
		this.jBossCacheService = jBossCacheService;
		this.cacheWrapper = new ExposedJBossCacheWrapper(jBossCacheService.cacheWrapper_);
	}
	
	public String getCacheConfigName() {
		return jBossCacheService.cacheConfigName_;
	}
	
	public void setCacheConfigName(String s) {
		jBossCacheService.cacheConfigName_ = s;
	}
	
	public String getCombinedPath() {
		return jBossCacheService.combinedPath_;
	}
	
	public ExposedJBossCacheWrapper getCacheWrapper() {
		return cacheWrapper;
	}

	public Cache<Object, Object> getCache() {
		return jBossCacheService.getCache();
	}

	public void setupSessionRegion(Fqn<String> fqn) {
		jBossCacheService.setupSessionRegion(fqn);
	}

	public Object getUnMarshalledValue(
			DistributableSessionMetadata metadata) {
		return jBossCacheService.getMarshalledValue(metadata);
	}

	public IncomingDistributableSessionData getDistributableSessionData(
			String key, Map<Object, Object> distributedCacheData,
			boolean includeAttributes) {
		return jBossCacheService.getDistributableSessionData(key, distributedCacheData, includeAttributes);
	}
	
	public Set<String> getChildrenNames(Fqn<String> sipappFqn) {
		return jBossCacheService.getChildrenNames(sipappFqn);
	}

	public void removeSessionRegion(String key, Fqn<String> fqn) {
		jBossCacheService.removeSessionRegion(key, fqn);
	}

	public AbstractJBossCacheService<OutgoingDistributableSessionData> getWrappedjBossCacheService() {
		return jBossCacheService;
	}
}
