/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.testutils.mockito;

import com.google.errorprone.annotations.Var;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.regex.Pattern;
import org.checkerframework.checker.regex.qual.Regex;

/**
 * Nicer shorter toString() for {@link Method} than it's default.
 *
 * <p>Without modifiers, return type, FQN of class and exceptions; instead with parameter names (if javac -parameters).
 *
 * @author Michael Vorburger
 */
final class MethodExtensions {

    // Intentionally just package local; if this is of interest elsewhere,
    // then move it to org.opendaylight.infrautils.utils instead of keeping it here.

    @Regex
    private static final String PARAM_PATTERN_STR = "\\[\\]$";
    private static final Pattern PARAM_PATTERN = Pattern.compile(PARAM_PATTERN_STR);

    private MethodExtensions() {
    }

    public static String toString(Method method) {
        StringBuilder sb = new StringBuilder();
        sb.append(method.getName());

        // copy/paste from java.lang.reflect.Executable.sharedToGenericString(int, boolean)
        sb.append('(');
        Type[] params = method.getGenericParameterTypes();

        // NEW
        Parameter[] parameters = method.getParameters();
        for (int j = 0; j < params.length; j++) {
            @Var String param = params[j].getTypeName();
            if (method.isVarArgs() && j == params.length - 1) {
                // replace T[] with T...
                param = PARAM_PATTERN.matcher(param).replaceFirst("...");
            }
            sb.append(param);
            // NEW
            if (parameters[j].isNamePresent()) {
                sb.append(' ');
                sb.append(parameters[j].getName());
            }
            // NEW END
            if (j < params.length - 1) {
                // NEW ", " instead of ','
                sb.append(", ");
            }
        }
        sb.append(')');

        return sb.toString();
    }

}
