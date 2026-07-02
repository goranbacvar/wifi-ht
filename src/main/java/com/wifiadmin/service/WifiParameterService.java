package com.wifiadmin.service;

import com.wifiadmin.dto.WifiConfigurationDto;
import com.wifiadmin.entity.WifiConfigurationEntity;
import com.wifiadmin.exception.CpeNotFoundException;
import com.wifiadmin.exception.PlatformCommunicationException;
import com.wifiadmin.repository.WifiConfigurationRepository;
import com.wifiadmin.validation.WifiConfigurationValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class WifiParameterService {

    private static final Logger log = LoggerFactory.getLogger(WifiParameterService.class);
    private static final String NS = "http://wifi-admin.local/platform/v1";
    private static final String SOAP_NS = "http://schemas.xmlsoap.org/soap/envelope/";

    private static final String SOAP_ACTION_GET = "http://wifi-admin.local/platform/v1#getCpeID";
    private static final String SOAP_ACTION_UPDATE = "http://wifi-admin.local/platform/v1#updateCpeId";

    private final WifiConfigurationValidator validator;
    private final WifiConfigurationRepository repository;
    private final String platformUrl;

    public WifiParameterService(WifiConfigurationValidator validator,
                                WifiConfigurationRepository repository,
                                @Value("${platform.soap.url}") String platformUrl) {
        this.validator = validator;
        this.repository = repository;
        this.platformUrl = platformUrl;
    }

    public WifiConfigurationDto getWifiParameter(String cpeId) {
        Optional<WifiConfigurationEntity> cached = repository.findById(cpeId);
        if (cached.isPresent()) {
            log.debug("Returning cached WiFi configuration for cpeId={}", cpeId);
            return toDto(cached.get());
        }
        WifiConfigurationDto dto = fetchFromPlatform(cpeId);
        saveToDatabase(dto);
        return dto;
    }

    public WifiConfigurationDto updateWifiParameter(WifiConfigurationDto dto) {
        validator.validate(dto);
        WifiConfigurationDto updated = updateOnPlatform(dto);
        saveToDatabase(updated);
        return updated;
    }

    public WifiConfigurationDto fetchFromPlatform(String cpeId) {
        try {
            String soapRequest = String.format(
                "<soap:Envelope xmlns:soap=\"%s\" xmlns:tns=\"%s\">" +
                "  <soap:Body>" +
                "    <tns:GetCpeIdRequest>" +
                "      <tns:cpeId>%s</tns:cpeId>" +
                "    </tns:GetCpeIdRequest>" +
                "  </soap:Body>" +
                "</soap:Envelope>",
                SOAP_NS, NS, escapeXml(cpeId));

            String responseXml = sendSoapRequest(soapRequest, SOAP_ACTION_GET);

            if (responseXml.contains("Fault") && responseXml.contains("faultstring")) {
                String faultMsg = extractFaultString(responseXml);
                log.warn("SOAP fault for getCpeID cpeId={}: {}", cpeId, faultMsg);
                if (faultMsg.toLowerCase().contains("not found")) {
                    throw new CpeNotFoundException(cpeId);
                }
                throw new PlatformCommunicationException("SOAP fault from platform: " + faultMsg);
            }

            return parseGetCpeIdResponse(responseXml, cpeId);

        } catch (CpeNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching WiFi config from platform for cpeId={}", cpeId, e);
            throw new PlatformCommunicationException(
                "Failed to fetch WiFi config from platform: " + e.getMessage(), e);
        }
    }

    private WifiConfigurationDto updateOnPlatform(WifiConfigurationDto dto) {
        try {
            String encType = dto.getEncryptionType() != null ? dto.getEncryptionType() : "";
            String password = dto.getPassword() != null ? dto.getPassword() : "";

            StringBuilder soapRequest = new StringBuilder();
            soapRequest.append(String.format(
                "<soap:Envelope xmlns:soap=\"%s\" xmlns:tns=\"%s\">" +
                "  <soap:Body>" +
                "    <tns:UpdateCpeIdRequest>" +
                "      <tns:configuration>" +
                "        <tns:cpeId>%s</tns:cpeId>" +
                "        <tns:wifiBand>%s</tns:wifiBand>" +
                "        <tns:ssid>%s</tns:ssid>",
                SOAP_NS, NS, escapeXml(dto.getCpeId()),
                escapeXml(dto.getWifiBand()), escapeXml(dto.getSsid())));

            if (!encType.isEmpty()) {
                soapRequest.append(String.format("<tns:encryptionType>%s</tns:encryptionType>", escapeXml(encType)));
            }
            if (!password.isEmpty()) {
                soapRequest.append(String.format("<tns:password>%s</tns:password>", escapeXml(password)));
            }

            soapRequest.append(
                "      </tns:configuration>" +
                "    </tns:UpdateCpeIdRequest>" +
                "  </soap:Body>" +
                "</soap:Envelope>");

            String responseXml = sendSoapRequest(soapRequest.toString(), SOAP_ACTION_UPDATE);

            if (responseXml.contains("Fault") && responseXml.contains("faultstring")) {
                String faultMsg = extractFaultString(responseXml);
                log.warn("SOAP fault for updateCpeId cpeId={}: {}", dto.getCpeId(), faultMsg);
                if (faultMsg.toLowerCase().contains("not found")) {
                    throw new CpeNotFoundException(dto.getCpeId());
                }
                throw new PlatformCommunicationException("SOAP fault from platform: " + faultMsg);
            }

            return parseGetCpeIdResponse(responseXml, dto.getCpeId());

        } catch (CpeNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating WiFi config on platform for cpeId={}", dto.getCpeId(), e);
            throw new PlatformCommunicationException(
                "Failed to update WiFi config on platform: " + e.getMessage(), e);
        }
    }

    private WifiConfigurationDto parseGetCpeIdResponse(String xml, String expectedCpeId) {
        try {
            WifiConfigurationDto dto = new WifiConfigurationDto();
            dto.setCpeId(extractXmlValue(xml, "cpeId"));
            dto.setWifiBand(extractXmlValue(xml, "wifiBand"));
            dto.setSsid(extractXmlValue(xml, "ssid"));
            dto.setEncryptionType(extractXmlValue(xml, "encryptionType"));
            dto.setPassword(extractXmlValue(xml, "password"));
            if (dto.getCpeId() == null) {
                throw new CpeNotFoundException(expectedCpeId);
            }
            return dto;
        } catch (CpeNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new PlatformCommunicationException(
                "Failed to parse platform response: " + e.getMessage(), e);
        }
    }

    private String extractXmlValue(String xml, String tagName) {
        String openTag = "<" + tagName + ">";
        String closeTag = "</" + tagName + ">";
        // Also try with namespace prefix: <tns:tagName>
        String openNsTag = "<tns:" + tagName + ">";
        String closeNsTag = "</tns:" + tagName + ">";

        int start;
        if (xml.contains(openNsTag)) {
            start = xml.indexOf(openNsTag) + openNsTag.length();
        } else if (xml.contains(openTag)) {
            start = xml.indexOf(openTag) + openTag.length();
        } else {
            return null;
        }

        String closeTagToUse = xml.contains(closeNsTag) ? closeNsTag : closeTag;
        int end = xml.indexOf(closeTagToUse, start);
        if (end < 0) return null;

        String value = xml.substring(start, end).trim();
        return value.isEmpty() ? null : value;
    }

    private String extractFaultString(String xml) {
        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLEventReader reader = factory.createXMLEventReader(new StringReader(xml));
            boolean inFaultString = false;
            while (reader.hasNext()) {
                XMLEvent event = reader.nextEvent();
                if (event.isStartElement()) {
                    String local = event.asStartElement().getName().getLocalPart();
                    if ("faultstring".equals(local)) inFaultString = true;
                } else if (event.isCharacters() && inFaultString) {
                    return event.asCharacters().getData();
                }
            }
            reader.close();
        } catch (Exception e) {
            // ignore parsing errors
        }
        return "Unknown error";
    }

    private String sendSoapRequest(String soapXml, String soapAction) throws Exception {
        URI uri = new URI(platformUrl);
        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
        conn.setRequestProperty("SOAPAction", soapAction);
        conn.setDoOutput(true);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);

        byte[] requestBytes = soapXml.getBytes(StandardCharsets.UTF_8);
        conn.setRequestProperty("Content-Length", String.valueOf(requestBytes.length));

        try (OutputStream os = conn.getOutputStream()) {
            os.write(requestBytes);
            os.flush();
        }

        int status = conn.getResponseCode();
        BufferedReader reader;
        if (status >= 200 && status < 300) {
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        } else {
            reader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
        }

        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line).append("\n");
        }
        reader.close();
        conn.disconnect();

        return response.toString().trim();
    }

    private String escapeXml(String value) {
        if (value == null) return "";
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;");
    }

    // --- Mapping helpers ---

    public WifiConfigurationDto toDto(WifiConfigurationEntity entity) {
        WifiConfigurationDto dto = new WifiConfigurationDto();
        dto.setCpeId(entity.getCpeId());
        dto.setWifiBand(entity.getWifiBand());
        dto.setSsid(entity.getSsid());
        dto.setEncryptionType(entity.getEncryptionType());
        dto.setPassword(entity.getPassword());
        return dto;
    }

    public WifiConfigurationEntity toEntity(WifiConfigurationDto dto) {
        WifiConfigurationEntity entity = new WifiConfigurationEntity();
        entity.setCpeId(dto.getCpeId());
        entity.setWifiBand(dto.getWifiBand());
        entity.setSsid(dto.getSsid());
        entity.setEncryptionType(dto.getEncryptionType());
        entity.setPassword(dto.getPassword());
        entity.setLastSyncedAt(LocalDateTime.now());
        return entity;
    }

    private void saveToDatabase(WifiConfigurationDto dto) {
        WifiConfigurationEntity entity = repository.findById(dto.getCpeId())
                .orElse(new WifiConfigurationEntity());
        entity.setCpeId(dto.getCpeId());
        entity.setWifiBand(dto.getWifiBand());
        entity.setSsid(dto.getSsid());
        entity.setEncryptionType(dto.getEncryptionType());
        entity.setPassword(dto.getPassword());
        entity.setLastSyncedAt(LocalDateTime.now());
        repository.save(entity);
    }
}
