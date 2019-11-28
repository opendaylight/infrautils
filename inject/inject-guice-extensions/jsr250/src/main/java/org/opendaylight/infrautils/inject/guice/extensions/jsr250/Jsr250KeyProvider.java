/*
 * Copyright (C) 2010 Mycila (mathieu.carbou@gmail.com).  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.inject.guice.extensions.jsr250;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import java.lang.reflect.Field;
import javax.annotation.Resource;
import javax.inject.Inject;
import org.opendaylight.infrautils.inject.guice.extensions.injection.KeyProviderSkeleton;

/**
 * This code originated in https://github.com/mycila/guice and was forked into
 * OpenDaylight.
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
class Jsr250KeyProvider extends KeyProviderSkeleton<Resource> {

    @Inject Injector injector;

    @Override
    public Key<?> getKey(TypeLiteral<?> injectedType, Field injectedMember, Resource resourceAnnotation) {
        String name = resourceAnnotation.name();

        if (name.length() != 0) {
            // explicit key

            // if a name is provided, it acts as a Named binding and this means we ask for a precise key
            return Key.get(injectedType.getFieldType(injectedMember), Names.named(name));

        } else {
            // implicit key

            // if no name given, try a combination with the field name
            Key<?> implicitKey = Key.get(injectedType.getFieldType(injectedMember),
                    Names.named(injectedMember.getName()));

            if (injector.getExistingBinding(implicitKey) != null) {
                return implicitKey;

            } else {
                // else create the find based on the field type (default behavior) - with
                // optional existing binding annotations
                return super.getKey(injectedType, injectedMember, resourceAnnotation);
            }

        }
    }
}
