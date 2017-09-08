/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.infrautils.diagstatus.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Utility for JMX Remote operations.
 *
 * @author Faseela K
 */
public class JMXRemoteOperationsUtil {

    private static final Logger LOG = LoggerFactory.getLogger(JMXRemoteOperationsUtil.class);
    private static final String JMX_REST_HTTP_OPERATION = "GET";
    private static final String JMX_REST_HTTP_JOLOKIA_BASE_URI = "/jolokia/exec/";
    private static final String JMX_REST_HTTP_AUTH_UNAME_PWD = "admin:admin";
    private static final String JMX_REST_HTTP_PORT = "8181";
    private static final int TIMEOUT = 5000;

    public static String invokeRemoteJMXOperationREST(String host, String mbeanName, String operation,
                                                      List<String> operationArgs) throws Exception {
        // initialize response code to indicate error
        String restUrl = buildJMXRemoteRESTUrlWithMultipleArg(host, mbeanName, operation, operationArgs);
        String respStr = "ERROR accessing JMX URL - " + restUrl;
        LOG.info("invokeRemoteJMXOperationRESTWithArg : REST JMX URL - {}", restUrl);
        HTTPRequest request = new HTTPRequest();
        request.setUri(restUrl);
        request.setMethod(JMX_REST_HTTP_OPERATION);
        request.setTimeout(TIMEOUT);
        // To compensate for respond
        // within default TIMEOUT during
        // IT so setting an indefinite
        // TIMEOUT till the issue is
        // sorted out
        Map<String, List<String>> headers = new HashMap<String, List<String>>();
        String authString = JMX_REST_HTTP_AUTH_UNAME_PWD;
        byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
        String authStringEnc = new String(authEncBytes);
        List<String> header = new ArrayList<String>();
        header.add("Basic " + authStringEnc);
        headers.put("Authorization", header);
        header = new ArrayList<String>();
        header.add("application/json");
        headers.put("Accept", header);
        request.setHeaders(headers);
        request.setContentType("application/json");
        LOG.debug("invokeRemoteJMXOperationRESTWithArg : sending request ... ");
        HTTPResponse response = null;
        response = HTTPClient.sendRequest(request);
        LOG.debug("invokeRemoteJMXOperationREST : response received ... ");
        // Response code for success should be 2xx
        int httpResponseCode = response.getStatus();
        LOG.debug("invokeRemoteJMXOperationRESTWithArg : HTTP Response code is - {}", httpResponseCode);
        if (httpResponseCode > 299) {
            LOG.error("invokeRemoteJMXOperationRESTWithArg : Non-200 HTTP Response code is  {} for URL {}",
                    httpResponseCode, restUrl);
            return respStr + " HTTP Response Code : " + Integer.toString(httpResponseCode);
        }
        LOG.debug("invokeRemoteJMXOperationRESTWithArg : HTTP Response is - {} for URL {}",
                response.getEntity(), restUrl);
        respStr = response.getEntity();
        return respStr;
    }

    private static String buildJMXRemoteRESTUrlWithMultipleArg(String host, String mbeanName, String operation,
                                                               List<String> args) {
        StringBuilder jmxUrl = new StringBuilder();
        jmxUrl.append("http://" + host + ":" + JMX_REST_HTTP_PORT + JMX_REST_HTTP_JOLOKIA_BASE_URI + mbeanName + "/"
                + operation);
        for (String str : args) {
            jmxUrl.append("/").append(str);
        }
        return jmxUrl.toString();
    }

    static String execRemoteJMXOperation(String jmxServiceObjectName, String invokeCommand,
                                         String ipAddress, List args) throws Exception {
        LOG.debug("runClusterwideCommand : Peer cluster address : {}", ipAddress);
        String operationResult = JMXRemoteOperationsUtil.invokeRemoteJMXOperationREST(ipAddress,
                jmxServiceObjectName, invokeCommand, args);
        LOG.debug("runClusterwideCommand : Remote JMX Response successful");
        return operationResult;
    }
}




