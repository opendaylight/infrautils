/*
 * Copyright (c) 2016 Hewlett Packard Enterprise, Co. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.infrautils.async.impl;

public interface IAsyncConfig {
    public static final String CONFIG_POOL_NAME = "pool.name";
    public static final String CONFIG_PROXIES = "/proxies/";

    String getString(String path, String defaultValue);

    String getString(String path);

    Integer getInt(String path, int defaultValue);

    Integer getInt(String path);
}
