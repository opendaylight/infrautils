/*
 * Copyright (C) 2010 Mycila (mathieu.carbou@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
