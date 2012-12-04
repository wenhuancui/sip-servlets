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

package org.rhq.plugins.jbossas5.adapter.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.domain.configuration.PropertySimple;
import org.rhq.core.domain.configuration.definition.PropertyDefinitionSimple;

import org.jboss.metatype.api.values.MetaValue;

/**
 * A base class for {@link PropertySimple} <-> ???MetaValue adapters.
 *
 * @author Ian Springer
 */
public abstract class AbstractPropertySimpleAdapter implements PropertyAdapter<PropertySimple, PropertyDefinitionSimple>
{
    private final Log log = LogFactory.getLog(this.getClass());

    public PropertySimple convertToProperty(MetaValue metaValue, PropertyDefinitionSimple propDefSimple)
    {
        PropertySimple propSimple = new PropertySimple(propDefSimple.getName(), null);
        populatePropertyFromMetaValue(propSimple, metaValue, propDefSimple);
        return propSimple;
    }

    public void populateMetaValueFromProperty(PropertySimple propSimple, MetaValue metaValue,
                                              PropertyDefinitionSimple propDefSimple)
    {
        if (metaValue == null)
            throw new IllegalArgumentException("MetaValue to be populated is null.");
        String value;
        if (propSimple == null || propSimple.getStringValue() == null)
        {
            // Value is null (i.e. prop is unset) - figure out what to use as a default value.
            String defaultValue = propDefSimple.getDefaultValue();
            if (defaultValue != null)
            {
                log.debug("Simple property '" + propDefSimple.getName()
                        + "' has a null value - setting inner value of corresponding ManagedProperty's MetaValue to plugin-specified default ('"
                        + defaultValue + "')...");
                value = defaultValue;
            }
            else
            {
                if (metaValue.getMetaType().isPrimitive())
                    // If it's a primitive, don't mess with the MetaValue - just let it keep whatever inner value it
                    // currently has.
                    return;
                // It's a non-primitive - set its value to null. In most cases, this should tell the managed resource
                // to use its internal default.
                log.debug("Simple property '" + propDefSimple.getName()
                        + "' has a null value - setting inner value of corresponding ManagedProperty's MetaValue to null...");
                value = null;
            }
        }
        else
        {
            value = propSimple.getStringValue();
        }
        setInnerValue(value, metaValue, propDefSimple);
    }

    protected abstract void setInnerValue(String propSimpleValue, MetaValue metaValue, PropertyDefinitionSimple propDefSimple);
}
