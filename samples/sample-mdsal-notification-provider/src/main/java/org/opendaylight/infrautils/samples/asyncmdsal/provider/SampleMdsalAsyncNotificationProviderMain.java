/*
 * Copyright (c) 2016 Hewlett Packard Enterprise, Co. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.samples.asyncmdsal.provider;

import java.lang.reflect.InvocationTargetException;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;

public class SampleMdsalAsyncNotificationProviderMain {

    private NotificationPublishService notificationPublishService;

    public void init()
            throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            InterruptedException {
        System.out.println("\nStarted Async MD-SAL Notification Provider!");
        System.out.println("Will send notifications in 10 seconds!");
        Thread.sleep(10000);

        SampleNotificationProvider rpcMDSALProvider = new SampleNotificationProvider(notificationPublishService);
        System.out.println("Creating first async notification");
        rpcMDSALProvider.createAsyncNotification();
        System.out.println("Creating second async notification");
        rpcMDSALProvider.createAsyncNotification();
        System.out.println("Creating first sync notification");
        rpcMDSALProvider.createSyncNotification();
        System.out.println("Creating second sync notification");
        rpcMDSALProvider.createSyncNotification();
    }

    public void setNotificationPublishService(NotificationPublishService notificationService) {
        this.notificationPublishService = notificationService;

    }
}
