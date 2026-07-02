package com.wifiadmin.service;

import com.wifiadmin.dto.WifiConfigurationDto;
import com.wifiadmin.entity.WifiConfigurationEntity;
import com.wifiadmin.repository.WifiConfigurationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Scheduled service that syncs WiFi configurations from the platform
 * to the local database during configurable night hours.
 */
@Service
public class SyncService {

    private static final Logger log = LoggerFactory.getLogger(SyncService.class);

    private final WifiParameterService wifiParameterService;
    private final WifiConfigurationRepository repository;

    @Value("${sync.cpe-ids-per-run:50}")
    private int cpeIdsPerRun;

    public SyncService(WifiParameterService wifiParameterService,
                       WifiConfigurationRepository repository) {
        this.wifiParameterService = wifiParameterService;
        this.repository = repository;
    }

    /**
     * Scheduled task that runs at configurable night time.
     * Default: every day at 02:00 AM.
     * Syncs platform data for all known CPE IDs (up to cpeIdsPerRun per execution).
     */
    @Scheduled(cron = "${sync.cron:0 0 2 * * ?}")
    public void syncFromPlatform() {
        log.info("Starting nightly sync from platform...");

        List<WifiConfigurationEntity> allEntities = repository.findAll();
        if (allEntities.isEmpty()) {
            log.info("No CPEs in database to sync. Skipping.");
            return;
        }

        List<String> cpeIds = allEntities.stream()
                .map(WifiConfigurationEntity::getCpeId)
                .limit(cpeIdsPerRun)
                .collect(Collectors.toList());

        int successCount = 0;
        int failCount = 0;

        for (String cpeId : cpeIds) {
            try {
                WifiConfigurationDto fresh = wifiParameterService.fetchFromPlatform(cpeId);
                WifiConfigurationEntity entity = wifiParameterService.toEntity(fresh);
                entity.setLastSyncedAt(LocalDateTime.now());
                repository.save(entity);
                successCount++;
                log.debug("Synced cpeId={}", cpeId);
            } catch (Exception e) {
                failCount++;
                log.warn("Failed to sync cpeId={}: {}", cpeId, e.getMessage());
            }
        }

        log.info("Nightly sync completed. Success={}, Failed={}", successCount, failCount);
    }
}
