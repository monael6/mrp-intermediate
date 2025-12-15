# Entwicklungsprotokoll – MRP Intermediate

## Architektur
Das Projekt ist in folgende Schichten unterteilt:

- **Handlers**
    - Verantwortlich für HTTP Requests
    - Validierung von HTTP-Methoden
    - Authentifizierung & Autorisierung
- **Domain**
    - Datenmodelle (User, Media, Credentials)
- **Persistence (Repository)**
    - Datenbankzugriffe via JDBC
- **Database**
    - Verwaltung der PostgreSQL-Verbindung
    - Automatische Tabellenerstellung beim Serverstart

---

## Authentifizierung
Es wird eine einfache token-basierte Authentifizierung verwendet.

- Beim Login wird ein Token generiert:

<username>-mrpToken

- Tokens werden serverseitig in einer In-Memory-Map gespeichert
- Jeder Request (außer Register & Login) prüft:
- Existenz des Authorization-Headers
- Gültigkeit des Tokens
- Ungültige oder fehlende Tokens führen zu `401 Unauthorized`

---

## Autorisierung (Creator-Check)
Für Media Update und Delete gilt:
- Nur der User, der das Media erstellt hat, darf es verändern oder löschen
- Der `creator_id` wird serverseitig gesetzt und **niemals vom Client übernommen**
- Bei Verstoß wird `403 Forbidden` zurückgegeben

---

## Datenbank
PostgreSQL wird zur Persistenz verwendet.

Tabellen:
- `users`
- `media`

Die Tabellen werden beim Start des Servers automatisch erstellt.
Die Datenbank läuft lokal auf Port **5332**.

---

## Probleme & Lösungen

### Problem: 403 Forbidden trotz korrektem User
**Ursache:**  
`creator_id` wurde nicht korrekt in der Datenbank gespeichert.

**Lösung:**
- Datenbankschema angepasst
- `creator_id` wird beim Media Create gesetzt
- Update/Delete vergleichen DB-Wert mit eingeloggtem User

---

### Problem: 500 Internal Server Error
**Ursache:**  
Mismatch zwischen Java-Code und Datenbankschema (fehlende Spalten).

**Lösung:**
- Einheitliche Spaltennamen eingeführt
- Tabellen neu erstellt

---

### Problem: Tabellen nicht sichtbar in psql
**Ursache:**  
Java verwendete Port 5332, psql standardmäßig Port 5432.

**Lösung:**  
Verbindung zu psql explizit mit Port 5332 hergestellt.

---

## Testabdeckung
Die Kernlogik wurde manuell über Postman getestet:
- Positive Tests (korrekte Nutzung)
- Negative Tests (fehlender Token, falscher User)

Alle relevanten HTTP-Statuscodes wurden überprüft.

---

## Zeitaufwand (geschätzt)
- Projektsetup & Architektur: ca. 3h
- Authentifizierung & Token-Handling: ca. 3h
- Media CRUD & DB-Integration: ca. 4h
- Debugging & Fehlerbehebung: ca. 4h
- Tests & Dokumentation: ca. 2h

---

## Fazit
Die Anforderungen der Intermediate Submission wurden vollständig umgesetzt.
Das System ist stabil, nachvollziehbar aufgebaut und vollständig testbar.

