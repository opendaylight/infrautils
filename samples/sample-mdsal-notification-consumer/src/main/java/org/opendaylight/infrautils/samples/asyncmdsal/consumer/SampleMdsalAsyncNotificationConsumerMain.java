/*
 * Copyright (c) 2016 Hewlett Packard Enterprise, Co. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.samples.asyncmdsal.consumer;

import java.lang.reflect.InvocationTargetException;
import org.opendaylight.controller.md.sal.binding.api.NotificationService;
import org.opendaylight.infrautils.async.impl.AsyncMethod;
import org.opendaylight.infrautils.async.impl.AsyncUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.infrautils.asyncsample.rev160703.AsyncNotificationExample;
import org.opendaylight.yang.gen.v1.urn.opendaylight.infrautils.asyncsample.rev160703.AsyncsampleListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.infrautils.asyncsample.rev160703.SyncNotificationExample;

/*
 * Copyright (c) 2016 Hewlett Packard Enterprise, Co. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
public class SampleMdsalAsyncNotificationConsumerMain implements AsyncsampleListener {

    public void init() {

    }

    public void setNotificationService(NotificationService notificationService)
            throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        System.out.println("Consumer Registered as Notification Listener!");
        AsyncsampleListener proxy = (AsyncsampleListener) AsyncUtil.makeAsync(this, AsyncsampleListener.class);
        notificationService.registerNotificationListener(proxy);
    }

    @Override
    public void onSyncNotificationExample(SyncNotificationExample notification) {
        System.out.println("Sync Notification Received on Thread: " + Thread.currentThread().getName());
        System.out.println(
                "Sync notification handling will hog the thread " + Thread.currentThread().getName()
                        + " for 15 seconds");
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    @AsyncMethod
    public void onAsyncNotificationExample(AsyncNotificationExample notification) {
        System.out.println("Async Notification Received on Thread: " + Thread.currentThread().getName());
        System.out.println(
                "Async notification handling will hog the thread " + Thread.currentThread().getName()
                        + " for 15 seconds");
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
