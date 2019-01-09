/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.testutils.web;

import com.google.common.io.CharStreams;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * HTTP Client.
 *
 * @author Michael Vorburger.ch
 */
public class TestWebClient {

    // TODO later unify this with org.opendaylight.infrautils.diagstatus.shell.HttpClient

    public enum Method {
        GET, POST, HEAD, OPTIONS, PUT, DELETE, TRACE
    }

    private final String baseURL;

    public TestWebClient(TestWebServer webServer) {
        this(webServer.getTestContextURL());
    }

    public TestWebClient(String baseURL) {
        this.baseURL = baseURL;
    }

    public HttpResponse request(Method httpMethod, String path) throws IOException {
        URL url = new URL(baseURL + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(httpMethod.name());

        String body;
        try (InputStream inputStream = conn.getInputStream()) {
            // The hard-coded ASCII here is strictly speaking wrong of course
            // (should interpret header from reply), but good enough for a test utility.
            try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.US_ASCII)) {
                body = CharStreams.toString(reader);
            }
        }

        return new HttpResponse(conn.getResponseCode(), body);
    }
}
