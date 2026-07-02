package com.wifiadmin.soap;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlRootElement(name = "GetCpeIdRequest", namespace = "http://wifi-admin.local/platform/v1")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetCpeIdRequest", namespace = "http://wifi-admin.local/platform/v1", propOrder = {"cpeId"})
public class GetCpeIdRequest {

    @XmlElement(name = "cpeId", namespace = "http://wifi-admin.local/platform/v1", required = true)
    private String cpeId;

    public String getCpeId() {
        return cpeId;
    }

    public void setCpeId(String cpeId) {
        this.cpeId = cpeId;
    }
}
