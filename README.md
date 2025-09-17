# CEU Mass Mediator – Backend

This is the backend for **CEU Mass Mediator**, a computational metabolomics platform designed to support researchers by providing tools for compound matching, database querying, and annotation of experimental data. This backend exposes several endpoints to handle different types of queries and analyses.

> ⚠️ **Note**: This project is in early development. APIs, configurations, and structure are subject to change.

---

## 💡 Overview

CEU Mass Mediator (CMM) helps scientists in the field of metabolomics match experimental results against curated compound databases. This backend application powers core functionalities such as:

- **CCS Search**: Match compounds by collisional cross-section (CCS)
- **RT Search**: Search and rank compounds by retention time
- **Scoring**: Score the likelihood of annotations based on the literature.
- And more to come...

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

Each module exposes a RESTful interface:
- `/ccsSearch` – Search by collisional cross-section
- `/rtSearch` – Retention time matching and annotation
- `/scoreAnnotations` – Score lipid annotations
- And more to come...

> Detailed API documentation yet to do.

---

## 📎 Resources

- [Architecture Overview](./ARCHITECTURE.md)
- PostgreSQL schema and metadata – internal, contact maintainers

## TODO 