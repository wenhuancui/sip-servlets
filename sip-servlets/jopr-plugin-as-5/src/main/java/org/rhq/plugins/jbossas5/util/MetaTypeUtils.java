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

package org.rhq.plugins.jbossas5.util;

import org.jboss.metatype.api.types.CompositeMetaType;
import org.jboss.metatype.api.types.MapCompositeMetaType;
import org.jboss.metatype.api.types.MetaType;
import org.jboss.metatype.api.types.SimpleMetaType;
import org.jboss.metatype.api.values.MetaValue;

/**
 * @author Mark Spritzler
 * @author Ian Springer
 */
public class MetaTypeUtils {
    /**
     * Checks if the specified MetaValue is an instance of the specified MetaType.
     *
     * @param metaValue a MetaValue
     * @param metaType  a MetaType
     * @return true if the MetaValue is an instance of the MetaType, or false if not
     */
    public static boolean instanceOf(MetaValue metaValue, MetaType metaType) {
        MetaType valueType = metaValue.getMetaType();
        if (valueType.isSimple() && metaType.isSimple())
            return true;
        else if (valueType.isEnum() && metaType.isEnum())
            return true;
        else if (valueType.isCollection() && metaType.isCollection())
            return true;
        else if (valueType.isArray() && metaType.isArray())
            return true;
        else if (valueType.isComposite() && metaType.isComposite()) {
            return (valueType instanceof MapCompositeMetaType && metaType instanceof MapCompositeMetaType)
                || (!(valueType instanceof CompositeMetaType) && !(metaType instanceof CompositeMetaType));
        } else if (valueType.isGeneric() && metaType.isGeneric())
            return true;
        else if (valueType.isTable() && metaType.isTable())
            return true;
        else if (valueType.isProperties() && metaType.isProperties())
            return true;
        else
            return false;
    }

    public static boolean isNumeric(SimpleMetaType simpleMetaType) {
        return (simpleMetaType.equals(SimpleMetaType.BIGDECIMAL) || simpleMetaType.equals(SimpleMetaType.BIGINTEGER)
            || simpleMetaType.equals(SimpleMetaType.BYTE) || simpleMetaType.equals(SimpleMetaType.BYTE_PRIMITIVE)
            || simpleMetaType.equals(SimpleMetaType.DOUBLE) || simpleMetaType.equals(SimpleMetaType.DOUBLE_PRIMITIVE)
            || simpleMetaType.equals(SimpleMetaType.FLOAT) || simpleMetaType.equals(SimpleMetaType.FLOAT_PRIMITIVE)
            || simpleMetaType.equals(SimpleMetaType.INTEGER) || simpleMetaType.equals(SimpleMetaType.INTEGER_PRIMITIVE)
            || simpleMetaType.equals(SimpleMetaType.LONG) || simpleMetaType.equals(SimpleMetaType.LONG_PRIMITIVE)
            || simpleMetaType.equals(SimpleMetaType.SHORT) || simpleMetaType.equals(SimpleMetaType.SHORT_PRIMITIVE));
    }

    private MetaTypeUtils() {
    }
}
