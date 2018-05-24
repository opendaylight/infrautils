/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.metrics.internal;

import com.codahale.metrics.jmx.DefaultObjectNameFactory;
import javax.management.ObjectName;

/**
 * Custom transformer of String Metric ID to JMX ObjectName.
 *
 * @author Michael Vorburger.ch
 */
public class CustomObjectNameFactory extends DefaultObjectNameFactory { // TODO implements ObjectNameFactory {

    // TODO tune the conversion of label name/values to appropriate JMX ObjectName.. perhaps using some <> and [] ?

    @Override
    public ObjectName createName(String type, String domain, String name) {
        return super.createName(type, domain, name);
    }

}
