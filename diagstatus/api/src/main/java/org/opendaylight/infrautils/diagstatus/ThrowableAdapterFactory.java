/*
 * Copyright (c) 2023 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.infrautils.diagstatus;

import com.google.errorprone.annotations.Var;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

final class ThrowableAdapterFactory implements TypeAdapterFactory {
    private ThrowableAdapterFactory() {
    }

    public static final ThrowableAdapterFactory INSTANCE = new ThrowableAdapterFactory();

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        // Only handles Throwable and subclasses; let other factories handle any other type
        if (!Throwable.class.isAssignableFrom(type.getRawType())) {
            return null;
        }

        @SuppressWarnings("unchecked")
        TypeAdapter<T> adapter = (TypeAdapter<T>) new TypeAdapter<Throwable>() {
            @Override
            public Throwable read(JsonReader in) throws IOException {
                JsonToken token = in.peek();
                if (token == JsonToken.NULL) {
                    in.nextNull();
                    return null;
                }

                @Var
                String exceptionType = null;
                @Var
                String message = null;
                @Var
                Throwable cause = null;
                List<Throwable> suppressed = new ArrayList<>();

                in.beginObject();
                while (in.hasNext()) {
                    String name = in.nextName();
                    if ("type".equals(name)) {
                        exceptionType = in.nextString();
                    } else if ("message".equals(name)) {
                        message = in.nextString();
                    } else if ("cause".equals(name)) {
                        cause = read(in); // Recursively read the cause
                    } else if ("suppressed".equals(name)) {
                        in.beginArray();
                        while (in.hasNext()) {
                            suppressed.add(read(in)); // Recursively read each suppressed exception
                        }
                        in.endArray();
                    }
                }
                in.endObject();

                // Create the appropriate exception instance based on the type name
                if (exceptionType != null && !exceptionType.isEmpty()) {
                    try {
                        Class<?> exceptionClass = Class.forName(exceptionType);
                        Constructor<?> constructor = exceptionClass.getConstructor(String.class, Throwable.class);
                        Throwable throwable = (Throwable) constructor.newInstance(message, cause);
                        suppressed.forEach(throwable::addSuppressed);
                        return throwable;
                    } catch (ClassNotFoundException e) {
                        throw new JsonParseException("Failed to create Throwable instance: Class not found", e);
                    } catch (NoSuchMethodException e) {
                        throw new JsonParseException("Failed to create Throwable instance: Constructor not found", e);
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                        throw new JsonParseException("Failed to create Throwable instance: Instantiation error", e);
                    }
                } else {
                    // If type is not available, create a generic Throwable with the message
                    return new Throwable(message, cause);
                }
            }

            @Override
            public void write(JsonWriter out, Throwable value) throws IOException {
                if (value == null) {
                    out.nullValue();
                    return;
                }

                out.beginObject();
                // Include exception type name to give more context; for example NullPointerException might
                // not have a message
                out.name("type");
                out.value(value.getClass().getSimpleName());

                out.name("message");
                out.value(value.getMessage());

                Throwable cause = value.getCause();
                if (cause != null) {
                    out.name("cause");
                    write(out, cause);
                }

                Throwable[] suppressedArray = value.getSuppressed();
                if (suppressedArray.length > 0) {
                    out.name("suppressed");
                    out.beginArray();

                    for (Throwable suppressed : suppressedArray) {
                        write(out, suppressed);
                    }

                    out.endArray();
                }
                out.endObject();
            }
        };
        return adapter;
    }
}