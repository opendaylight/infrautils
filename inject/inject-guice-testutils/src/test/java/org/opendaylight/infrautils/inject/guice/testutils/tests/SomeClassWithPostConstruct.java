/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.inject.guice.testutils.tests;

import javax.annotation.PostConstruct;

class SomeClassWithPostConstruct {

    boolean isInit = false;

    @PostConstruct
    public void init() {
        isInit = true;
    }

}
