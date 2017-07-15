/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils.concurrent;

import java.util.concurrent.CompletionStage;

/**
 * Utilities for {@link CompletionStage}.
 *
 * @author Michael Vorburger.ch
 */
public final class CompletionStages {

    private CompletionStages() { }

    /**
     * Return an immediately exceptional completed CompletionStage.
     * See {@link CompletableFutures#completedExceptionally(Throwable)}.
     */
    public static <T> CompletionStage<T> completedExceptionally(Throwable throwable) {
        return CompletionStageWrapper.wrap(CompletableFutures.completedExceptionally(throwable));
    }

}
