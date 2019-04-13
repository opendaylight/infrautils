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

// This is supposed to be immutable, but contains (and leaks) potentially-mutable collections
public final class HttpResponse {
    private final Integer status;        // response status
    private final String  body;        // response entity
    private final Map<String, List<String>> headers;  // http header values

    public HttpResponse(Integer status, String body, Map<String, List<String>> headers) {
        this.status = status;
        this.body = body;
        this.headers = headers;
    }

    public Integer getStatus() {
        return status;
    }

    public String getBody() {
        return body;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }
}
