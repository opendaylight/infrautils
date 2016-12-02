/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.inject.guice.testutils;

import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.mycila.guice.ext.closeable.CloseableInjector;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * JUnit Rule which initializes Guice {@link Injector} for tests.
 *
 * <p>Usage:
 * <pre>
 *   public {@literal @}Rule GuiceRule guice = new GuiceRule(YourGuiceModule.class);
 *
 *   {@literal @}Inject SomeClass someClass;
 * </pre>
 *
 * @author Michael Vorburger
 */
public class GuiceRule implements MethodRule {

    /**
     * Default Stage PRODUCTION.
     * Note that this is different from Guice's DEVELOPMENT default.
     * We do this to avoid having to declare bindings of Listeners asEagerSingleton(),
     * because in typical OpenDaylight projects there are Listener classes which are not @Inject,
     * but must still be created (so that they're registered).
     * See <a href="https://github.com/google/guice/wiki/Bootstrap">Guice documentation</a>.
     */
    protected static final Stage DEFAULT_STAGE = Stage.PRODUCTION;

    protected final Iterable<? extends Module> modules;
    protected final Stage stage;

    protected Injector injector;

    public GuiceRule(Module... modules) {
        this(DEFAULT_STAGE, modules);
    }

    protected GuiceRule(Stage stage, Module... modules) {
        this.modules = Arrays.asList(modules);
        this.stage = stage;
    }

    @SafeVarargs
    public GuiceRule(Class<? extends Module>... moduleClasses) {
        this.modules = createModules(Arrays.asList(moduleClasses));
        this.stage = DEFAULT_STAGE;
    }

    protected Iterable<? extends Module> createModules(List<Class<? extends Module>> moduleClasses) {
        return moduleClasses.stream().map(klass -> {
            try {
                return klass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new IllegalArgumentException("newInstance() failed: " + klass.getName(), e);
            }
        }).collect(Collectors.toList());
    }

    @Override
    public Statement apply(Statement base, FrameworkMethod method, Object target) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    setUpGuice(target);
                    base.evaluate();
                } finally {
                    tearDownGuice();
                }
            }
        };
    }

    protected void setUpGuice(Object target) {
        injector = Guice.createInjector(stage, modules);
        injector.injectMembers(target);
    }

    protected void tearDownGuice() {
        if (injector != null) {
            // http://code.mycila.com/guice/#3-jsr-250
            try {
                injector.getInstance(CloseableInjector.class).close();
            } catch (ConfigurationException e) {
                throw new IllegalStateException("You forgot to either add GuiceRule(..., AnnotationsModule.class), "
                        + "or in your Module use an install(new AnnotationsModule()) with "
                        + AnnotationsModule.class.getName(), e);
            }
        }
    }

}
