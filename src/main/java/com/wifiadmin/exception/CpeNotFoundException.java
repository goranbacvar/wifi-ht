package com.wifiadmin.exception;

public class CpeNotFoundException extends RuntimeException {

    private final String cpeId;

    public CpeNotFoundException(String cpeId) {
        super("CPE not found: " + cpeId);
        this.cpeId = cpeId;
    }

    public String getCpeId() {
        return cpeId;
    }
}
