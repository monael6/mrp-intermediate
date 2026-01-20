# Media Rating Platform (MRP) – Final Submission

Dieses Projekt ist eine **RESTful Backend-Anwendung** für eine Medien-Bewertungsplattform (Filme, Serien, Games). Es wurde als Semesterprojekt im Fach Software Engineering entwickelt ("Final Submission" Stand).

Das System läuft standalone (Java SE + JDBC) ohne große Frameworks wie Spring oder Hibernate, um das Verständnis für Low-Level HTTP-Handling und Datenbankinteraktion zu demonstrieren.

## 🔗 GitHub Repository
[https://github.com/monael6/mrp-intermediate](https://github.com/monael6/mrp-intermediate)

---

## ✅ Umgesetzte Features

### User Management
- **Registrierung & Login** mit Token-basierter Authentifizierung.
- **Profile**: Einsicht in eigene Statistiken (z.B. Anzahl Ratings).

### Media Management
- **CRUD**: Erstellen, Lesen, Aktualisieren und Löschen von Medien.
- **Metadaten**: Titel, Jahr, Typ, Altersfreigabe, Genres.
- **Score**: Automatische Berechnung des Durchschnitts-Ratings.
- **Suche**: Filtern nach Titel, Genre, Typ etc.

### Social Features (Rating & Moderation)
- **Bewertungen**: User können 1-5 Sterne vergeben und Kommentare schreiben.
- **Moderation**: Kommentare sind erst öffentlich, wenn der **Creator** des Mediums diese bestätigt (Feature "Confirmation").
- **Likes**: User können Bewertungen anderer User liken.
- **Favoriten**: User können Medien auf ihre persönliche Favoritenliste setzen.

### Recommendations
- **Genre-basiert**: Empfehlungen passend zum Lieblingsgenre des Users.
- **Content-basiert**: Empfehlungen basierend auf Ähnlichkeit zu hoch bewerteten Titeln.

---

## 🛠 Technologien & Setup

### Tech Stack
- **Sprache**: Java 20 (OpenJDK)
- **Server**: `com.sun.net.httpserver` (Pure Java HTTP Server)
- **Datenbank**: PostgreSQL (via Docker)
- **Datenzugriff**: JDBC (DAO Pattern)
- **JSON**: Jackson Library
- **Testing**: JUnit 5, Java HttpClient (Integration Tests)

### Installation & Start

1. **Datenbank starten (Docker)**
   Die `docker-compose.yaml` startet eine PostgreSQL-Instanz auf Port 5332.
   ```bash
   docker-compose up -d
   ```

2. **Server starten**
   Führe die Main-Klasse aus:
   `org.example.Main`
   
   Der Server läuft auf: `http://localhost:8080/api`

3. **Tests ausführen**
   - Unit/Integration Tests: `org.example.tests.RepositoryTests`
   - End-to-End Walkthrough: `org.example.tests.ApiWalkthrough`

---

## 📡 API Dokumentation

Eine vollständige Postman Collection für alle Endpunkte liegt dem Projekt bei:
📄 `MRP_Final_Postman_Collection.json`

### Wichtige Endpunkte
| Method | Endpoint | Beschreibung | Auth |
|--------|----------|--------------|------|
| POST | `/api/users/register` | User registrieren | Nein |
| POST | `/api/users/login` | Login (returns Token) | Nein |
| POST | `/api/media` | Neues Medium erstellen | Ja |
| POST | `/api/media/{id}/rate` | Medium bewerten | Ja |
| POST | `/api/ratings/{id}/confirm` | Kommentar bestätigen (Creator only) | Ja |
| GET | `/api/users/{id}/recommendations` | Empfehlungen abrufen | Ja |

---

## 👤 Autor
Mona Elhouriny
