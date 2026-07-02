@echo off
REM ============================================
REM WiFi Admin — build & run skripte
REM ============================================
REM
REM PREREQUISITES:
REM   - Java 17+ (JDK)
REM   - Apache Maven 3.8+
REM   - Docker Desktop (za pokretanje mock platforme)
REM   - Node.js 18+ (za frontend)
REM
REM ============================================
REM
REM KORAK 1: Pokreni mock platformu
REM ============================================
REM   docker compose up -d
REM
REM ============================================
REM KORAK 2: Buildaj backend
REM ============================================
REM   mvn clean package -DskipTests
REM
REM ============================================
REM KORAK 3: Pokreni backend (dev profil)
REM ============================================
REM   java -jar target/wifi-admin-1.0.0.jar --spring.profiles.active=dev
REM
REM   Ili direktno s Mavenom:
REM   mvn spring-boot:run -Dspring-boot.run.profiles=dev
REM
REM ============================================
REM KORAK 4: Pokreni frontend
REM ============================================
REM   cd frontend
REM   npm install
REM   npm run dev
REM
REM ============================================
REM Backend ce biti dostupan na:
REM   http://localhost:8081
REM
REM Frontend ce biti dostupan na:
REM   http://localhost:3000
REM
REM H2 Console (dev):
REM   http://localhost:8081/h2-console
REM   JDBC URL: jdbc:h2:file:./data/wifiadmin
REM   User: sa, Password: (empty)
REM ============================================
