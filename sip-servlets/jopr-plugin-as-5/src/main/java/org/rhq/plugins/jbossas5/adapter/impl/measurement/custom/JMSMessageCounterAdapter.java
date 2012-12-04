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

package org.rhq.plugins.jbossas5.adapter.impl.measurement.custom;

import org.rhq.core.domain.measurement.DataType;
import org.rhq.core.domain.measurement.MeasurementDataNumeric;
import org.rhq.core.domain.measurement.MeasurementDataTrait;
import org.rhq.core.domain.measurement.MeasurementDefinition;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.plugins.jbossas5.adapter.api.MeasurementAdapter;

import org.jboss.metatype.api.values.CompositeValueSupport;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.metatype.api.values.SimpleValueSupport;

public class JMSMessageCounterAdapter implements MeasurementAdapter {
    public void setMeasurementData(MeasurementReport report, MetaValue metaValue, MeasurementScheduleRequest request,
        MeasurementDefinition measurementDefinition) {
        // TODO: fix this
        CompositeValueSupport compositeValue = (CompositeValueSupport) metaValue;
        DataType dataType = measurementDefinition.getDataType();
        String metricName = request.getName();
        if (dataType.equals(DataType.MEASUREMENT)) {
            //@todo break out the getting the value out of the ValueSupport object
            MeasurementDataNumeric dataNumeric = new MeasurementDataNumeric(request,
                (Double) ((SimpleValueSupport) (compositeValue.get(metricName))).getValue());
            report.addData(dataNumeric);
        } else if (dataType.equals(DataType.TRAIT)) {
            //@todo break out the getting the value out of the ValueSupport object
            MeasurementDataTrait dataTrait = new MeasurementDataTrait(request,
                (String) ((SimpleValueSupport) (compositeValue.get(metricName))).getValue());
            report.addData(dataTrait);
        }
    }
}
