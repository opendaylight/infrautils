package org.opendaylight.infrautils.async.impl;

public interface IAsyncConfig {
    public static final String CONFIG_POOL_NAME = "PoolName";
    public static final String CONFIG_WORKER_COUNT = "WorkerCount";
    public static final String HANDLERS = "/handlers/";

    String getString(String path, String defaultValue);

    String getString(String path);

    Integer getInt(String path, int defaultValue);

    Integer getInt(String path);
}
