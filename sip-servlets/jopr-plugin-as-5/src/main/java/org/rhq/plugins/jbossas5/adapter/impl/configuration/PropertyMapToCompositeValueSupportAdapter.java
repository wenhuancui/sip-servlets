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

package org.rhq.plugins.jbossas5.adapter.impl.configuration;

import org.rhq.core.domain.configuration.PropertyMap;
import org.rhq.core.domain.configuration.definition.PropertyDefinition;
import org.rhq.core.domain.configuration.definition.PropertyDefinitionMap;
import org.rhq.plugins.jbossas5.adapter.api.PropertyAdapter;
import org.rhq.plugins.jbossas5.util.ConversionUtils;

import org.jboss.metatype.api.types.MetaType;
import org.jboss.metatype.api.values.CompositeValue;
import org.jboss.metatype.api.values.CompositeValueSupport;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.metatype.plugins.types.MutableCompositeMetaType;

/**
 * This class provides code that maps back and forth between a {@link PropertyMap} and a {@link CompositeValueSupport}.
 * A <code>CompositeValueSupport</code> is a {@link CompositeValue} implementation that contains items that may be of
 * different types.
 *
 * @author Ian Springer
 */
public class PropertyMapToCompositeValueSupportAdapter extends AbstractPropertyMapToCompositeValueAdapter
        implements PropertyAdapter<PropertyMap, PropertyDefinitionMap>
{
    protected void putValue(CompositeValue compositeValue, String key, MetaValue value)
    {
        CompositeValueSupport compositeValueSupport = (CompositeValueSupport)compositeValue;
        compositeValueSupport.set(key, value);
    }

    protected CompositeValue createCompositeValue(PropertyDefinitionMap propDefMap, MetaType metaType)
    {
        MutableCompositeMetaType compositeMetaType;
        if (metaType != null)
            compositeMetaType = (MutableCompositeMetaType)metaType;
        else
        {
            // TODO: See if this else block is actually necessary (I think it is needed for creates).
            String name = (propDefMap != null) ?
                    propDefMap.getName() : "CompositeMetaType";
            String desc = (propDefMap != null && propDefMap.getDescription() != null) ?
                    propDefMap.getDescription() : "none";
            compositeMetaType = new MutableCompositeMetaType(name, desc);
            if (propDefMap != null)
            {
                for (PropertyDefinition mapMemberPropDef : propDefMap.getPropertyDefinitions().values())
                {
                    String mapMemberDesc = (propDefMap.getDescription() != null) ? propDefMap.getDescription() : "none";
                    MetaType mapMemberMetaType = ConversionUtils.convertPropertyDefinitionToMetaType(mapMemberPropDef);
                    compositeMetaType.addItem(mapMemberPropDef.getName(), mapMemberDesc, mapMemberMetaType);
                }
            }
        }
        return new CompositeValueSupport(compositeMetaType);
    }
}