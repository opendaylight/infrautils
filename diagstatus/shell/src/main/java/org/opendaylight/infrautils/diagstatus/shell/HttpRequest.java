/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.infrautils.diagstatus.shell;

import java.util.List;
import java.util.Map;

public class HttpRequest {
    // the HTTP method to use: currently GET, POST, PUT, and DELETE are
    // supported
    private String method;
    // the full URI to send to (including protocol)
    private String uri;
    // the entity body to send
    private String entity;
    // additional headers (separate from content-type) to include in the request
    private Map<String, List<String>> headers;
    // HTTP_TIMEOUT in milliseconds. Defaults to 3 seconds
    private int timeout = 3000;
    // content type to set. Defaults to application/json
    private String contentType = "application/json";

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}