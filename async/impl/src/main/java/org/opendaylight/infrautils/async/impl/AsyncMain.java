package org.opendaylight.infrautils.async.impl;

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncMain {
	protected static final Logger logger = LoggerFactory.getLogger(AsyncMain.class);
	
	private SchedulerService schedulerService;
	private AsyncConfig config;
	private BundleContext bundleContext;
	
	public AsyncMain() {
		config = new AsyncConfig();
		schedulerService = new SchedulerService(config);
		AsyncInvocationProxy.setSchedulerService(schedulerService);
		AsyncInvocationProxy.setAsyncConfig(config);
	}
	
	public void setContext(BundleContext bcontext) {
	    this.bundleContext = bcontext;
	    AsyncInvocationProxy.setBundleContext(bcontext);
	}
	
	public void initialize() {
		try {
			//logger.debug("initialize() called, interval is: " + interval);
			System.out.println("Async init called");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	public void clean() {
		System.out.println("Async clean called!");
//		logger.info("Counters Thread Clean called!");
//		if (countersRunnable != null) {
//			countersRunnable.setKeepRunning(false);
//			countersThread.interrupt();
//			try {
//				countersThread.join();
//			} catch (InterruptedException e) {
//			}
//		}
	}
}
