# Project Architecture: CMM Spring Boot Backend

## Overview

This project is a Spring Boot backend that receives JSON POST requests, processes the data through a layered architecture, and returns structured responses also as JSON. It follows a **feature-based modular architecture** to maintain scalability, testability, and clear separation of concerns.

---

## How to develop a new endpoint / functionality

To create a new endpoint from scratch you should follow the steps:
1. Pull the repository and create a new branch.
2. Create a new package following the [package layout](#package-layout). For example `ceu.biolab.cmm.yourEndpoint`
3. Create the subpackages for each layer you will use: `controller`, `service`, `repository`, `domain` and `dto`. See the [Core Layers section](#core-layers) to understand them.
4. Use the `shared` package for all general classes that may be reused for other endpoints. Chances are your endpoint will require many classes from this package, so don't write them again! For example, check [`shared.compound.Compound.java`](/src/main/java/ceu/biolab/cmm/shared/domain/compound/Compound.java) which contains all the general fields to represent a compound in metabolomics. You can also extend these classes if you want to add specific fields.
5. When you've pushed your commits and have functioning code in your branch, you can make a **pull request** to the `master` branch, which will be reviewed before merging.

## REQUIREMENTS

- **Make unit tests**: It is mandatory to have unit tests for your main functionalities using the JUnit testing framework. This allows safety to your code in case of future changes or refactoring. Tests go in `src/test/java/ceu/biolab/cmm/`.

- **Use Lombok for data classes**: Specially for DTOs and domain classes, use the [Lombok library](https://projectlombok.org/) to avoid boilerplate code. This library allows you to use annotations like `@Data`, `@Getter`, `@Setter`, etc. to automatically generate getters, setters, and other methods. See example in [`shared.compound.Compound.java`](/src/main/java/ceu/biolab/cmm/shared/domain/compound/Compound.java).

---

## Core Layers

### Controller
- Entry point for incoming HTTP requests. Processes JSON POST requests and returns the output also in JSON format.
- Located in `controller` folders.
- See [`ccsSearch.controller`](/src/main/java/ceu/biolab/cmm/ccsSearch/controller/CompoundCcsController.java) for reference.

### Service
- All core logic of your endpoint. Anything that isn't database queries, domain data structures, or request handling goes here.
- Services are usually called or triggered by the controller after receiving a request. Services also can call functions from the repository.
- See [`ccsSearch.service`](/src/main/java/ceu/biolab/cmm/ccsSearch/service/CcsSearchService.java) for reference.

### Repository
- Encapsulates data access logic (SQL queries, entity resolution, etc.)
- See [`ccsSearch.repository`](/src/main/java/ceu/biolab/cmm/ccsSearch/repository/CcsSearchRepository.java) for reference.

### Domain
- Any data structures, interfaces or enums that represent a clear concept such as `Compound`, `Annotation` or `IMSFeature`. You can find these and more examples in the `shared.domain` package.

### DTO (Data Transfer Objects)
- Data structures with many fields sometimes used to transfer information between functions, particularily between packages, and specially for requests.
- These can be considered those data structures that don't fit a clear concept but are meant to contain many fields of data.

### > Example of Request Handling Flow

1. **Controller** receives JSON POST request.
2. **DTO** maps the request body.
3. **Service** performs processing using domain models and shared logic.
4. **Repository** retrieves or persists data as needed.
5. **Response DTO** is returned via the controller.

---

## Architecture of the Domain

The architecture follows its own rules. We try to keep the rule of modularity by having each endpoint package isolated with its own specific domain classes and 

> The `shared` package should never have references to other endpoint packages!

> Domain classes in `shared` should avoid having optional or nullable fields specific of other endpoints. Instead the endpoints should use interfaces or class hierarchies.

A simplified example of the domain classes for `shared` and 2 other endpoints. Notice how interfaces are used to allow customization within general domain classes.

![Domain Architecture Example](/resources/domain_architecture_example.png)

---

## Package Layout

```
ceu.biolab.cmm
│
├── ccsSearch/
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── dto/
│   └── domain/
│
├── rtSearch/
│   ├── controller/
│   ├── service/
│   ├── repository/
│   └── model/
│
├── scoreAnnotations/
│   ├── controller/
│   ├── service/
│   ├── dto/
│   └── domain/
│
├── shared/
│   ├── domain/  (commonly used models, enums, constants)
│   └── service/ (common functionalities)
│
├── config/
│   └── WebConfig.java
│
└── Application.java
```

---


---

## Tools & Configuration

- **Drools Rules**: `.xls` files used for rule-based processing. Find an example in the code of the scoreAnnotations endpoint.
- **Springboot configuration**: Managed via `application.yml`.
- **Security**: TLS configuration via `mykeystore.jks`.
- **SQL Scripts**: Custom queries stored under `resources/sql/`.

