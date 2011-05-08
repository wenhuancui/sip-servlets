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

package org.rhq.plugins.jbossas5;

import java.util.Set;

import org.jboss.managed.api.ManagedComponent;
import org.jboss.managed.api.ComponentType;

import org.rhq.plugins.jbossas5.util.Ejb2BeanUtils;

/**
 * A plugin component for managing an EJB 1/2 bean.
 *
 * @author Lukas Krejci
 */
public class Ejb2BeanComponent extends AbstractEjbBeanComponent {
    private static final ComponentType MDB_COMPONENT_TYPE = new ComponentType("EJB", "MDB");

    @Override
    protected ManagedComponent getManagedComponent() {
        if (MDB_COMPONENT_TYPE.equals(getComponentType())) {
            try {
                //we need to reload the management view here, because the MDBs might have changed since
                //the last call, because the @object-id is part of their names.
                getConnection().getManagementView().load();

                Set<ManagedComponent> mdbs = getConnection().getManagementView().getComponentsForType(
                    MDB_COMPONENT_TYPE);

                for (ManagedComponent mdb : mdbs) {
                    if (getComponentName().equals(Ejb2BeanUtils.getUniqueBeanIdentificator(mdb))) {
                        return mdb;
                    }
                }
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        } else {
            return super.getManagedComponent();
        }

        return null;
    }
}
