// Copyright 2017 Yahoo Holdings. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.vespa.config.server;

import com.yahoo.cloud.config.ConfigserverConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tonytv
 */
public class ConfigServerSpec implements com.yahoo.config.model.api.ConfigServerSpec {

    private final String hostName;
    private final int configServerPort;
    private final int httpPort;
    private final int zooKeeperPort;

    public String getHostName() {
        return hostName;
    }

    public int getConfigServerPort() {
        return configServerPort;
    }

    public int getHttpPort() {
        return httpPort;
    }

    public int getZooKeeperPort() {
        return zooKeeperPort;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ConfigServerSpec) {
            ConfigServerSpec other = (ConfigServerSpec)o;

            return hostName.equals(other.hostName) &&
                    configServerPort == other.configServerPort &&
                    httpPort == other.httpPort &&
                    zooKeeperPort == other.zooKeeperPort;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return hostName.hashCode();
    }

    public ConfigServerSpec(String hostName, int configServerPort, int httpPort, int zooKeeperPort) {
        this.hostName = hostName;
        this.configServerPort = configServerPort;
        this.httpPort = httpPort;
        this.zooKeeperPort = zooKeeperPort;
    }

    public static List<com.yahoo.config.model.api.ConfigServerSpec> fromConfig(ConfigserverConfig configserverConfig) {
        List<com.yahoo.config.model.api.ConfigServerSpec> specs = new ArrayList<>();
        for (ConfigserverConfig.Zookeeperserver server : configserverConfig.zookeeperserver()) {
            // TODO We cannot be sure that http port always is rpcport + 1
            specs.add(new ConfigServerSpec(server.hostname(), configserverConfig.rpcport(), configserverConfig.rpcport() + 1, server.port()));
        }
        return specs;
    }

    @Override
    public String toString() {
        return "hostname=" + hostName + ", rpc port=" + configServerPort + ", http port=" + httpPort + ", zookeeper port=" + zooKeeperPort;
    }

}
