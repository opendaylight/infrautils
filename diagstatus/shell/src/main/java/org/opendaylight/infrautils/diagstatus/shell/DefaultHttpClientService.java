/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.diagstatus.shell;

import com.google.errorprone.annotations.Var;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Map;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(configurationPid = "org.ops4j.pax.web")
public final class DefaultHttpClientService implements HttpClientService {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultHttpClientService.class);

    // TODO later unify this with org.opendaylight.infrautils.testutils.web.TestWebClient
    private final HttpClient httpClient = HttpClient.newHttpClient();

    private int httpPort;

    @Override
    public int getHttpPort() {
        return httpPort;
    }

    @Override
    public HttpResponse<String> sendRequest(HttpRequest request) throws IOException, InterruptedException {
        return httpClient.send(request, BodyHandlers.ofString());
    }

    @Activate
    void activate(Map<String, String> properties) {
        String portString = properties.get("org.osgi.service.http.port");
        if (portString != null) {
            @Var int portInt;
            try {
                portInt = Integer.parseInt(portString);
                LOG.info("http port configuration read, http port set to {}", portInt);
            } catch (NumberFormatException e) {
                LOG.warn("Ignored property '{}' that was expected to be an Integer but was not", portString, e);
                portInt = 8181;
            }
            httpPort = portInt;
        }
    }
}
