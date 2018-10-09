/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.diagstatus.spi;

import static java.util.Collections.emptyList;

import com.google.common.net.InetAddresses;
import java.net.InetAddress;
import java.util.List;
import org.opendaylight.infrautils.diagstatus.ClusterMemberInfo;

/**
 * ClusterMemberInfo implementation when there is no cluster, suitable e.g. for tests.
 *
 * @author Michael Vorburger
 */
public class NoClusterMemberInfo implements ClusterMemberInfo {

    private final InetAddress selfInetAddress;

    public NoClusterMemberInfo() {
        this(InetAddresses.forString("127.0.0.1"));
    }

    public NoClusterMemberInfo(InetAddress selfInetAddress) {
        this.selfInetAddress = selfInetAddress;
    }

    @Override
    public InetAddress getSelfAddress() {
        return selfInetAddress;
    }

    @Override
    public List<InetAddress> getClusterMembers() {
        return emptyList();
    }

    @Override
    public boolean isLocalAddress(InetAddress isLocalIpAddress) {
        return selfInetAddress.equals(isLocalIpAddress);
    }
}
