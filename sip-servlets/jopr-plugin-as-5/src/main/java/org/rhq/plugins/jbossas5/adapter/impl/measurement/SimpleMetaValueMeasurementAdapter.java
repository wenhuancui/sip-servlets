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

package org.rhq.plugins.jbossas5.adapter.impl.measurement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.domain.measurement.DataType;
import org.rhq.core.domain.measurement.MeasurementDataNumeric;
import org.rhq.core.domain.measurement.MeasurementDataTrait;
import org.rhq.core.domain.measurement.MeasurementDefinition;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.plugins.jbossas5.adapter.api.MeasurementAdapter;

import org.jboss.metatype.api.values.MetaValue;
import org.jboss.metatype.api.values.SimpleValueSupport;

/**
 * TODO
 */
public class SimpleMetaValueMeasurementAdapter implements MeasurementAdapter {
    private final Log LOG = LogFactory.getLog(SimpleMetaValueMeasurementAdapter.class);

    public void setMeasurementData(MeasurementReport report, MetaValue metaValue, MeasurementScheduleRequest request,
        MeasurementDefinition measurementDefinition) {
        SimpleValueSupport simpleValue = (SimpleValueSupport) metaValue;
        DataType dataType = measurementDefinition.getDataType();
        switch (dataType) {
        case MEASUREMENT:
            try {
                MeasurementDataNumeric dataNumeric = new MeasurementDataNumeric(request, new Double(simpleValue
                    .getValue().toString()));
                report.addData(dataNumeric);
            } catch (NumberFormatException e) {
                LOG.warn("Measurement request: " + request.getName()
                    + " did not return a numeric value from the Profile Service", e);
            }
            break;
        case TRAIT:
            MeasurementDataTrait dataTrait = new MeasurementDataTrait(request, String.valueOf(simpleValue.getValue()));
            report.addData(dataTrait);
            break;
        default:
            throw new IllegalStateException("Unsupported measurement data type: " + dataType);

        }
    }
}
