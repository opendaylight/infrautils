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

public class ThrowableDeserializer implements JsonDeserializer<Throwable> {
    @Override
    public Throwable deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        if (!json.isJsonObject()) {
            throw new JsonParseException("Invalid JSON format for Throwable");
        }

        JsonObject jsonObject = json.getAsJsonObject();
        String message = jsonObject.get("message").getAsString();
        String className = jsonObject.get("class").getAsString();

        // Create a new instance of the throwable class
        Throwable throwable;
        try {
            Class<?> throwableClass = Class.forName(className);
            throwable = (Throwable) throwableClass.getDeclaredConstructor(String.class).newInstance(message);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException
                 | InvocationTargetException e) {
            throw new JsonParseException("Failed to create Throwable instance", e);
        }

        // Deserialize the stack trace
        if (jsonObject.has("stackTrace") && jsonObject.get("stackTrace").isJsonArray()) {
            JsonArray stackTraceArray = jsonObject.get("stackTrace").getAsJsonArray();
            StackTraceElement[] stackTrace = new StackTraceElement[stackTraceArray.size()];
            for (int i = 0; i < stackTraceArray.size(); i++) {
                JsonObject stackTraceObject = stackTraceArray.get(i).getAsJsonObject();
                String classNameTrace = stackTraceObject.get("className").getAsString();
                String fileName = stackTraceObject.get("fileName").getAsString();
                String methodName = stackTraceObject.get("methodName").getAsString();
                int lineNumber = stackTraceObject.get("lineNumber").getAsInt();
                stackTrace[i] = new StackTraceElement(classNameTrace, methodName, fileName, lineNumber);
            }
            throwable.setStackTrace(stackTrace);
        }

        // Deserialize the cause recursively
        if (jsonObject.has("cause") && jsonObject.get("cause").isJsonObject()) {
            JsonElement causeElement = jsonObject.get("cause");
            Throwable cause = context.deserialize(causeElement, Throwable.class);
            throwable.initCause(cause);
        }

        return throwable;
    }
}
