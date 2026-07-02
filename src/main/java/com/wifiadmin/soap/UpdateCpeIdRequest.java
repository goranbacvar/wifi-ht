package com.wifiadmin.soap;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlRootElement(name = "UpdateCpeIdRequest", namespace = "http://wifi-admin.local/platform/v1")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UpdateCpeIdRequest", namespace = "http://wifi-admin.local/platform/v1", propOrder = {"configuration"})
public class UpdateCpeIdRequest {

    @XmlElement(name = "configuration", namespace = "http://wifi-admin.local/platform/v1", required = true)
    private WifiConfigurationType configuration;

    public WifiConfigurationType getConfiguration() {
        return configuration;
    }

    public void setConfiguration(WifiConfigurationType configuration) {
        this.configuration = configuration;
    }
}
