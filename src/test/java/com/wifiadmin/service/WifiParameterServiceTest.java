package com.wifiadmin.service;

import com.wifiadmin.dto.WifiConfigurationDto;
import com.wifiadmin.exception.CpeNotFoundException;
import com.wifiadmin.exception.PlatformCommunicationException;
import com.wifiadmin.repository.WifiConfigurationRepository;
import com.wifiadmin.validation.WifiConfigurationValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.client.SoapFaultClientException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WifiParameterServiceTest {

    @Mock
    private WifiConfigurationValidator validator;

    @Mock
    private WifiConfigurationRepository repository;

    private WifiParameterService service;

    @BeforeEach
    void setUp() {
        service = new WifiParameterService(validator, repository,
                "http://localhost:8080/platform");
    }

    @Test
    void updateWifiParameter_shouldThrowOnInvalidEncryption() {
        WifiConfigurationDto dto = new WifiConfigurationDto();
        dto.setCpeId("CPE_001");
        dto.setWifiBand("BAND_2_4_GHZ");
        dto.setSsid("Test");
        dto.setEncryptionType("INVALID_TYPE");

        doThrow(new IllegalArgumentException("Invalid encryptionType: INVALID_TYPE"))
                .when(validator).validate(dto);

        assertThrows(IllegalArgumentException.class,
                () -> service.updateWifiParameter(dto));
    }

    @Test
    void updateWifiParameter_shouldThrowOnMissingPassword() {
        WifiConfigurationDto dto = new WifiConfigurationDto();
        dto.setCpeId("CPE_001");
        dto.setWifiBand("BAND_2_4_GHZ");
        dto.setSsid("Test");
        dto.setEncryptionType("WPA2_PSK");
        // no password set

        doThrow(new IllegalArgumentException(
                "Password is required when encryptionType is WPA2_PSK"))
                .when(validator).validate(dto);

        assertThrows(IllegalArgumentException.class,
                () -> service.updateWifiParameter(dto));
    }
}
