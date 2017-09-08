/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.infrautils.diagstatus.util;

import java.util.List;
import java.util.Map;

/**
 * HTTP Request.
 *
 * @author Faseela K
 */
class HTTPRequest {
    // the HTTP method to use: currently GET, POST, PUT, and DELETE are
    // supported
    private String method;
    // the full URI to send to (including protocol)
    private String uri;
    // the entity body to send
    private String entity;
    // additional headers (separate from content-type) to include in the request
    private Map<String, List<String>> headers;
    // timeout in milliseconds. Defaults to 3 seconds
    private int timeout;
    // content type to set. Defaults to application/json
    private String contentType;

    HTTPRequest() {
        timeout = 3000;
        contentType = "application/json";
    }

    String getMethod() {
        return method;
    }

    void setMethod(String method) {
        this.method = method;
    }

    String getUri() {
        return uri;
    }

    void setUri(String uri) {
        this.uri = uri;
    }

    String getEntity() {
        return entity;
    }

    void setEntity(String entity) {
        this.entity = entity;
    }

    Map<String, List<String>> getHeaders() {
        return headers;
    }

    void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }

    int getTimeout() {
        return timeout;
    }

    void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    String getContentType() {
        return contentType;
    }

    void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
