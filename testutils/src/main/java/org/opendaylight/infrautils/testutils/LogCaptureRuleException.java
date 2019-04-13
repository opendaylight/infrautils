/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.testutils;

import java.util.Optional;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Exception thrown by the {@link LogCaptureRule}.
 *
 * @author Michael Vorburger.ch
 */
public class LogCaptureRuleException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public LogCaptureRuleException(String lastLoggedErrorMessage) {
        super(lastLoggedErrorMessage);
    }

    public LogCaptureRuleException(String lastLoggedErrorMessage, @Nullable Throwable lastLoggedErrorThrowable) {
        super(lastLoggedErrorMessage, lastLoggedErrorThrowable);
    }

    public LogCaptureRuleException(String lastLoggedErrorMessage, @Nullable Throwable lastLoggedErrorThrowable,
            @Nullable Throwable testFailingThrowable) {
        this(lastLoggedErrorMessage, lastLoggedErrorThrowable);
        if (testFailingThrowable != null) {
            addSuppressed(testFailingThrowable);
        }
    }

    public Optional<Throwable> getLastLoggedThrowable() {
        return Optional.ofNullable(getCause());
    }

    public Optional<Throwable> getTestFailingThrowable() {
        if (getSuppressed().length == 0) {
            return Optional.empty();
        }
        return Optional.of(getSuppressed()[0]);
    }

    /**
     * This is the same as {@link #getLastLoggedThrowable()}.
     */
    @Override
    public synchronized Throwable getCause() {
        return super.getCause();
    }
}
