# Media Rating Platform (MRP) – Intermediate Submission

## Beschreibung
Dieses Projekt implementiert ein REST-basiertes Backend für eine Media Rating Platform.
Es handelt sich um eine **Standalone Java HTTP Server Anwendung** ohne Frameworks
(Spring etc. werden nicht verwendet).

Die Anwendung ermöglicht:
- User Registrierung
- User Login mit Token-basierter Authentifizierung
- Verwaltung von Media-Einträgen (CRUD)

Ein Frontend ist **nicht Teil des Projekts**.

---

## Technologien
- Java 20
- Java HttpServer (`com.sun.net.httpserver`)
- PostgreSQL
- JDBC
- Jackson (JSON)
- Postman (API Tests)

---

## Projekt starten

### 1. PostgreSQL
Die Anwendung verbindet sich mit einer lokalen PostgreSQL-Datenbank.

**Verbindungsdaten:**
Database: mrp
User: postgres
Password: password
Host: localhost
Port: 5332


### 2. Server starten
Starte die Klasse:

org.example.Main

Erwartete Ausgabe:

Connected to DB: mrp
Tables ready.
Server running...

Die benötigten Tabellen werden beim Start automatisch erstellt.

---

## API testen (Postman)

Eine Postman Collection liegt bei:
MRP_Postman_Collection.json


### Typischer Ablauf:
1. User registrieren
2. User einloggen → Token erhalten
3. Media-Endpunkte mit Authorization-Header testen

**Authorization Header:**
Authorization: Bearer <username>-mrpToken


---

## Umgesetzte Features (Intermediate)
- HTTP REST Server
- User Registrierung
- User Login
- Token-basierte Authentifizierung
- Media CRUD (Create, Read, Update, Delete)
- PostgreSQL Persistenz
- Zugriffskontrolle:
    - Nur eingeloggte User dürfen Media erstellen
    - Nur der Ersteller darf Media ändern oder löschen

---

## Nicht Teil der Intermediate-Abgabe
- Ratings
- Likes
- Favorites
- Empfehlungen
- Filter & Sortierung

Diese Features sind für die Final Submission vorgesehen.