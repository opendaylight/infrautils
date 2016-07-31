/*
 * Copyright (c) 2016 Hewlett Packard Enterprise, Co. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.infrautils.async.impl;

import java.util.Arrays;
import java.util.Queue;

import org.opendaylight.infrautils.async.api.IWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueueReaderWorker implements IWorker {
    protected static final Logger logger = LoggerFactory.getLogger(QueueReaderWorker.class);

    private Queue<MethodCallMessage> queue;
    private Object theInstance;

    public QueueReaderWorker(Queue<MethodCallMessage> queue, Object theInstance) {
        this.queue = queue;
        this.theInstance = theInstance;
    }

    public void work() {
        MethodCallMessage msg = null;
        msg = queue.poll();
        if (msg == null) {
            return;
        }

        if (logger.isTraceEnabled()) {
            logger.trace("invoking method {} on {} with parameters {}", msg.method,
                    theInstance.getClass().getSimpleName(), Arrays.toString(msg.args));
        }

        try {
            msg.method.invoke(theInstance, msg.args);
        } catch (Throwable t) {
            logger.error(t.getMessage() + " while executing " + msg.method + " on "
                    + theInstance.getClass().getSimpleName());
        }
    }
}
