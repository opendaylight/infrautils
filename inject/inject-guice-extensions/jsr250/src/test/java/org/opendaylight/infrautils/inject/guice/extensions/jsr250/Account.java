/*
 * Copyright (C) 2010 Mycila (mathieu.carbou@gmail.com).  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.inject.guice.extensions.jsr250;

import javax.annotation.Resource;
import javax.inject.Named;
import javax.inject.Provider;

/**
 * This code originated in https://github.com/mycila/guice and was forked into
 * OpenDaylight.
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public class Account {

    @Resource
    Bank bank;

    String number;

    @Resource
    void init(Client client, @Named("RNG") Provider<Id> rng) {
        number = bank.id() + "" + client.id() + "" + rng.get().id();
    }

    public String getNumber() {
        return number;
    }

    void close() {
    }
}
