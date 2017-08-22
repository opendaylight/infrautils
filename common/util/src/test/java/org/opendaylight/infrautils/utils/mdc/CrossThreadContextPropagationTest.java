/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils.mdc;

import static com.google.common.truth.Truth.assertThat;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import org.junit.Test;

/**
 * Unit test illustrating cross thread context propagation.
 *
 * @author Michael Vorburger.ch
 */
public class CrossThreadContextPropagationTest {

//    private static final Logger LOG = LoggerFactory.getLogger(MDCTest.class);
//
//    public @Rule LogRule logRule = new LogRule();
//    public @Rule LogCaptureRule logCaptureRule = new LogCaptureRule();

    @Test
    public void testPropagationInParallelStream() throws InterruptedException, ExecutionException {
        // from http://www.baeldung.com/java-8-parallel-streams-custom-threadpool
        final long firstNum = 1;
        final long lastNum = 1_000_000;
        List<Long> list = LongStream.rangeClosed(firstNum, lastNum).boxed().collect(Collectors.toList());

        ThreadLocal<String> threadLocalString = new ThreadLocal<>();
        threadLocalString.set("hello, world");

        // For MDC propagation to work, instead of doing something using the MDC like:
        //    long actualTotal = list.parallelStream().reduce(0L, Long::sum);
        // we have to execute it as a task in a fork-join pool,
        // because parallelStream() does not allow us to customize the Executor (in Java 8),
        // but ForkJoinTask.fork "Arranges to asynchronously execute this task in the pool the current task is running
        // in, if applicable, or using the ForkJoinPool.commonPool() if not inForkJoinPool()", see https://stackoverflow.com/a/22269778/421602.
        ForkJoinPool customThreadPool = new ForkJoinPool(4);
        long actualTotal = customThreadPool.submit(() -> {
            assertThat(threadLocalString.get()).isEqualTo("hello, world");
            return list.parallelStream().reduce(0L, Long::sum);
        }).get();

        assertThat((lastNum + firstNum) * lastNum / 2).isEqualTo(actualTotal);
    }

}
