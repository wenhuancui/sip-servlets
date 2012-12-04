package org.mobicents.javax.media.mscontrol;

import java.net.URI;
import java.util.Iterator;

import javax.media.mscontrol.MediaObject;
import javax.media.mscontrol.Parameter;
import javax.media.mscontrol.Parameters;

public abstract class ParameterableContainer implements MediaObject{
/*
	public Parameters createParameters() {
		return new ParametersImpl();
	}

	public Parameters getParameters(Parameter[] params) {
		Parameters tmpParameters = this.createParameters();

		if (this.parameters != null) {
			if (params != null && params.length > 0) {
				for (Parameter p : this.parameters.keySet()) {
					for (Parameter pArg : params) {
						if (p.equals(pArg)) {
							tmpParameters.put(p, this.parameters.get(p));
						}
					}
				}
			} else {
				tmpParameters.putAll(this.parameters);
			}
		}
		return tmpParameters;
	}
	
	public void setParameters(Parameters params) {
		
	}

	abstract public Iterator<MediaObject> getMediaObjects();

	abstract <T extends MediaObject> Iterator<T> getMediaObjects(Class<T> arg0);

	abstract URI getURI();

	abstract void release();
*/
}
