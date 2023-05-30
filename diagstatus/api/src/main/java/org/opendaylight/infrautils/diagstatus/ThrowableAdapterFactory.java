/*
 * Copyright (c) 2023 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.infrautils.diagstatus;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;

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
                throw new UnsupportedOperationException();
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