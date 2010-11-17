package org.openxdm.xcap.server.slee.appusage.resourcelists;

import javax.xml.validation.Schema;

import org.mobicents.xdm.server.appusage.AppUsage;
import org.mobicents.xdm.server.appusage.AppUsageDataSourceInterceptor;
import org.mobicents.xdm.server.appusage.AppUsageFactory;

public class ResourceListsAppUsageFactory implements AppUsageFactory {

	private final Schema schema;
	private final boolean omaCompliant;

	public ResourceListsAppUsageFactory(Schema schema, boolean omaCompliant) {
		this.schema = schema;
		this.omaCompliant = omaCompliant;
	}
	
	public AppUsage getAppUsageInstance() {
		return new ResourceListsAppUsage(schema.newValidator(),omaCompliant);
	}
	
	public String getAppUsageId() {
		return ResourceListsAppUsage.ID;
	}
	
	@Override
	public AppUsageDataSourceInterceptor getDataSourceInterceptor() {
		return null;
	}
}