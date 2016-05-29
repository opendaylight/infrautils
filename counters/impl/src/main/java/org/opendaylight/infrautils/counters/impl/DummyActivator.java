/*
 * Copyright (c) 2016 Hewlett Packard Enterprise, Co. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.counters.impl;

import org.opendaylight.infrautils.counters.impl.service.CountersDumperService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;


public class DummyActivator implements BundleActivator {

	public void start(BundleContext context) throws Exception {
		System.out.println("Guy Sela Bundle Started!");
		new CountersDumperService().init();
		new Thread(new GuyMiniThread()).start();
		System.out.println("Finished!");
		
	}

	public void stop(BundleContext context) throws Exception {
		System.out.println("Guy Bundle Stopped!");
		
	}
	
	class GuyMiniThread implements Runnable {

		@Override
		public void run() {
			System.out.println("Guy thread started!");
			while (true) {
			System.out.println("Incremented Counter");
			DummyActivatorCounters.guy_counter.inc();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	 enum DummyActivatorCounters {
	        guy_counter, //
	        ;

	        private OccurenceCounter counter;

	        DummyActivatorCounters() {
	            counter = new OccurenceCounter(getClass().getEnclosingClass().getSimpleName(), name(), "");
	        }

	        public void inc() {
	            counter.inc();
	        }
	    }
}
