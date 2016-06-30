package org.opendaylight.infrautils.async.impl;

public interface IAsyncConfig {
    public static final String CONFIG_POOL_NAME = "pool.name";
    public static final String CONFIG_PROXIES = "/proxies/";

    String getString(String path, String defaultValue);

    String getString(String path);

    Integer getInt(String path, int defaultValue);

    Integer getInt(String path);
}
