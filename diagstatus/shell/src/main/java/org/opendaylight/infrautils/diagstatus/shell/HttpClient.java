/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.diagstatus.shell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpClient {
    private static final Logger LOG = LoggerFactory.getLogger(HttpClient.class);

    // TODO later unify this with org.opendaylight.infrautils.testutils.web.TestWebClient

    private int httpPort = 8181;

    public HttpClient(Map<String, String> initialProperties) {
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

    public int getHttpPort() {
        return httpPort;
    }

    public HttpResponse sendRequest(HttpRequest request) throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        if (httpclient == null) {
            throw new ClientProtocolException("Couldn't create an HTTP client");
        }
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(request.getTimeout())
                .setConnectTimeout(request.getTimeout())
                .build();
        HttpRequestBase httprequest;
        String method = request.getMethod();
        if (method.equalsIgnoreCase("GET")) {
            httprequest = new HttpGet(request.getUri());
        } else if (method.equalsIgnoreCase("POST")) {
            httprequest = new HttpPost(request.getUri());
            if (request.getEntity() != null) {
                StringEntity sentEntity = new StringEntity(request.getEntity());
                sentEntity.setContentType(request.getContentType());
                ((HttpEntityEnclosingRequestBase) httprequest).setEntity(sentEntity);
            }
        } else if (method.equalsIgnoreCase("PUT")) {
            httprequest = new HttpPut(request.getUri());
            if (request.getEntity() != null) {
                StringEntity sentEntity = new StringEntity(request.getEntity());
                sentEntity.setContentType(request.getContentType());
                ((HttpEntityEnclosingRequestBase) httprequest).setEntity(sentEntity);
            }
        } else if (method.equalsIgnoreCase("DELETE")) {
            httprequest = new HttpDelete(request.getUri());
        } else {
            httpclient.close();
            throw new IllegalArgumentException("This profile class only supports GET, POST, PUT, and DELETE methods");
        }
        httprequest.setConfig(requestConfig);
        // add request headers
        Iterator<String> headerIterator = request.getHeaders().keySet().iterator();
        while (headerIterator.hasNext()) {
            String header = headerIterator.next();
            Iterator<String> valueIterator = request.getHeaders().get(header).iterator();
            while (valueIterator.hasNext()) {
                httprequest.addHeader(header, valueIterator.next());
            }
        }
        CloseableHttpResponse response = httpclient.execute(httprequest);
        try {
            int httpResponseCode = response.getStatusLine().getStatusCode();
            HashMap<String, List<String>> headerMap = new HashMap<>();
           // copy response headers
            HeaderIterator it = response.headerIterator();
            while (it.hasNext()) {
                Header nextHeader = it.nextHeader();
                String name = nextHeader.getName();
                String value = nextHeader.getValue();
                if (headerMap.containsKey(name)) {
                    headerMap.get(name).add(value);
                } else {
                    List<String> list = new ArrayList<>();
                    list.add(value);
                    headerMap.put(name, list);
                }
            }
            if (httpResponseCode > 299) {
                return new HttpResponse(httpResponseCode, response.getStatusLine().getReasonPhrase(), headerMap);
            }
            Optional<HttpEntity> receivedEntity = Optional.ofNullable(response.getEntity());
            String httpBody = receivedEntity.isPresent()
                    ? EntityUtils.toString(receivedEntity.get()) : null;
            return new HttpResponse(response.getStatusLine().getStatusCode(), httpBody, headerMap);
        } finally {
            response.close();
        }
    }
}
