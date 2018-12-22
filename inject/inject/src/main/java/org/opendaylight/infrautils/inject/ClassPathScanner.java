/*
 * Copyright © 2018 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.inject;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class path scanner designed to be used with Guice. This provides a way for modules to request the bindings they
 * need by scanning the class path.
 */
@SuppressWarnings("rawtypes")
public class ClassPathScanner {
    private static final Logger LOG = LoggerFactory.getLogger(ClassPathScanner.class);

    private final Map<String, Class> implementations = new HashMap<>();
    private final Set<Class<?>> singletons = new HashSet<>();

    /**
     * Create a class path scanner, scanning packages with the given prefix for {@literal @}Singleton annotated classes.
     *
     * @param prefix The package prefix.
     */
    public ClassPathScanner(String prefix) {
        try (ScanResult scanResult =
                 new ClassGraph()
                     .enableClassInfo()
                     .enableAnnotationInfo()
                     .whitelistPackages(prefix)
                     .scan()) {
            Set<String> duplicateInterfaces = new HashSet<>();
            for (ClassInfo singletonInfo : scanResult.getClassesWithAnnotation(Singleton.class.getName())) {
                ClassInfoList interfaces = singletonInfo.getInterfaces();
                if (interfaces.isEmpty()) {
                    singletons.add(singletonInfo.loadClass());
                } else {
                    for (ClassInfo interfaceInfo : interfaces) {
                        String interfaceName = interfaceInfo.getName();
                        if (!duplicateInterfaces.contains(interfaceName)) {
                            if (implementations.put(interfaceName, singletonInfo.loadClass()) != null) {
                                LOG.debug("{} is declared multiple times, ignoring it", interfaceName);
                                implementations.remove(interfaceName);
                                duplicateInterfaces.add(interfaceName);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Binds all {@link Singleton} annotated classes discovered by scanning the class path to all their interfaces.
     *
     * @param prefix the package prefix of Singleton implementations to consider
     * @param binder The binder (modeled as a generic consumer)
     */
    public void bindAllSingletons(String prefix, BiConsumer<Class, Class> binder, Consumer<Class> singletonConsumer) {
        implementations.forEach((interfaceName, singletonClass) -> {
            if (singletonClass.getName().startsWith(prefix)) {
                try {
                    Class interfaceClass = Class.forName(interfaceName);
                    binder.accept(interfaceClass, singletonClass);
                    // TODO later probably lower this info to debug, but for now it's very useful..
                    LOG.info("Bound {} to {}", interfaceClass, singletonClass);
                } catch (ClassNotFoundException e) {
                    LOG.warn("ClassNotFoundException on Class.forName: {}", interfaceName, e);
                }
            }
        });
        singletons.stream().filter(singletonClass -> singletonClass.getName().startsWith(prefix))
                .forEach(singletonClass -> singletonConsumer.accept(singletonClass));
    }
}
