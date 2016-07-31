/*
 * Copyright (c) 2016 Hewlett Packard Enterprise, Co. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.infrautils.samples.async;

import org.opendaylight.infrautils.async.impl.AsyncMethod;

public interface IOtherRpcImplementor {
    
    @AsyncMethod
    public void anotherAsyncRpcMethod();
}
