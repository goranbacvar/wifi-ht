package com.wifiadmin.controller;

import com.wifiadmin.dto.WifiConfigurationDto;
import com.wifiadmin.service.WifiParameterService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/wifi-parameter")
public class WifiParameterController {

    private static final Logger log = LoggerFactory.getLogger(WifiParameterController.class);

    private final WifiParameterService wifiParameterService;

    public WifiParameterController(WifiParameterService wifiParameterService) {
        this.wifiParameterService = wifiParameterService;
    }

    @GetMapping(value = "/{cpeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WifiConfigurationDto> getWifiParameter(@PathVariable String cpeId) {
        log.info("GET /wifi-parameter/{}", cpeId);
        WifiConfigurationDto result = wifiParameterService.getWifiParameter(cpeId);
        return ResponseEntity.ok(result);
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WifiConfigurationDto> putWifiParameter(
            @Valid @RequestBody WifiConfigurationDto dto) {
        log.info("PUT /wifi-parameter for cpeId={}", dto.getCpeId());
        WifiConfigurationDto result = wifiParameterService.updateWifiParameter(dto);
        return ResponseEntity.ok(result);
    }
}
