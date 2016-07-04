/*
 * Copyright (c) 2016 Hewlett Packard Enterprise, Co. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.samples.asyncmdsal.provider;

import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.infrautils.asyncsample.rev160703.AsyncNotificationExampleBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.infrautils.asyncsample.rev160703.SyncNotificationExampleBuilder;

public class SampleNotificationProvider {
    private NotificationPublishService notificationService;

    public SampleNotificationProvider(NotificationPublishService notificationService) {
        this.notificationService = notificationService;
    }

    public void createAsyncNotification() throws InterruptedException {
        notificationService.putNotification(new AsyncNotificationExampleBuilder().setAsyncValue("Async Value").build());
    }

    public void createSyncNotification() throws InterruptedException {
        notificationService.putNotification(new SyncNotificationExampleBuilder().setSyncValue("Sync Value").build());
    }
}
