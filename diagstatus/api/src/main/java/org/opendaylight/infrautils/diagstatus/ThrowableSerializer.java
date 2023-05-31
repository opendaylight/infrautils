/*
 * Copyright (c) 2023 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.diagstatus;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;

@SuppressWarnings("Var")
public class ThrowableSerializer implements JsonSerializer<Throwable> {

    @Override
    public JsonElement serialize(final Throwable src, final Type typeOfSrc, final JsonSerializationContext context) {
        final var jsonObject = new JsonObject();
        jsonObject.addProperty("message", src.getMessage());
        jsonObject.addProperty("class", src.getClass().getName());

        // Serialize the stack trace
        final var stackTraceArray = new JsonArray();
        for (final var element : src.getStackTrace()) {
            final var stackTraceObject = new JsonObject();
            stackTraceObject.addProperty("className", element.getClassName());
            stackTraceObject.addProperty("fileName", element.getFileName());
            stackTraceObject.addProperty("methodName", element.getMethodName());
            stackTraceObject.addProperty("lineNumber", element.getLineNumber());
            stackTraceArray.add(stackTraceObject);
        }
        jsonObject.add("stackTrace", stackTraceArray);

        // If the throwable has a cause, serialize it recursively
        if (src.getCause() != null) {
            final var causeElement = context.serialize(src.getCause());
            jsonObject.add("cause", causeElement);
        }

        return jsonObject;
    }
}
