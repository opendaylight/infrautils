/*
 * Copyright (c) 2019 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.infrautils.jobcoordinator.internal;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class JcServiceStatus implements JcServiceStatusMXBean {


    private final JobCoordinatorImpl jobCoordinatorImpl;

    @Inject
    public JcServiceStatus(JobCoordinatorImpl jobCoordinatorImpl) {

        this.jobCoordinatorImpl = jobCoordinatorImpl;
    }

    @Override
    public Map<String, JcState> jcStatus() {

        return ImmutableMap.copyOf(Maps.transformValues(jobCoordinatorImpl.getJobQueueMap(), value ->
                new JcState(value.getPendingJobCount(),
                        value.getFinishedJobCount(), value.getJobQueueMovingAverageExecutionTime())));
    }
}

