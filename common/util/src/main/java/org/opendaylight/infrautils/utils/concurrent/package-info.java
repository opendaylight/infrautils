/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

/**
 * General purpose concurrency related utilities.
 */
// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=525512
// Add TYPE_PARAMETER & ARRAY_CONTENTS to NonNullByDefault value() default:
@NonNullByDefault({ PARAMETER, RETURN_TYPE, FIELD, TYPE_PARAMETER, TYPE_BOUND, TYPE_ARGUMENT, ARRAY_CONTENTS })
package org.opendaylight.infrautils.utils.concurrent;

import static org.eclipse.jdt.annotation.DefaultLocation.ARRAY_CONTENTS;
import static org.eclipse.jdt.annotation.DefaultLocation.FIELD;
import static org.eclipse.jdt.annotation.DefaultLocation.PARAMETER;
import static org.eclipse.jdt.annotation.DefaultLocation.RETURN_TYPE;
import static org.eclipse.jdt.annotation.DefaultLocation.TYPE_ARGUMENT;
import static org.eclipse.jdt.annotation.DefaultLocation.TYPE_BOUND;
import static org.eclipse.jdt.annotation.DefaultLocation.TYPE_PARAMETER;

import org.eclipse.jdt.annotation.NonNullByDefault;
