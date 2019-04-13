/*
 * Copyright (c) 2019 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.testutils.web;

import com.google.errorprone.annotations.Immutable;

/**
 * HTTP Response.
 *
 * @author Michael Vorburger.ch
 */
@Immutable
public class HttpResponse {

    // TODO later unify this with org.opendaylight.infrautils.diagstatus.shell.HttpResponse

    private final int status;
    private final String body;

    // package-local
    HttpResponse(int status, String body) {
        this.status = status;
        this.body = body;
    }

    public int getStatus() {
        return status;
    }

    public String getBody() {
        return body;
    }
}
