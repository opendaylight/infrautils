/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.testutils.web;

import static com.google.common.truth.Truth.assertThat;
import static org.opendaylight.infrautils.testutils.Asserts.assertThrows;

import java.io.FileNotFoundException;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Test;
import org.opendaylight.infrautils.testutils.web.TestWebClient.Method;

/**
 * Test for {@link TestWebServer}.
 *
 * @author Michael Vorburger.ch
 */
public class TestWebServerTest {

    // This class is a simpler version of code inspired by org.opendaylight.aaa.web.test

    @Test
    public void testWebServerWithoutServlet() throws ServletException, IOException {
        try (TestWebServer webServer = new TestWebServer()) {
            assertThrows(FileNotFoundException.class, () -> checkTestServlet(webServer, "nada"));
        }
    }

    @Test
    public void testWebServerWithOneServlet() throws ServletException, IOException {
        try (TestWebServer webServer = new TestWebServer()) {
            webServer.registerServlet(new TestServlet(), "/testServlet");
            checkTestServlet(webServer, "testServlet");
        }
    }

    @Test
    public void testWebServerWithTwosServlets() throws ServletException, IOException {
        try (TestWebServer webServer = new TestWebServer()) {
            webServer.registerServlet(new TestServlet(), "/firstServlet");
            webServer.registerServlet(new TestServlet(), "/secondServlet");
            checkTestServlet(webServer, "secondServlet");
        }
    }

    private static void checkTestServlet(TestWebServer webServer, String urlSuffix) throws IOException {
        String body = new TestWebClient(webServer).request(Method.GET, urlSuffix).getBody();
        assertThat(body).startsWith("hello, world");
    }

    @SuppressWarnings("serial")
    private static class TestServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse response) throws IOException {
            response.getOutputStream().println("hello, world");
        }
    }
}
