package com.wifiadmin.soap;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlElementDecl;
import jakarta.xml.bind.annotation.XmlRegistry;

import javax.xml.namespace.QName;

@XmlRegistry
public class ObjectFactory {

    private static final String NS = "http://wifi-admin.local/platform/v1";

    @XmlElementDecl(namespace = NS, name = "GetCpeIdRequest")
    public JAXBElement<GetCpeIdRequest> createGetCpeIdRequest(GetCpeIdRequest value) {
        return new JAXBElement<>(new QName(NS, "GetCpeIdRequest"), GetCpeIdRequest.class, null, value);
    }

    @XmlElementDecl(namespace = NS, name = "GetCpeIdResponse")
    public JAXBElement<GetCpeIdResponse> createGetCpeIdResponse(GetCpeIdResponse value) {
        return new JAXBElement<>(new QName(NS, "GetCpeIdResponse"), GetCpeIdResponse.class, null, value);
    }

    @XmlElementDecl(namespace = NS, name = "UpdateCpeIdRequest")
    public JAXBElement<UpdateCpeIdRequest> createUpdateCpeIdRequest(UpdateCpeIdRequest value) {
        return new JAXBElement<>(new QName(NS, "UpdateCpeIdRequest"), UpdateCpeIdRequest.class, null, value);
    }

    @XmlElementDecl(namespace = NS, name = "UpdateCpeIdResponse")
    public JAXBElement<UpdateCpeIdResponse> createUpdateCpeIdResponse(UpdateCpeIdResponse value) {
        return new JAXBElement<>(new QName(NS, "UpdateCpeIdResponse"), UpdateCpeIdResponse.class, null, value);
    }

    @XmlElementDecl(namespace = NS, name = "configuration")
    public JAXBElement<WifiConfigurationType> createConfiguration(WifiConfigurationType value) {
        return new JAXBElement<>(new QName(NS, "configuration"), WifiConfigurationType.class, null, value);
    }
}
