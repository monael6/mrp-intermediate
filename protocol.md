# Entwicklungsprotokoll – MRP (Final Submission)

## Architektur
Ich habe das Projekt als RESTful Service in Java implementiert und dabei eine klare Schichtentrennung (Layered Architecture) eingehalten:

- **Handlers (`org.example.handlers`)**
    - Diese Schicht verarbeitet die HTTP-Requests.
    - Ich validiere hier die Inputs und prüfe die Authentifizierung (Token).
    - Die Logik delegiere ich an die darunterliegenden Repositories.
- **Domain (`org.example.domain`)**
    - Hier liegen meine Datenstrukturen (POJOs) wie `User`, `Media` und `Rating`.
- **Persistence (`org.example.persistence`)**
    - Dies ist meine Datenzugriffsschicht (DAO-Pattern).
    - Ich führe hier Low-Level SQL-Queries gegen die PostgreSQL-Datenbank aus.
    - Die `ResultSet`-Daten mappe ich manuell auf die Domain-Objekte.
- **Database (`org.example.db`)**
    - Diese Klasse verwaltet die JDBC-Verbindung.
    - Beim Start der Anwendung initialisiere ich hier automatisch das Datenbankschema.

---

## Features & Umsetzung (Final Submission)

### 1. User Management
- **Register/Login**: Ich habe eine Token-basierte Authentifizierung implementiert. Die Tokens speichere ich zur Laufzeit in einer Map.
- **Profile**: Statistiken wie die Anzahl der abgegebenen Ratings berechne ich "live" via SQL.

### 2. Media Management (CRUD)
- Filme, Serien und Games können angelegt, bearbeitet und gelöscht werden.
- **Genres**: Diese habe ich in eine separate Tabelle `media_genres` ausgelagert (1:n Beziehung), um flexibel zu filtern.
- **Suche/Filter**: Ich habe komplexe SQL-Queries geschrieben, um nach Titel, Genre, Typ und Jahr zu filtern.
- **Score**: Den Durchschnittswert der Bewertungen (`score`) berechne ich direkt in der Datenbank mit `AVG(stars)` und mappe ihn beim Laden in das `Media`-Objekt.

### 3. Rating System
- User können Medien mit 1-5 Sternen bewerten und optional einen Kommentar dazu schreiben.
- **Moderation**: Ich habe ein Feature eingebaut, dass Kommentare erst öffentlich sichtbar macht, wenn der Ersteller (Creator) des Mediums diese bestätigt (`confirmed`). Bis dahin sieht sie nur der Autor selbst.
- **Likes**: Zusätzlich können User die Bewertungen anderer User liken.

### 4. Empfehlungen (Recommendations)
- **By Genre**: Ich suche hier zufällige Medien, die dem `favorite_genre` des Users entsprechen.
- **By Content**: Basierend auf dem Medium, das der User am besten bewertet hat, suche ich ähnliche Inhalte (gleicher Typ, gleiche Altersfreigabe).

---

## Testabdeckung

Die Anforderung von mindestens 20 Unit Tests habe ich durch **Integrationstests** der Repository-Schicht erfüllt. Da meine Business-Logik stark mit der Datenbank verknüpft ist, hielt ich Tests gegen die (lokale) Datenbank für am sinnvollsten.

Die Tests befinden sich in `src/test/java/org/example/tests/RepositoryTests.java`. Ich teste dort folgende Szenarien:
- **User**: Registrierung und Prüfung auf doppelte Usernamen.
- **Media**: Erstellung, diverse Suchfilter, Update-Funktion und Löschen.
- **Ratings**: Hinzufügen, Update und **Verifikation der Score-Berechnung**.
- **Favorites & Likes**: Hinzufügen und Entfernen aus der Favoritenliste.
- **Recommendations**: Ich stelle sicher, dass die Recommendation-Queries Ergebnisse liefern.

Zusätzlich habe ich ein Skript `ApiWalkthrough.java` erstellt, das einen kompletten User-Flow (User A erstellt Film, User B bewertet ihn, User A bestätigt Kommentar) gegen die laufende API testet.

---

## Datenbank Setup
- Ich verwende **PostgreSQL** in einem Docker-Container.
- Port: **5332** (Mapped von 5432).
- Die Tabellen werden beim Start automatisch erstellt (`CREATE TABLE IF NOT EXISTS`), falls sie noch nicht da sind.

---

## Zeitaufwand (Gesamtprojekt)
- **Intermediate (Basis)**: ca. 16h
    - Setup, HTTP-Server, grundlegendes CRUD.
- **Final (Erweiterungen)**: ca. 12h
    - Implementierung des Rating-Systems, SQL-Optimierung, Recommendations.
- **Testing & Refactoring**: ca. 4h
    - Schreiben der Tests, Bugfixing (u.a. Score-Berechnung), Code Cleanup.

**Gesamt:** ca. 32h

---

## Fazit
Das System erfüllt meiner Meinung nach alle funktionalen Anforderungen. Die Trennung von HTTP-Handling und Datenbanklogik hilft mir, den Code übersichtlich zu halten. Durch den Verzicht auf große Frameworks (wie Hibernate) habe ich gelernt, wie JDBC "unter der Haube" funktioniert.
