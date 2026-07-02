package com.wifiadmin.soap;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WifiConfigurationType", namespace = "http://wifi-admin.local/platform/v1",
         propOrder = {"cpeId", "wifiBand", "ssid", "encryptionType", "password"})
public class WifiConfigurationType {

    @XmlElement(name = "cpeId", namespace = "http://wifi-admin.local/platform/v1", required = true)
    private String cpeId;

    @XmlElement(name = "wifiBand", namespace = "http://wifi-admin.local/platform/v1", required = true)
    private String wifiBand;

    @XmlElement(name = "ssid", namespace = "http://wifi-admin.local/platform/v1", required = true)
    private String ssid;

    @XmlElement(name = "encryptionType", namespace = "http://wifi-admin.local/platform/v1", required = false)
    private String encryptionType;

    @XmlElement(name = "password", namespace = "http://wifi-admin.local/platform/v1", required = false)
    private String password;

    public String getCpeId() {
        return cpeId;
    }

    public void setCpeId(String cpeId) {
        this.cpeId = cpeId;
    }

    public String getWifiBand() {
        return wifiBand;
    }

    public void setWifiBand(String wifiBand) {
        this.wifiBand = wifiBand;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getEncryptionType() {
        return encryptionType;
    }

    public void setEncryptionType(String encryptionType) {
        this.encryptionType = encryptionType;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
