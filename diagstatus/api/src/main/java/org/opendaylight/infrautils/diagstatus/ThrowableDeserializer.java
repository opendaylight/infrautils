/*
 * Copyright (c) 2023 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.diagstatus;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
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

        var jsonObject = json.getAsJsonObject();
        var message = jsonObject.get("message").getAsString();
        var className = jsonObject.get("class").getAsString();

        // Create a new instance of the throwable class
        Throwable throwable;
        try {
            var throwableClass = Class.forName(className);
            throwable = (Throwable) throwableClass.getDeclaredConstructor(String.class).newInstance(message);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException
                 | InvocationTargetException e) {
            throw new JsonParseException("Failed to create Throwable instance", e);
        }

        // Deserialize the stack trace
        if (jsonObject.has("stackTrace") && jsonObject.get("stackTrace").isJsonArray()) {
            var stackTraceArray = jsonObject.get("stackTrace").getAsJsonArray();
            var stackTrace = new StackTraceElement[stackTraceArray.size()];
            for (int i = 0; i < stackTraceArray.size(); i++) {
                var stackTraceObject = stackTraceArray.get(i).getAsJsonObject();
                var classNameTrace = stackTraceObject.get("className").getAsString();
                var fileName = stackTraceObject.get("fileName").getAsString();
                var methodName = stackTraceObject.get("methodName").getAsString();
                var lineNumber = stackTraceObject.get("lineNumber").getAsInt();
                stackTrace[i] = new StackTraceElement(classNameTrace, methodName, fileName, lineNumber);
            }
            throwable.setStackTrace(stackTrace);
        }

        // Deserialize the cause recursively
        if (jsonObject.has("cause") && jsonObject.get("cause").isJsonObject()) {
            var causeElement = jsonObject.get("cause");
            Throwable cause = context.deserialize(causeElement, Throwable.class);
            throwable.initCause(cause);
        }

        return throwable;
    }
}
