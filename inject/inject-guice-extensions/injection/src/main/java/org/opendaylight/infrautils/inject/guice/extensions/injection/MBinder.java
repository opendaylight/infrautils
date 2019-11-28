/**
 * Copyright (C) 2010 Mycila (mathieu.carbou@gmail.com).  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.inject.guice.extensions.injection;

import com.google.inject.Binder;
import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.MembersInjector;
import com.google.inject.Module;
import com.google.inject.PrivateBinder;
import com.google.inject.Provider;
import com.google.inject.Scope;
import com.google.inject.Stage;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.binder.AnnotatedConstantBindingBuilder;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.matcher.Matcher;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.Dependency;
import com.google.inject.spi.Message;
import com.google.inject.spi.ModuleAnnotatedMethodScanner;
import com.google.inject.spi.ProvisionListener;
import com.google.inject.spi.TypeConverter;
import com.google.inject.spi.TypeListener;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;

/**
 * This code originated in https://github.com/mycila/guice and was forked into
 * OpenDaylight.
 * @author Mathieu Carbou (mathieu.carbou@gmail.com) date 2013-07-20
 */
@SuppressWarnings("checkstyle:OverloadMethodsDeclarationOrder")
public final class MBinder implements Binder {

    private final Binder binder;

    private MBinder(Binder binder) {
        this.binder = binder;
    }

    public <A extends Annotation> MBinder bindAnnotationInjector(Class<A> annotationType,
            Class<? extends KeyProvider<A>> providerClass) {
        binder.bindListener(Matchers.any(),
                willInject(new MemberInjectorTypeListener<A>(annotationType, providerClass)));
        return this;
    }

    public <A extends Annotation> MBinder handleMethodAfterInjection(Class<A> annotationType,
            Class<? extends MethodHandler<A>> providerClass) {
        binder.bindListener(Matchers.any(),
                willInject(new MethodHandlerTypeListener<A>(annotationType, providerClass)));
        return this;
    }

    public <A extends Annotation> MBinder handleFieldAfterInjection(Class<A> annotationType,
            Class<? extends FieldHandler<A>> providerClass) {
        binder.bindListener(Matchers.any(), willInject(new FieldHandlerTypeListener<A>(annotationType, providerClass)));
        return this;
    }

    public <T> T willInject(T object) {
        binder.requestInjection(object);
        return object;
    }

    public static MBinder wrap(Binder binder) {
        return new MBinder(binder);
    }

    // DELEGATES

    @Override
    public void addError(Message message) {
        binder.addError(message);
    }

    @Override
    public void addError(String message, Object... arguments) {
        binder.addError(message, arguments);
    }

    @Override
    public void addError(Throwable throwable) {
        binder.addError(throwable);
    }

    @Override
    public <T> LinkedBindingBuilder<T> bind(Key<T> key) {
        return binder.bind(key);
    }

    @Override
    public <T> AnnotatedBindingBuilder<T> bind(Class<T> type) {
        return binder.bind(type);
    }

    @Override
    public <T> AnnotatedBindingBuilder<T> bind(TypeLiteral<T> typeLiteral) {
        return binder.bind(typeLiteral);
    }

    @Override
    public AnnotatedConstantBindingBuilder bindConstant() {
        return binder.bindConstant();
    }

    @Override
    public void bindInterceptor(Matcher<? super Class<?>> classMatcher, Matcher<? super Method> methodMatcher,
            MethodInterceptor... interceptors) {
        for (MethodInterceptor interceptor : interceptors) {
            requestInjection(interceptor);
        }
        binder.bindInterceptor(classMatcher, methodMatcher, interceptors);
    }

    @Override
    public void bindListener(Matcher<? super TypeLiteral<?>> typeMatcher, TypeListener listener) {
        binder.bindListener(typeMatcher, listener);
    }

    @Override
    public void bindScope(Class<? extends Annotation> annotationType, Scope scope) {
        binder.bindScope(annotationType, scope);
    }

    @Override
    public void convertToTypes(Matcher<? super TypeLiteral<?>> typeMatcher, TypeConverter converter) {
        binder.convertToTypes(typeMatcher, converter);
    }

    @Override
    public Stage currentStage() {
        return binder.currentStage();
    }

    @Override
    public void disableCircularProxies() {
        binder.disableCircularProxies();
    }

    @Override
    public <T> MembersInjector<T> getMembersInjector(Class<T> type) {
        return binder.getMembersInjector(type);
    }

    @Override
    public <T> MembersInjector<T> getMembersInjector(TypeLiteral<T> typeLiteral) {
        return binder.getMembersInjector(typeLiteral);
    }

    @Override
    public <T> Provider<T> getProvider(Key<T> key) {
        return binder.getProvider(key);
    }

    @Override
    public <T> Provider<T> getProvider(Class<T> type) {
        return binder.getProvider(type);
    }

    @Override
    public void install(Module module) {
        binder.install(module);
    }

    @Override
    public PrivateBinder newPrivateBinder() {
        return binder.newPrivateBinder();
    }

    @Override
    public void requestInjection(Object instance) {
        binder.requestInjection(instance);
    }

    @Override
    public <T> void requestInjection(TypeLiteral<T> type, T instance) {
        binder.requestInjection(type, instance);
    }

    @Override
    public void requestStaticInjection(Class<?>... types) {
        binder.requestStaticInjection(types);
    }

    @Override
    public void requireExplicitBindings() {
        binder.requireExplicitBindings();
    }

    @Override
    public Binder skipSources(Class... classesToSkip) {
        return binder.skipSources(classesToSkip);
    }

    @Override
    public Binder withSource(Object source) {
        return binder.withSource(source);
    }

    @Override
    public void bindListener(Matcher<? super Binding<?>> bindingMatcher, ProvisionListener... listeners) {
        binder.bindListener(bindingMatcher, listeners);
    }

    @Override
    public void requireAtInjectOnConstructors() {
        binder.requireAtInjectOnConstructors();
    }

    @Override
    public void requireExactBindingAnnotations() {
        binder.requireExactBindingAnnotations();
    }

    @Override
    public <T> Provider<T> getProvider(Dependency<T> dpndnc) {
        return binder.getProvider(dpndnc);
    }

    @Override
    public void scanModulesForAnnotatedMethods(ModuleAnnotatedMethodScanner mams) {
        binder.scanModulesForAnnotatedMethods(mams);
    }

}
