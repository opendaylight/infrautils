/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.diagstatus.shell;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultHttpClientService implements HttpClientService {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultHttpClientService.class);

    // TODO later unify this with org.opendaylight.infrautils.testutils.web.TestWebClient
    private final HttpClient httpClient = HttpClient.newHttpClient();

    private int httpPort = 8181;

    public DefaultHttpClientService(Map<String, String> initialProperties) {
        String propertyValueAsString = initialProperties.get("org.osgi.service.http.port");
        if (propertyValueAsString != null) {
            try {
                Integer propertyValueAsInt = Integer.parseInt(propertyValueAsString);
                LOG.info("http port configuration read, http port set to {}", propertyValueAsInt);
                httpPort = propertyValueAsInt;
            } catch (NumberFormatException nfe) {
                LOG.warn("Ignored property '{}' that was expected to be an Integer but was not",
                        propertyValueAsString, nfe);
            }
        }
    }

    @Override
    public int getHttpPort() {
        return httpPort;
    }

    @Override
    public HttpResponse<String> sendRequest(HttpRequest request) throws IOException, InterruptedException {
        return httpClient.send(request, BodyHandlers.ofString());
    }
}
