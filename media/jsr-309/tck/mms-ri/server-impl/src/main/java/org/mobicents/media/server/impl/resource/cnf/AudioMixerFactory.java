/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mobicents.media.server.impl.resource.cnf;

import org.mobicents.media.Component;
import org.mobicents.media.ComponentFactory;
import org.mobicents.media.server.spi.Endpoint;
import org.mobicents.media.server.spi.ResourceUnavailableException;

/**
 *
 * @author kulikov
 */
public class AudioMixerFactory implements ComponentFactory {

    private String name;
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Component newInstance(Endpoint endpoint) throws ResourceUnavailableException {
        return new AudioMixer(name, endpoint.getTimer());
    }
    
}
