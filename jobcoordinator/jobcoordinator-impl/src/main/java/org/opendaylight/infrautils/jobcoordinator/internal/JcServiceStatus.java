/*
 * Copyright (c) 2019 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.jobcoordinator.internal;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.MapMaker;
import com.google.errorprone.annotations.Var;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class JcServiceStatus implements JcServiceStatusMXBean {
    private static final Logger LOG = LoggerFactory.getLogger(JcServiceStatus.class);
    // Keep track of classes we have warned about. Under normal circumstances we will never touch this
    private static final Map<Class<?>, Boolean> WARNED_KEY_CLASSES =
        new MapMaker().concurrencyLevel(1).weakKeys().makeMap();

    private static final ObjectName OBJECT_NAME;

    static {
        try {
            OBJECT_NAME =
                    ObjectName.getInstance("org.opendaylight.infrautils.jobcoordinator.internal:type=JcServiceStatus");
        } catch (MalformedObjectNameException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final ConcurrentMap<Object, ? extends JobQueue> jobQueueMap;
    private final @Nullable MBeanServer mbeanServer;

    @SuppressFBWarnings(value = "NP_STORE_INTO_NONNULL_FIELD", justification = "SpotBugs does not grok @Nullable")
    JcServiceStatus(ConcurrentMap<Object, ? extends JobQueue> jobQueueMap) {
        this.jobQueueMap = jobQueueMap;

        @Var MBeanServer srv = null;
        try {
            srv = ManagementFactory.getPlatformMBeanServer();
        } catch (SecurityException e) {
            LOG.warn("Failed to acquire platform MBean server, continuing JMX", e);
        }
        mbeanServer = srv;
    }

    @Override
    public ImmutableMap<String, JcState> jcStatus() {
        Map<String, JcState> strKeyed = new HashMap<>();
        Map<Object, JcState> objKeyed = new HashMap<>();

        for (Entry<Object, ? extends JobQueue> entry : jobQueueMap.entrySet()) {
            JobQueue queue = entry.getValue();
            JcState state = new JcState(queue.getPendingJobCount(), queue.getFinishedJobCount(),
                queue.getJobQueueMovingAverageExecutionTime());
            Object key = entry.getKey();
            if (key instanceof String) {
                strKeyed.put((String) key, state);
            } else {
                objKeyed.put(key, state);
            }
        }

        if (!objKeyed.isEmpty()) {
            // This is unfortunate: we have to encode Objects into Strings, and hope the toString() contract matches
            // equals() in that distinct objects produce distinct values. For now just issue warnings if things go
            // south.
            for (Entry<Object, JcState> entry : objKeyed.entrySet()) {
                Object key = entry.getKey();
                String str = key.toString();
                if (strKeyed.putIfAbsent(str, entry.getValue()) != null) {
                    Class<?> keyClass = key.getClass();
                    if (WARNED_KEY_CLASSES.putIfAbsent(keyClass, Boolean.TRUE) == null) {
                        LOG.warn("Queue key {} resulted in conflicting string \"{}\", please fix the implemetation",
                            keyClass, str);
                    }
                }
            }
        }

        return ImmutableMap.copyOf(strKeyed);
    }

    void register() {
        if (mbeanServer != null) {
            try {
                mbeanServer.registerMBean(this, OBJECT_NAME);
            } catch (NotCompliantMBeanException e) {
                throw new IllegalStateException("Unexpected registration failure", e);
            } catch (InstanceAlreadyExistsException | MBeanRegistrationException e) {
                LOG.warn("Failed to register bean, continuing without it", e);
            }
        }
    }

    void unregister() {
        if (mbeanServer != null) {
            try {
                mbeanServer.unregisterMBean(OBJECT_NAME);
            } catch (MBeanRegistrationException | InstanceNotFoundException e) {
                LOG.warn("Failed to unregister bean, cleanup might be incomplete", e);
            }
        }
    }
}

