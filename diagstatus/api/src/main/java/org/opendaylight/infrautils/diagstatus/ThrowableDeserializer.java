/*
 * Copyright (c) 2023 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.diagstatus;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;

@SuppressWarnings("Var")
public class ThrowableDeserializer implements JsonDeserializer<Throwable> {

    @Override
    public Throwable deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)
            throws JsonParseException {
        if (!json.isJsonObject()) {
            throw new JsonParseException("Invalid JSON format for Throwable");
        }

        final JsonObject jsonObject = json.getAsJsonObject();
        final String message = jsonObject.get("message").getAsString();
        final String className = jsonObject.get("class").getAsString();

        // Create a new instance of the throwable class
        final Throwable throwable;
        try {
            final Class<?> throwableClass = Class.forName(className);
            throwable = (Throwable) throwableClass.getDeclaredConstructor(String.class).newInstance(message);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException
                 | InvocationTargetException e) {
            throw new JsonParseException("Failed to create Throwable instance", e);
        }

        // Deserialize the stack trace
        if (jsonObject.has("stackTrace") && jsonObject.get("stackTrace").isJsonArray()) {
            final JsonArray stackTraceArray = jsonObject.get("stackTrace").getAsJsonArray();
            final StackTraceElement[] stackTrace = new StackTraceElement[stackTraceArray.size()];
            for (int i = 0; i < stackTraceArray.size(); i++) {
                final JsonObject stackTraceObject = stackTraceArray.get(i).getAsJsonObject();
                final String classNameTrace = stackTraceObject.get("className").getAsString();
                final String fileName = stackTraceObject.get("fileName").getAsString();
                final String methodName = stackTraceObject.get("methodName").getAsString();
                final int lineNumber = stackTraceObject.get("lineNumber").getAsInt();
                stackTrace[i] = new StackTraceElement(classNameTrace, methodName, fileName, lineNumber);
            }
            throwable.setStackTrace(stackTrace);
        }

        // Deserialize the cause recursively
        if (jsonObject.has("cause") && jsonObject.get("cause").isJsonObject()) {
            final JsonElement causeElement = jsonObject.get("cause");
            final Throwable cause = context.deserialize(causeElement, Throwable.class);
            throwable.initCause(cause);
        }

        return throwable;
    }
}
