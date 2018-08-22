/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.diagstatus.web;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.aaa.web.ServletDetails;
import org.opendaylight.aaa.web.WebContext;
import org.opendaylight.aaa.web.WebContextBuilder;
import org.opendaylight.aaa.web.WebContextRegistration;
import org.opendaylight.aaa.web.jetty.JettyWebServer;
import org.opendaylight.infrautils.diagstatus.DiagStatusService;
import org.opendaylight.infrautils.diagstatus.ServiceDescriptor;
import org.opendaylight.infrautils.testutils.Partials;

/**
 * Test for {@link DiagStatusServlet}.
 *
 * @author Michael Vorburger.ch
 */
public class DiagStatusServletTest {

    private static String URL = OsgiWebInitializer.DIAGSTATUS_URL;

    private JettyWebServer webServer;
    private WebContextRegistration webContextRegistration;
    private final TestDiagStatusService testDiagStatusService = Partials.newPartial(TestDiagStatusService.class);

    @Before
    @SuppressWarnings("checkstyle:IllegalThrows") // Jetty throws Throwable
    public void beforeTest() throws Throwable {
        webServer = new JettyWebServer(8282);
        webServer.start();

        WebContextBuilder webContextBuilder = WebContext.builder().contextPath(URL);
        webContextBuilder.addServlet(
            ServletDetails.builder().addUrlPattern("/*").servlet(new DiagStatusServlet(testDiagStatusService)).build());
        webContextRegistration = webServer.registerWebContext(webContextBuilder.build());
    }

    @After
    public void afterTest() throws Exception {
        webServer.stop();
        webContextRegistration.close();
    }

    @Test
    public void testGetWhenOk() throws IOException {
        testDiagStatusService.isOperational = true;
        assertThat(getDiagStatusResponseCode("GET")).isEqualTo(200);
    }

    @Test
    public void testHeadWhenOk() throws IOException {
        testDiagStatusService.isOperational = true;
        assertThat(getDiagStatusResponseCode("HEAD")).isEqualTo(200);
    }

    @Test
    public void testGetWhenNok() throws IOException {
        testDiagStatusService.isOperational = false;
        assertThat(getDiagStatusResponseCode("GET")).isEqualTo(503);
    }

    @Test
    public void testHeadWhenNok() throws IOException {
        testDiagStatusService.isOperational = false;
        assertThat(getDiagStatusResponseCode("HEAD")).isEqualTo(503);
    }

    private int getDiagStatusResponseCode(String httpMethod) throws IOException {
        URL url = new URL(webServer.getBaseURL() + URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(httpMethod);
        return conn.getResponseCode();
    }

    private abstract static class TestDiagStatusService implements DiagStatusService {

        Boolean isOperational;

        @Override
        public Collection<ServiceDescriptor> getAllServiceDescriptors() {
            return Collections.emptyList();
        }

        @Override
        public String getAllServiceDescriptorsAsJSON() {
            return "{}";
        }

        @Override
        public boolean isOperational() {
            return this.isOperational;
        }

    }
}
