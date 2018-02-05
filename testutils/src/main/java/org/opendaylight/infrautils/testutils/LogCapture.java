/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.testutils;

import static java.util.Objects.requireNonNull;

import com.google.errorprone.annotations.Var;
import javax.annotation.Nullable;

/**
 * Captured log statement.
 *
 * @author Michael Vorburger.ch
 */
public class LogCapture {

    public enum Level { ERROR }

    private final Level level;
    private final String message;
    private final @Nullable Throwable cause;

    public LogCapture(Level level, String message, @Nullable Throwable cause) {
        this.level = requireNonNull(level, "level");
        this.message = requireNonNull(message);
        this.cause = cause;
    }

//    public Level getLevel() {
//        return level;
//    }

    public String getMessage() {
        return message;
    }

    public @Nullable Throwable getCause() {
        return cause;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        @Var int result = 1;
        result = prime * result + (cause == null ? 0 : cause.hashCode());
        result = prime * result + level.hashCode();
        result = prime * result + message.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        LogCapture other = (LogCapture) obj;
        if (cause == null) {
            if (other.cause != null) {
                return false;
            }
        } else if (!cause.equals(other.cause)) {
            return false;
        }
        if (level != other.level) {
            return false;
        }
        if (!message.equals(other.message)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "LogCapture [level=" + level + ", message=" + message + ", cause=" + cause + "]";
    }

}
