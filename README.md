# CEU Mass Mediator – Backend

This repository contains the development-phase backend for **CEU Mass Mediator (CMM)**, a computational metabolomics platform that provides compound matching, annotation, and scoring utilities. The service exposes a number of REST endpoints that accept JSON payloads, validate them, interact with the database and the Drools rules engine, and return curated annotations.

---

## 💡 Overview

CEU Mass Mediator (CMM) helps scientists in the field of metabolomics match experimental results against curated compound databases. This backend currently powers:

- **MS search** – simple and batch compound searches by m/z (`/api/compounds/simple-search`, `/api/compounds/batch-search`).
- **MS/MS search** – tandem mass spectra matching (`/api/msms-search`) and LC-enriched scoring (`/api/lcmsms-search`).
- **LC-MS batch search** – annotate and score multiple features (`/api/lcms-search`).
- **GC-MS search** – match GC-MS spectra to reference libraries (`/api/gcms-search`).
- **CCS search** – query compounds by collisional cross-section (`/api/imms-search`).
- **CE-MS search & markers** – electrophoretic mobility searches and marker-assisted workflows (`/api/cems-search`, `/api/cems-1-marker`, `/api/cems-2-marker`, `/api/cems-rmt-search`).
- **Browse search** – browse curated databases with flexible filters (`/api/browse-search`).
- **Score annotations** – score annotations using Drools rules (`/api/score-annotations`).

---

## 🛠️ Running the Project Locally

### Requirements

- **Java 24**
- **Maven**
- **PostgreSQL** database populated with the internal CEU Mass Mediator schema. As of right now you have to run it locally.

### Steps

1. Clone the repository.
2. Update your database connection settings (you need to have postgresql installed and running with our local database):
  
   2.1. Go to `src/main/resources/application-local.yml.template` and fill in your connection details.
   
   2.2. Rename the file to `application-local.yml` in the same directory.

3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

> 🔐 Contact us for details on how to set up the database.

---

## 📁 Structure Overview

This project follows a **feature-based architecture**. Each feature (e.g., `ccsSearch`, `rtSearch`, `scoreAnnotations`) is isolated with its own controller, service, DTOs, and domain objects. Shared models and utilities are under `shared/`.

For detailed design and flow, see [ARCHITECTURE.md](./ARCHITECTURE.md)

---

## 📌 Main Endpoints

All endpoints live under the `/api` prefix. Most request DTOs use Jakarta Bean Validation; missing or invalid parameters trigger a 400 response with a descriptive message.

| Endpoint | Description |
| --- | --- |
| `POST /api/compounds/simple-search` | Annotate a single feature by m/z. |
| `POST /api/compounds/batch-search` | Annotate multiple m/z values sequentially. |
| `POST /api/lcms-search` | LC-MS workflow that detects adducts, searches, and scores features. |
| `POST /api/gcms-search` | GC-MS search that matches spectra to our spectral libraries. |
| `POST /api/imms-search` | Collisional cross-section search. |
| `POST /api/lcimms-search` | CCS search enriched with LC retention data and lipid scoring. |
| `POST /api/msms-search` | MS/MS search (tandem mass spectra matching). |
| `POST /api/lcmsms-search` | MS/MS search enriched with LC retention-time scoring. |
| `POST /api/cems-search` | Capillary electrophoresis search by effective mobility and m/z. |
| `POST /api/cems-rmt-search` | CE search using relative migration time. |
| `POST /api/cems-1-marker` / `POST /api/cems-2-marker` | One- and two-marker CE guided searches. |
| `POST /api/browse-search` | Browse search across curated databases. |
| `POST /api/score-annotations` | Score pre-annotated features via Drools rules. |

When something goes wrong, services raise `ResponseStatusException`, ensuring clients receive a precise HTTP status code and message.

## 🔎 Search Filters

- `POST /api/compounds/simple-search`, `POST /api/compounds/batch-search`, and `POST /api/browse-search` accept an optional `formulaType` filter such as `CHNOPS` to restrict results to a chemical alphabet.
- `POST /api/msms-search` accepts `spectrumSource` with `ALL`, `experimental`, or `predicted` to control which library spectra are searched.
- Each `/api/msms-search` hit now includes its `spectrumSource` so clients can distinguish predicted from experimental matches.
- `POST /api/lcmsms-search` accepts the same MS/MS payload plus `rtValue` and optional `experimentParameters`, and returns the same MS/MS hits enriched with LC scores when available.

## ✅ Validation & Error Handling

- All public DTOs use Jakarta Bean Validation to guard against missing values and malformed data.
- Controllers annotate request bodies with `@Valid`, so invalid requests never reach the service layer.
- Services and repositories convert domain failures into `ResponseStatusException`, providing predictable error responses.

## 🧪 Testing

The project includes unit tests for core services and integration tests that exercise each endpoint. Run them with:

```bash
mvn test
```

Tests rely on the Drools knowledge base and the configured PostgreSQL schema. See the integration tests under `src/test/java/ceu/biolab/cmm/integration` for examples of expected payloads and responses.

---

## 📎 Resources

- [Architecture Overview](./ARCHITECTURE.md)
- PostgreSQL schema and metadata – internal, contact maintainers
