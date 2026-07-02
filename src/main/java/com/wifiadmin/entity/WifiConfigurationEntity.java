package com.wifiadmin.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "wifi_configurations")
public class WifiConfigurationEntity {

    @Id
    @Column(name = "cpe_id", length = 100)
    private String cpeId;

    @Column(name = "wifi_band", length = 20, nullable = false)
    private String wifiBand;

    @Column(name = "ssid", length = 100, nullable = false)
    private String ssid;

    @Column(name = "encryption_type", length = 30)
    private String encryptionType;

    @Column(name = "password", length = 100)
    private String password;

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

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

    public LocalDateTime getLastSyncedAt() {
        return lastSyncedAt;
    }

    public void setLastSyncedAt(LocalDateTime lastSyncedAt) {
        this.lastSyncedAt = lastSyncedAt;
    }
}
