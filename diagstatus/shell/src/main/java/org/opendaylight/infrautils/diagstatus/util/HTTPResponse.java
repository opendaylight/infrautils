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
 * HTTP Response.
 *
 * @author Faseela K
 */
public class HTTPResponse {
    private Integer status;        // response status
    private String  entity;        // response entity
    private Map<String, List<String>> headers;  // http header values

    HTTPResponse() {
    }

    Integer getStatus() {
        return status;
    }

    void setStatus(Integer status) {
        this.status = status;
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

    void setHeaders(Map<String, List<String>> map) {
        this.headers = map;
    }
}
