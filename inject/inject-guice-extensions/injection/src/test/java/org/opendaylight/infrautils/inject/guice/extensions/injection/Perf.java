/**
 * Copyright (C) 2010 Mycila (mathieu.carbou@gmail.com).  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.inject.guice.extensions.injection;

import com.google.common.collect.Iterables;

import java.io.File;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * This code originated in https://github.com/mycila/guice and was forked into
 * OpenDaylight.
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public final class Perf {
    private Perf() {

    }

    public static void main(String[] args) throws Exception {
        perfTestMembers();
    }

    private static void perfTestMembers() throws Exception {
        List<Class<?>> classes = new LinkedList<Class<?>>();
        JarFile jarFile = new JarFile(new File("C:\\Program Files\\Java\\jdk1.7.0_15\\jre\\lib\\rt.jar"));
        Enumeration<JarEntry> enums = jarFile.entries();
        while (enums.hasMoreElements()) {
            JarEntry entry = enums.nextElement();
            if (entry.getName().endsWith(".class")) {
                if (entry.getName().startsWith("javax/swing")
                    || entry.getName().startsWith("java/awt")
                    || entry.getName().startsWith("java/awt")) {
                    classes.add(Class
                            .forName(entry.getName().replace('/', '.').substring(0, entry.getName().length() - 6)));
                }
            }
        }

        // start visual VM
        Thread.sleep(10000);

        for (int i = 0; i < 100; i++) {
            long time = System.nanoTime();
            for (Class<?> c : classes) {
                Iterables.size(Reflect.findAllAnnotatedMethods(c, Deprecated.class));
            }
            long end = System.nanoTime();
        }
    }
}
