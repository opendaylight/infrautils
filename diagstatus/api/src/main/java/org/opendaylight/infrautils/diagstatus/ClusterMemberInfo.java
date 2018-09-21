/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.diagstatus;

import java.net.InetAddress;
import java.util.List;

/**
 * ODL cluster information.
 *
 * @author Michael Vorburger.ch
 */
public interface ClusterMemberInfo {

    // TODO change String to InetAddress in both getSelfAddress() and getClusterMembers()

    String getSelfAddress();

    List<String> getClusterMembers();

    default boolean isLocalAddress(String ipAddress) {
        // TODO also checking if ipAddress === getSelfAddress() would seem to make sense here?
        return ipAddress.equals(InetAddress.getLoopbackAddress().getHostAddress());
    }
}
