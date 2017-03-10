/*
 * Copyright (c) 2016 Hewlett Packard Enterprise, Co. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.infrautils.async.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.opendaylight.infrautils.async.api.IAsyncConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncConfig implements IAsyncConfig {
    protected static final Logger LOG = LoggerFactory.getLogger(AsyncConfig.class);

    private static final String CONFIG_FILE_PATH = "etc/org.opendaylight.async.cfg";
    private final Properties configuration;

    public AsyncConfig() {
        File configFile = new File(CONFIG_FILE_PATH).getAbsoluteFile();
        configuration = new Properties();
        try (FileInputStream inStream = new FileInputStream(configFile)) {
            configuration.load(inStream);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read: " + configFile, e);
            // LOG.error("Cannot read " + configFile, e);
        }
    }

    @Override
    public String getString(String path) {
        LOG.debug("ConfigurationManager.get {}", path);
        return configuration.getProperty(path);
    }

    @Override
    public String getString(String path, String defaultValue) {
        LOG.debug("ConfigurationManager.get {} default {}", path, defaultValue);
        return configuration.getProperty(path, defaultValue);
    }

    @Override
    public Integer getInt(String path) {
        String stringValue = getString(path);
        if (stringValue == null) {
            return null;
        }

        return Integer.valueOf(stringValue);
    }

    @Override
    public Integer getInt(String path, int defaultValue) {
        return Integer.valueOf(getString(path, String.valueOf(defaultValue)));
    }
}
