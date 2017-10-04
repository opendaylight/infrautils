/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils.nonnull;

import com.google.common.collect.ForwardingList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.eclipse.jdt.annotation.Checks;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Utilities related to null safe collections.
 *
 * @author Michael Vorburger.ch
 */
public final class NullSafeCollections {

    private NullSafeCollections() { }

    public static <E> List<E> wrapAsNullSafeList(List<E> list) {
        // return ListUtils.predicatedList(list, PredicateUtils.notNullPredicate());
        return new NullSafe<>(list);
    }

    private static class NullSafe<E> extends ForwardingList<E> {

        private final List<E> delegate;

        NullSafe(List<E> listToWrap) {
            Objects.requireNonNull(listToWrap, "listToWrap == null");
            requireNonNullElements(listToWrap);
            this.delegate = listToWrap;
        }

        @Override
        protected List<E> delegate() {
            return delegate;
        }

        private @NonNull E requireNonNull(@Nullable E element) {
            return Checks.requireNonNull(element, "This List does not permit null elements");
        }

        private void requireNonNullElements(Collection<? extends E> collection) {
            for (@Nullable E element : collection) {
                requireNonNull(element);
            }
        }

        @Override
        public boolean add(@Nullable E element) {
            return super.add(requireNonNull(element));
        }

        @Override
        public void add(int index, @Nullable E element) {
            super.add(index, requireNonNull(element));
        }

        @Override
        public boolean addAll(Collection<? extends E> elements) {
            requireNonNullElements(elements);
            return super.addAll(elements);
        }

        @Override
        public boolean addAll(int index, Collection<? extends E> elements) {
            requireNonNullElements(elements);
            return super.addAll(index, elements);
        }
    }

}
