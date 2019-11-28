/**
 * Copyright (C) 2010 Mycila (mathieu.carbou@gmail.com).  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.inject.guice.extensions.jsr250;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * This code originated in https://github.com/mycila/guice and was forked into
 * OpenDaylight.
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
@Singleton
public class Bank {

    List<Account> accounts = new ArrayList<Account>();

    @Resource
    Provider<Account> provider;

    @PostConstruct
    void openBank() {
        // create two account initially
        accounts.add(provider.get());
        accounts.add(provider.get());
    }

    @PreDestroy
    void closeBank() {
        accounts.clear();
    }

    int id() {
        return 2;
    }

    List<Account> accounts() {
        return accounts;
    }
}
