// Copyright 2017 Yahoo Holdings. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.vespa.model.search;

import com.yahoo.config.provision.Flavor;
import com.yahoo.config.provisioning.FlavorsConfig;
import com.yahoo.vespa.config.search.core.ProtonConfig;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static com.yahoo.vespa.model.search.NodeFlavorTuning.MB;
import static com.yahoo.vespa.model.search.NodeFlavorTuning.GB;

/**
 * @author geirst
 */
public class NodeFlavorTuningTest {

    @Test
    public void require_that_hwinfo_disk_size_is_set() {
        ProtonConfig cfg = configFromDiskSetting(100);
        assertEquals(100 * GB, cfg.hwinfo().disk().size());
    }

    @Test
    public void require_that_hwinfo_memory_size_is_set() {
        ProtonConfig cfg = configFromMemorySetting(24);
        assertEquals(24 * GB, cfg.hwinfo().memory().size());
    }

    @Test
    public void require_that_fast_disk_is_reflected_in_proton_config() {
        ProtonConfig cfg = configFromDiskSetting(true);
        assertEquals(200, cfg.hwinfo().disk().writespeed(), 0.001);
        assertEquals(100, cfg.hwinfo().disk().slowwritespeedlimit(), 0.001);
    }

    @Test
    public void require_that_slow_disk_is_reflected_in_proton_config() {
        ProtonConfig cfg = configFromDiskSetting(false);
        assertEquals(40, cfg.hwinfo().disk().writespeed(), 0.001);
        assertEquals(100, cfg.hwinfo().disk().slowwritespeedlimit(), 0.001);
    }

    @Test
    public void require_that_document_store_maxfilesize_is_set_based_on_available_memory() {
        assertDocumentStoreMaxFileSize(256 * MB, 4);
        assertDocumentStoreMaxFileSize(256 * MB, 6);
        assertDocumentStoreMaxFileSize(256 * MB, 8);
        assertDocumentStoreMaxFileSize(256 * MB, 12);
        assertDocumentStoreMaxFileSize(512 * MB, 16);
        assertDocumentStoreMaxFileSize(1 * GB, 24);
        assertDocumentStoreMaxFileSize(1 * GB, 32);
        assertDocumentStoreMaxFileSize(1 * GB, 48);
        assertDocumentStoreMaxFileSize(1 * GB, 64);
        assertDocumentStoreMaxFileSize(4 * GB, 128);
        assertDocumentStoreMaxFileSize(4 * GB, 256);
        assertDocumentStoreMaxFileSize(4 * GB, 512);
    }

    @Test
    public void require_that_documentstore_numthreads_is_based_on_num_cores() {
        assertDocumentStoreNumThreads(8, 0);
        assertDocumentStoreNumThreads(8, 1.0);
        assertDocumentStoreNumThreads(8, 3.0);
        assertDocumentStoreNumThreads(8, 4.0);
        assertDocumentStoreNumThreads(8, 8.0);
        assertDocumentStoreNumThreads(12, 24.0);
        assertDocumentStoreNumThreads(16, 32.0);
        assertDocumentStoreNumThreads(24, 48.0);
        assertDocumentStoreNumThreads(32, 64.0);
    }

    @Test
    public void require_that_flush_strategy_memory_limits_are_set_based_on_available_memory() {
        assertFlushStrategyMemory(512 * MB, 4);
        assertFlushStrategyMemory(1 * GB, 8);
        assertFlushStrategyMemory(3 * GB, 24);
        assertFlushStrategyMemory(8 * GB, 64);
    }

    @Test
    public void require_that_flush_strategy_tls_size_is_set_based_on_available_disk() {
        assertFlushStrategyTlsSize(7 * GB, 100);
        assertFlushStrategyTlsSize(35 * GB, 500);
        assertFlushStrategyTlsSize(84 * GB, 1200);
        assertFlushStrategyTlsSize(100 * GB, 1720);
        assertFlushStrategyTlsSize(100 * GB, 24000);
    }

    private static void assertDocumentStoreMaxFileSize(long expFileSizeBytes, int memoryGb) {
        assertEquals(expFileSizeBytes, configFromMemorySetting(memoryGb).summary().log().maxfilesize());
    }

    private static void assertFlushStrategyMemory(long expMemoryBytes, int memoryGb) {
        assertEquals(expMemoryBytes, configFromMemorySetting(memoryGb).flush().memory().maxmemory());
        assertEquals(expMemoryBytes, configFromMemorySetting(memoryGb).flush().memory().each().maxmemory());
    }

    private static void assertDocumentStoreNumThreads(int numThreads, double numCores) {
        assertEquals(numThreads, configFromNumCoresSetting(numCores).summary().log().numthreads());
    }

    private static void assertFlushStrategyTlsSize(long expTlsSizeBytes, int diskGb) {
        assertEquals(expTlsSizeBytes, configFromDiskSetting(diskGb).flush().memory().maxtlssize());
    }

    private static ProtonConfig configFromDiskSetting(boolean fastDisk) {
        return getConfig(new FlavorsConfig.Flavor.Builder().
                fastDisk(fastDisk));
    }

    private static ProtonConfig configFromDiskSetting(int diskGb) {
        return getConfig(new FlavorsConfig.Flavor.Builder().
                minDiskAvailableGb(diskGb));
    }

    private static ProtonConfig configFromMemorySetting(int memoryGb) {
        return getConfig(new FlavorsConfig.Flavor.Builder().
                minMainMemoryAvailableGb(memoryGb));
    }

    private static ProtonConfig configFromNumCoresSetting(double numCores) {
        return getConfig(new FlavorsConfig.Flavor.Builder().minCpuCores(numCores));
    }

    private static ProtonConfig getConfig(FlavorsConfig.Flavor.Builder flavorBuilder) {
        flavorBuilder.name("my_flavor");
        NodeFlavorTuning tuning = new NodeFlavorTuning(new Flavor(new FlavorsConfig.Flavor(flavorBuilder)));
        ProtonConfig.Builder protonBuilder = new ProtonConfig.Builder();
        tuning.getConfig(protonBuilder);
        return new ProtonConfig(protonBuilder);
    }

}
