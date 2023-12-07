/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.testutils.web;

import com.google.common.base.Throwables;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple Web server for easily testing {@link Servlet}s. This is a simplified
 * version of org.opendaylight.aaa.web.jetty.JettyWebServer, and useful for web
 * tests in infrautils. Other projects than infrautils are free to use this for
 * their tests as well, but may prefer to use the full JettyWebServer instead.
 *
 * @author Michael Vorburger.ch
 */
public final class TestWebServer implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(TestWebServer.class);

    private static final int HTTP_SERVER_IDLE_TIMEOUT = 30000;

    private final int httpPort;
    private final Server server;
    private final ServletContextHandler context;
    private final String testContext;
    private final String host;

    public TestWebServer() throws ServletException {
        this("localhost", 0, "/test");
    }

    public TestWebServer(String host, int httpPort, String testContext) throws ServletException {
        this.server = new Server();
        server.setStopAtShutdown(true);

        ServerConnector http = new ServerConnector(server);
        http.setHost(host);
        http.setPort(httpPort); // 0 = automatically choose free port
        http.setIdleTimeout(HTTP_SERVER_IDLE_TIMEOUT);
        server.addConnector(http);

        ContextHandlerCollection contextHandlerCollection = new ContextHandlerCollection();
        server.setHandler(contextHandlerCollection);

        context = new ServletContextHandler(contextHandlerCollection, testContext, ServletContextHandler.NO_SESSIONS);

        start(server);
        this.httpPort = http.getLocalPort();
        this.testContext = testContext;
        this.host = host;

        LOG.info("Started Jetty-based HTTP web server on port {}", this.httpPort);
    }

    @Override
    @SuppressWarnings({ "checkstyle:IllegalCatch" })
    public void close() throws ServletException {
        LOG.info("Stopping Jetty-based web server...");
        // NB server.stop() will call stop() on all ServletContextHandler/WebAppContext
        try {
            server.stop();
        } catch (Exception e) {
            Throwables.throwIfInstanceOf(e, ServletException.class);
            throw new ServletException("Jetty server stop failed: " + server, e);
        }
        LOG.info("Stopped Jetty-based web server.");
    }

    public String getTestContextURL() {
        return "http://" + host + ":" + httpPort + testContext + "/";
    }

    public void registerServlet(Servlet servlet, String urlPattern) throws ServletException {
        ServletHolder servletHolder = new ServletHolder(servlet);
        servletHolder.setInitOrder(1); // AKA <load-on-startup> 1
        context.addServlet(servletHolder, urlPattern);
        start(context);
    }

    @SuppressWarnings({ "checkstyle:IllegalCatch" })
    private static void start(AbstractLifeCycle lifecycle) throws ServletException {
        try {
            lifecycle.start();
        } catch (Exception e) {
            Throwables.throwIfInstanceOf(e, ServletException.class);
            throw new ServletException("start failed: " + lifecycle, e);
        }
    }
}
