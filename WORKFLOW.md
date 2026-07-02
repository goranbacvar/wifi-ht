# WiFi Admin — Workflow dokumentacija

## Arhitektura sustava

```text
┌──────────────┐      REST (JSON)       ┌──────────────────┐      SOAP 1.1 (XML)      ┌───────────────┐
│  Klijent     │ ──────────────────────▶ │  Backend         │ ───────────────────────▶ │  Mock         │
│ (Postman /   │                        │  (Spring Boot)   │                         │  platforma    │
│  Frontend)   │ ◀────────────────────── │  localhost:8081  │ ◀─────────────────────── │  (Mockoon)    │
└──────────────┘      JSON response      └──────────────────┘      SOAP response       └───────────────┘
                                              │
                                              │ ◀─── (ako nema u cache-u)
                                              ▼
                                      ┌──────────────┐
                                      │  H2 Database  │
                                      │  (Lokalni     │
                                      │   cache)      │
                                      └──────────────┘
```

---

## 📥 GET /wifi-parameter/{cpeId} — Dijagram toka

```
Klijent                    Controller              Service                 DB                 Mock (SOAP)
   │                           │                      │                    │                    │
   │  GET /wifi-parameter/     │                      │                    │                    │
   │      {cpeId}              │                      │                    │                    │
   │ ────────────────────────▶ │                      │                    │                    │
   │                           │                      │                    │                    │
   │                           │  getWifiParameter()  │                    │                    │
   │                           │ ────────────────────▶│                    │                    │
   │                           │                      │                    │                    │
   │                           │                      │ findById(cpeId)    │                    │
   │                           │                      │ ──────────────────▶│                    │
   │                           │                      │                    │                    │
   │                           │                      │◀───── entitet ─────│                    │
   │                           │                      │  (ili prazno)      │                    │
   │                           │                      │                    │                    │
   ├─── Ako postoji u bazi ────┤                      │                    │                    │
   │                           │                      │                    │                    │
   │                           │◀─────── DTO ─────────│                    │                    │
   │                           │                      │                    │                    │
   │◀───────── 200 OK ─────────│                      │                    │                    │
   │       + JSON              │                      │                    │                    │
   │                           │                      │                    │                    │
   ├─── Ako NE postoji u bazi ─┤                      │                    │                    │
   │                           │                      │                    │                    │
   │                           │                      │ Konstruiraj SOAP   │                    │
   │                           │                      │ <soap:Envelope>    │                    │
   │                           │                      │ <GetCpeIdRequest>  │                    │
   │                           │                      │                    │                    │
   │                           │                      │ HTTP POST /platform│                    │
   │                           │                      │ SOAPAction:        │                    │
   │                           │                      │   #getCpeID         │                    │
   │                           │                      │ ──────────────────────────────────────▶│
   │                           │                      │                    │                    │
   │                           │                      │◀──── SOAP XML ─────────────────────────│
   │                           │                      │    response        │                    │
   │                           │                      │                    │                    │
   │                           │                      │ parseGetCpeIdResp()│                    │
   │                           │                      │                    │                    │
   │   ── SOAP Fault ──────────┤                      │                    │                    │
   │   (CPE ne postoji)        │                      │                    │                    │
   │                           │◀─── CpeNotFound ─────│                    │                    │
   │◀───────── 404 ────────────│                      │                    │                    │
   │  "CPE not found"          │                      │                    │                    │
   │                           │                      │                    │                    │
   │   ── Uspjeh ──────────────┤                      │                    │                    │
   │                           │                      │ saveToDatabase()   │                    │
   │                           │                      │ ──────────────────▶│                    │
   │                           │◀─────── DTO ─────────│                    │                    │
   │                           │                      │                    │                    │
   │◀───────── 200 OK ─────────│                      │                    │                    │
   │       + JSON              │                      │                    │                    │
```

---

## 📤 PUT /wifi-parameter — Dijagram toka

```
Klijent                    Controller            Validator               Service              DB              Mock (SOAP)
   │                           │                      │                    │                    │                    │
   │  PUT /wifi-parameter      │                      │                    │                    │                    │
   │  Content-Type: app/json   │                      │                    │                    │                    │
   │  {cpeId, wifiBand, ...}   │                      │                    │                    │                    │
   │ ────────────────────────▶ │                      │                    │                    │                    │
   │                           │                      │                    │                    │                    │
   │                           │  @Valid provjera     │                    │                    │                    │
   │                           │  (cpeId, wifiBand,   │                    │                    │                    │
   │                           │   ssid obavezni)     │                    │                    │                    │
   │                           │                      │                    │                    │                    │
   ├─── Jakarta Validation ────┤                      │                    │                    │                    │
   │   ne prolazi              │                      │                    │                    │                    │
   │◀───────── 400 ────────────│                      │                    │                    │                    │
   │  VALIDATION_ERROR         │                      │                    │                    │                    │
   │                           │                      │                    │                    │                    │
   │                           │  updateWifiParam()   │                    │                    │                    │
   │                           │ ────────────────────────────────────────▶│                    │                    │
   │                           │                      │                    │                    │                    │
   │                           │                      │  validator.        │                    │                    │
   │                           │                      │  validate(dto)     │                    │                    │
   │                           │                      │ ◀──────────────────│                    │                    │
   │                           │                      │                    │                    │                    │
   │                           │                      │  Provjera:         │                    │                    │
   │                           │                      │  • encryptionType  │                    │                    │
   │                           │                      │  • wifiBand        │                    │                    │
   │                           │                      │  • password required│                    │                    │
   │                           │                      │  • password length  │                    │                    │
   │                           │                      │                    │                    │                    │
   ├─── Business Validation ───┤                      │                    │                    │                    │
   │   ne prolazi              │                      │                    │                    │                    │
   │◀───────── 400 ────────────│                      │                    │                    │                    │
   │  VALIDATION_ERROR         │                      │                    │                    │                    │
   │                           │                      │                    │                    │                    │
   │                           │                      │                    │ Konstruiraj SOAP   │                    │
   │                           │                      │                    │ <soap:Envelope>    │                    │
   │                           │                      │                    │ <UpdateCpeIdReq>   │                    │
   │                           │                      │                    │ <configuration>    │                    │
   │                           │                      │                    │                    │                    │
   │                           │                      │                    │ HTTP POST /platform│                    │
   │                           │                      │                    │ SOAPAction:        │                    │
   │                           │                      │                    │   #updateCpeId     │                    │
   │                           │                      │                    │ ───────────────────────────────────▶│
   │                           │                      │                    │                    │                    │
   │                           │                      │                    │◀──── SOAP XML ──────────────────────│
   │                           │                      │                    │    response        │                    │
   │                           │                      │                    │                    │                    │
   │                           │                      │                    │ parseResponse()    │                    │
   │                           │                      │                    │                    │                    │
   │   ── SOAP Fault ──────────┤                      │                    │                    │                    │
   │   (CPE ne postoji)        │                      │                    │                    │                    │
   │◀───────── 404 ────────────│                      │                    │                    │                    │
   │  CPE_NOT_FOUND            │                      │                    │                    │                    │
   │                           │                      │                    │                    │                    │
   │   ── Ostala greška ───────┤                      │                    │                    │                    │
   │◀───────── 502 ────────────│                      │                    │                    │                    │
   │  PLATFORM_ERROR           │                      │                    │                    │                    │
   │                           │                      │                    │                    │                    │
   │   ── Uspjeh ──────────────┤                      │                    │                    │                    │
   │                           │                      │                    │ saveToDatabase()   │                    │
   │                           │                      │                    │ ─────────────────▶│                    │
   │                           │◀───────── DTO ───────│────────────────────│                    │                    │
   │                           │                      │                    │                    │                    │
   │◀───────── 200 OK ─────────│                      │                    │                    │                    │
   │       + JSON              │                      │                    │                    │                    │
```

---

## Noćna sinkronizacija

```
Cron: 0 0 2 * * ?  (svaki dan u 02:00)

         │
         ▼
  repository.findAll()  ──── dohvati sve CPE-ove iz baze
         │
         ▼
  ╔════════════════════════════════════════╗
  ║  Za svaki CPE (max cpeIdsPerRun):      ║
  ║                                         ║
  ║  1. fetchFromPlatform(cpeId)           ║
  ║     ─── SOAP getCpeID ───▶ Mock        ║
  ║     ◀─── SOAP response ◀────────────── ║
  ║                                         ║
  ║  2. toEntity(freshData)                ║
  ║     + setLastSyncedAt(now)             ║
  ║                                         ║
  ║  3. repository.save(entity)            ║
  ╚════════════════════════════════════════╝
         │
         ▼
  Log: "Nightly sync completed. Success=X, Failed=Y"
```

---

## HTTP Status kodovi

| Status | Kada se vraća | Primjer odgovora |
|--------|--------------|------------------|
| **200 OK** | Uspješan GET ili PUT | `{ "cpeId": "CPE_001", "wifiBand": "BAND_2_4_GHZ", "ssid": "Office-2G" }` |
| **400 Bad Request** | Validacija ne prolazi | `{ "message": "Password is required when encryptionType is WPA2_PSK", "code": "VALIDATION_ERROR" }` |
| **404 Not Found** | CPE ne postoji na platformi | `{ "message": "CPE not found: UNKNOWN", "code": "CPE_NOT_FOUND" }` |
| **502 Bad Gateway** | Platforma nedostupna ili vraća Fault | `{ "message": "Failed to communicate with platform: ...", "code": "PLATFORM_ERROR" }` |
| **500 Internal Server Error** | Neočekivana greška | `{ "message": "Internal server error", "code": "INTERNAL_ERROR" }` |

---

## SOAP XML primjeri

### GET zahtjev (backend → mock)
```xml
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
               xmlns:tns="http://wifi-admin.local/platform/v1">
  <soap:Body>
    <tns:GetCpeIdRequest>
      <tns:cpeId>CPE_001</tns:cpeId>
    </tns:GetCpeIdRequest>
  </soap:Body>
</soap:Envelope>
```

### GET odgovor (mock → backend)
```xml
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
               xmlns:tns="http://wifi-admin.local/platform/v1">
  <soap:Body>
    <tns:GetCpeIdResponse>
      <tns:configuration>
        <tns:cpeId>CPE_001</tns:cpeId>
        <tns:wifiBand>BAND_2_4_GHZ</tns:wifiBand>
        <tns:ssid>Office-2G</tns:ssid>
        <tns:encryptionType>WPA2_PSK</tns:encryptionType>
        <tns:password>seed-wifi-01</tns:password>
      </tns:configuration>
    </tns:GetCpeIdResponse>
  </soap:Body>
</soap:Envelope>
```

### PUT zahtjev (backend → mock)
```xml
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
               xmlns:tns="http://wifi-admin.local/platform/v1">
  <soap:Body>
    <tns:UpdateCpeIdRequest>
      <tns:configuration>
        <tns:cpeId>CPE_001</tns:cpeId>
        <tns:wifiBand>BAND_2_4_GHZ</tns:wifiBand>
        <tns:ssid>New-SSID</tns:ssid>
        <tns:encryptionType>WPA2_PSK</tns:encryptionType>
        <tns:password>new-password</tns:password>
      </tns:configuration>
    </tns:UpdateCpeIdRequest>
  </soap:Body>
</soap:Envelope>
```

---

## Komponente i odgovornosti

| Sloj | Klasa | Odgovornost |
|------|-------|-------------|
| **Controller** | `WifiParameterController` | Primanje HTTP zahtjeva, `@Valid` validacija, mapiranje HTTP metoda |
| **Service** | `WifiParameterService` | Poslovna logika, SOAP komunikacija, keširanje u bazu |
| **Validation** | `WifiConfigurationValidator` | Poslovna pravila (lozinka ↔ encryptionType, min duljina, dopuštene vrijednosti) |
| **Exception** | `GlobalExceptionHandler` | `@RestControllerAdvice` — mapiranje iznimki u HTTP statuse (400, 404, 502, 500) |
| **Repository** | `WifiConfigurationRepository` | Spring Data JPA — pristup H2 bazi |
| **Entity** | `WifiConfigurationEntity` | JPA entitet za `wifi_configurations` tablicu |
| **Scheduler** | `SyncService` | Noćna sinkronizacija (cron: 0 0 2 * * ?) |
| **Config** | `SoapClientConfig` | Konfiguracija `WebServiceTemplate` za SOAP pozive |

---

## Validacijska pravila

1. **Jakarta Bean Validation** (@Valid):
   - `cpeId` — obavezno (`@NotBlank`)
   - `wifiBand` — obavezno (`@NotBlank`)
   - `ssid` — obavezno (`@NotBlank`), max 32 znaka (`@Size`)

2. **Poslovna pravila** (`WifiConfigurationValidator`):
   - `encryptionType` mora biti jedna od: `OPEN`, `WEP`, `WPA_PSK`, `WPA2_PSK`, `WPA3_SAE`, `WPA2_ENTERPRISE`
   - `wifiBand` mora biti `BAND_2_4_GHZ` ili `BAND_5_GHZ`
   - Ako je `encryptionType` = `OPEN`, password ne smije biti postavljen
   - Ako `encryptionType` ≠ `OPEN`, password je obavezan (min 8, max 64 znaka)
   - Ako je `encryptionType` izostavljen, podrazumijeva se `OPEN`
