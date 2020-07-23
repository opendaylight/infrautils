/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.diagstatus.shell;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public interface HttpClientService {
    HttpResponse<String> sendRequest(HttpRequest request) throws IOException, InterruptedException;

    int getHttpPort();
}
