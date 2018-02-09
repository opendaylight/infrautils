/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.metrics.internal;

import static com.google.common.collect.Maps.fromProperties;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.junit.Test;

/**
 * Unit Test for {@link Configuration}.
 *
 * @author Michael Vorburger.ch
 */
public class ConfigurationTest {

    @Test
    public void testDefaults() throws IOException {
        Configuration config = new Configuration(mock(MetricProviderImpl.class), loadDefaultProperties());
        assertThat(config.getThreadsWatcherIntervalMS()).isEqualTo(500);
        assertThat(config.getMaxThreads()).isEqualTo(1000);
        assertThat(config.getFileReporterIntervalSecs()).isEqualTo(0);
    }

    @Test
    // because Checkstyle does not understand :( that error-prone make "config" final:
    @SuppressWarnings("checkstyle:VariableDeclarationUsageDistance")
    public void testConfigurationChange() throws IOException {
        ImmutableMap<String, String> defaultProperties = loadDefaultProperties();
        MetricProviderImpl metricProviderImpl = new MetricProviderImpl();
        Configuration config = new Configuration(metricProviderImpl, defaultProperties);
        assertThat(metricProviderImpl.getThreadsWatcher()).isNotNull();
        assertThat(metricProviderImpl.getThreadsWatcher().getMaxThreads()).isEqualTo(1000);
        assertThat(metricProviderImpl.getThreadsWatcher().getInterval().getNano()).isEqualTo(500000000);
        assertThat(metricProviderImpl.getMetricsFileReporter()).isNull();

        // We now intentionally use properties.put() here instead of config.setFileReporterIntervalSecs(3),
        // just because that is the flow that we actually need to cover in this test...

        Map<String, String> newProperties1 = new HashMap<>(defaultProperties);
        newProperties1.put("fileReporterIntervalSecs", "3");
        config.updateProperties(newProperties1);
        assertThat(metricProviderImpl.getThreadsWatcher()).isNotNull();
        assertThat(metricProviderImpl.getThreadsWatcher().getMaxThreads()).isEqualTo(1000);
        assertThat(metricProviderImpl.getThreadsWatcher().getInterval().getNano()).isEqualTo(500000000);
        assertThat(metricProviderImpl.getMetricsFileReporter()).isNotNull();
        assertThat(metricProviderImpl.getMetricsFileReporter().getInterval().getSeconds()).isEqualTo(3);

        Map<String, String> newProperties2 = new HashMap<>(newProperties1);
        newProperties2.put("threadsWatcherIntervalMS", "0");
        config.updateProperties(newProperties2);
        assertThat(metricProviderImpl.getThreadsWatcher()).isNull();
        assertThat(metricProviderImpl.getMetricsFileReporter()).isNotNull();
        assertThat(metricProviderImpl.getMetricsFileReporter().getInterval().getSeconds()).isEqualTo(3);

        metricProviderImpl.close();
    }

    private ImmutableMap<String, String> loadDefaultProperties() throws IOException {
        Properties defaultProperties = new Properties();
        try (InputStream is = this.getClass().getResourceAsStream("/etc/org.opendaylight.infrautils.metrics.cfg")) {
            defaultProperties.load(is);
        }
        return fromProperties(defaultProperties);
    }
}
