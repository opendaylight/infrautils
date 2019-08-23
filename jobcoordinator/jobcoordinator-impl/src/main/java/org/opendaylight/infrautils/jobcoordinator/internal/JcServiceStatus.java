/*
 * Copyright (c) 2019 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.infrautils.jobcoordinator.internal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
        Map<String,JcState> displayMapCreate = new ConcurrentHashMap<>();
        Map<String, JobQueue>  jobQueueMap = jobCoordinatorImpl.getJobQueueMap();
        for (Map.Entry<String,JobQueue> entry : jobQueueMap.entrySet()) {
            displayMapCreate.put(entry.getKey(), new JcState(entry.getValue().getPendingJobCount(),
                    entry.getValue().getFinishedJobCount(), entry.getValue().getJobQueueMovingAverage()));
        }
        return displayMapCreate;
    }
}
