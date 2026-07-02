package com.wifiadmin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wifiadmin.dto.WifiConfigurationDto;
import com.wifiadmin.exception.CpeNotFoundException;
import com.wifiadmin.exception.PlatformCommunicationException;
import com.wifiadmin.service.WifiParameterService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WifiParameterController.class)
class WifiParameterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WifiParameterService wifiParameterService;

    @Test
    void getWifiParameter_shouldReturn200() throws Exception {
        WifiConfigurationDto dto = new WifiConfigurationDto();
        dto.setCpeId("CPE_001");
        dto.setWifiBand("BAND_2_4_GHZ");
        dto.setSsid("Office-2G");
        dto.setEncryptionType("WPA2_PSK");
        dto.setPassword("secret");

        when(wifiParameterService.getWifiParameter("CPE_001")).thenReturn(dto);

        mockMvc.perform(get("/wifi-parameter/CPE_001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpeId").value("CPE_001"))
                .andExpect(jsonPath("$.ssid").value("Office-2G"))
                .andExpect(jsonPath("$.wifiBand").value("BAND_2_4_GHZ"))
                .andExpect(jsonPath("$.encryptionType").value("WPA2_PSK"))
                .andExpect(jsonPath("$.password").value("secret"));
    }

    @Test
    void getWifiParameter_shouldReturn404_whenNotFound() throws Exception {
        when(wifiParameterService.getWifiParameter("UNKNOWN"))
                .thenThrow(new CpeNotFoundException("UNKNOWN"));

        mockMvc.perform(get("/wifi-parameter/UNKNOWN"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("CPE not found: UNKNOWN"))
                .andExpect(jsonPath("$.code").value("CPE_NOT_FOUND"));
    }

    @Test
    void getWifiParameter_shouldReturn502_whenPlatformFails() throws Exception {
        when(wifiParameterService.getWifiParameter("CPE_001"))
                .thenThrow(new PlatformCommunicationException("Timeout"));

        mockMvc.perform(get("/wifi-parameter/CPE_001"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.code").value("PLATFORM_ERROR"));
    }

    @Test
    void putWifiParameter_shouldReturn200() throws Exception {
        WifiConfigurationDto request = new WifiConfigurationDto();
        request.setCpeId("CPE_001");
        request.setWifiBand("BAND_2_4_GHZ");
        request.setSsid("New-SSID");
        request.setEncryptionType("WPA2_PSK");
        request.setPassword("new-password-123");

        WifiConfigurationDto response = new WifiConfigurationDto();
        response.setCpeId("CPE_001");
        response.setWifiBand("BAND_2_4_GHZ");
        response.setSsid("New-SSID");
        response.setEncryptionType("WPA2_PSK");
        response.setPassword("new-password-123");

        when(wifiParameterService.updateWifiParameter(any())).thenReturn(response);

        mockMvc.perform(put("/wifi-parameter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ssid").value("New-SSID"));
    }

    @Test
    void putWifiParameter_shouldReturn400_whenMissingCpeId() throws Exception {
        WifiConfigurationDto request = new WifiConfigurationDto();
        request.setWifiBand("BAND_2_4_GHZ");
        request.setSsid("Test-SSID");

        mockMvc.perform(put("/wifi-parameter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
