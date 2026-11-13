# CMM backend API reference

This guide documents the REST API implemented by the `code-review` branch of
`ceu-biolab/cmm-springboot`, commit `c4bb35eb2dba1a8c9d6c9e6eb146694279060ae8`
(8 May 2026), reviewed on **12 November 2026**. It
describes the request and response contract visible in the controllers, DTOs,
services, and test fixtures in that revision. It does not describe a running
deployment or guarantee that a public CMM server runs this backend.

Start with [calling conventions](#2-calling-the-api), the
[endpoint index](#3-endpoint-reference), and [response models](#4-response-data-shapes).
All 22 application operations (40 method/path mappings including aliases)
are covered below.
Source links are relative to this revision; external compatibility findings
are dated and linked to their evidence.

## 1. CMM 3.0 compatibility

**A compatibility module was developed, but it is not integrated into
`code-review`.** The branch has no `ceu.biolab.cmm.adapters` package, legacy
request translators, or CMM 3.0 route mappings. Its existing route aliases
invoke the same current DTOs; they do not translate old requests or responses.

The comparison uses the original server's
[APIServices.java](https://github.com/albertogilf/ceuMassMediator/blob/f8ff0f1a7c4ff0d2dd933afcd63682dc8ab2ef4e/ceu-mass-mediator-v2.0/src/java/services/rest/api/APIServices.java)
(which includes the `/v3` routes despite its historical directory name), its
request models, and the [cmmr client](https://github.com/YaoxiangLi/cmmr).
These establish source-level contracts, not the version running on a public
server. Searches of the fetched upstream branch tips and commit messages
found no integrated adapter; the adapter was located in a contributor fork
through the pull requests below.

### 1.1 Adapter implementation and integration status

As checked on 12 November 2026:

| Evidence | Finding |
|---|---|
| [PR #41, Adapter/joaquin de vicente](https://github.com/ceu-biolab/cmm-springboot/pull/41) | Open, unmerged, targeting `master`, from `JoaquinDeVA/cmm-springboot:Adapter/JoaquinDeVicente`; inspected head `23160dd62624a68e5e2a9e253f3f5037943e1fed`. |
| [Earlier PR #38](https://github.com/ceu-biolab/cmm-springboot/pull/38) | Closed without merging. |
| `code-review` tree | No `src/main/java/ceu/biolab/cmm/adapters/` directory. |
| Git ancestry | PR #41's inspected head is not an ancestor of `code-review`. |

The proposed module contains controllers, legacy DTOs, services, enum
deserializers, adduct/database/metabolite translators, response converters,
and test fixtures. Its four controllers register these routes, **available
only in the PR, not this branch**:

| Proposed route | Adapter |
|---|---|
| `POST /api/simplesearch` | Single-mass search |
| `POST /api/advancedbatch` | LC-MS batch annotation/scoring |
| `POST /api/browsesearch` | Name/formula search |
| `POST /api/msmssearch` | Tandem-MS search |

See the [adapter source at the inspected PR head](https://github.com/JoaquinDeVA/cmm-springboot/tree/23160dd62624a68e5e2a9e253f3f5037943e1fed/src/main/java/ceu/biolab/cmm/adapters).
It accepts old names such as `mass`, `masses`, `tolerance_mode`, `ion_mode`,
`metabolites_type`, `composite_spectra`, and unbracketed adducts, then maps
results into legacy-style `results` objects.

The proposal is **partial compatibility**, even if integrated. Its controllers
do not register `/api/v3/batch` or `/api/v3/advancedbatch`, and it has no batch-MS
adapter controller. The advanced adapter selects non-empty `all_*` arrays in
preference to the primary arrays rather than combining them. It explicitly
rejects MINE/Metlin selections; its MS/MS adapter does not forward
`spectra_types` to the current spectrum-source filter and labels output as
experimental. Route coverage, translations, and behavior need review and
contract tests against captured CMM 3.0 requests/responses before promising
drop-in compatibility. This documentation does not merge or enable the module.

### 1.2 Legacy routes and current equivalents

| CMM 3.0 operation or path | Current branch route | Compatibility |
|---|---|---|
| `POST /api/v3/batch` | `POST /api/compounds/batch-search` | Similar batch m/z search; the current route requires the current request property names and enum values. |
| `POST /api/v3/advancedbatch` | `POST /api/lcms-search` | Similar LC-MS annotation and scoring workflow; payload shape and names differ. |
| `POST /api/msmssearch` | `POST /api/msms-search` | Similar tandem-MS operation; current route expects current MS/MS fields and peak-object format. |
| `POST /api/simplesearch` | `POST /api/compounds/simple-search` | Single mass; property names, adducts, and response envelope differ. |
| `POST /api/advancedbatch` | `POST /api/lcms-search` | Older advanced-batch operation; different request/response models. |
| `POST /api/advancedsearch` | `POST /api/lcms-search` with one feature | Functional substitute using one-element arrays, not an alias. |
| `POST /api/browsesearch` | `POST /api/browse-search` | Old `name` becomes `compoundName`; no `exactFormula` flag. Case-sensitive paths matter: current alias `/api/browseSearch` is different. |

The legacy paths above are **not implemented** in this branch. The current
controllers do not accept the CMM 3.0 field names as aliases. In particular,
do not assume that `masses`, `tolerance_mode`, `ion_mode`, old adduct labels
such as `M+H`, or older enum spellings will work on MS, LC-MS, or MS/MS
requests. The closest current equivalents are:

| Older field or value | Current field or value |
|---|---|
| `masses` | `mzValues` on `/api/compounds/batch-search`; `mz` (a list) on `/api/lcms-search`; `precursorIonMZ` or `precursorIonMZValues` on MS/MS routes |
| `tolerance_mode` | `mzToleranceMode` on MS and LC-MS search; `toleranceModePrecursorIon` / `toleranceModeFragments` on MS/MS |
| `ion_mode` | `ionizationMode` on MS, LC-MS, CCS, and MS/MS; GC-MS has no ionization-mode field; CE-MS RMT/marker DTOs retain `ion_mode` |
| `ppm`, `mDa` | `PPM`, `MDA` for the shared m/z tolerance mode |
| `positive`, `negative` | `POSITIVE`, `NEGATIVE` on most routes; CE-MS accepts case-insensitive ionization strings. CE `polarity` is a separate `Direct` / `Reverse` setting. |
| `M+H`, `M-H` | Canonical adduct strings such as `[M+H]+`, `[M-H]-`, listed by `GET /api/adducts` |
| List of `{mz, intensity}` peak pairs | `{"peaks":[{"mz": ..., "intensity": ...}]}` for MS/MS; LC-MS composite spectra use one `{"mz": intensity}` map per feature |
| `metabolites_type: "only-lipids"` | `metaboliteType: "ONLYLIPIDS"`; neither old peptide-selection value has an exact current equivalent |
| `databases: ["hmdb"]` | `databases: ["HMDB"]`; use current enum values rather than display labels |
| `chemical_alphabet`, `modifiers_type`, `retention_times`, `composite_spectra` | LC-MS `chemicalAlphabet` (or `formulaType`), `modifiersType`, `retentionTimes`, `compositeSpectrum` |
| MS/MS `ion_mass`, `ms_ms_peaks` | `precursorIonMZ`, `fragmentsMZsIntensities.peaks` |
| MS/MS `precursor_ion_tolerance` / `precursor_mz_tolerance` and their modes | `tolerancePrecursorIon` / `toleranceFragments` and their respective mode fields |
| MS/MS `ionization_voltage: "medium"` | `CIDEnergy: "MED"` |
| MS/MS `spectra_types` array | A single `spectrumSource`: `ALL`, `experimental`, or `predicted` |

Request-field evidence: original
[batch DTO](https://github.com/albertogilf/ceuMassMediator/blob/f8ff0f1a7c4ff0d2dd933afcd63682dc8ab2ef4e/ceu-mass-mediator-v2.0/src/java/services/rest/api/request/SimpleBatchQuery.java),
[advanced DTO](https://github.com/albertogilf/ceuMassMediator/blob/f8ff0f1a7c4ff0d2dd933afcd63682dc8ab2ef4e/ceu-mass-mediator-v2.0/src/java/services/rest/api/request/AdvancedBatchQuery.java),
[MS/MS DTO](https://github.com/albertogilf/ceuMassMediator/blob/f8ff0f1a7c4ff0d2dd933afcd63682dc8ab2ef4e/ceu-mass-mediator-v2.0/src/java/services/rest/api/request/MsMsSearchQuery.java),
and [browse DTO](https://github.com/albertogilf/ceuMassMediator/blob/f8ff0f1a7c4ff0d2dd933afcd63682dc8ab2ef4e/ceu-mass-mediator-v2.0/src/java/services/rest/api/request/BrowseSearchQuery.java).

These are migration mappings, not automatic conversions. In particular,
`metaboliteType: "ALL"` does not promise the former peptide-exclusion semantics,
and `databases: ["ALL"]` means a union of the current supported identifier
filters, not the old database universe. All current MS/MS tolerances must be
at most 100; historical clients using 500/1000 mDa defaults need adjustment.
Responses also need conversion: a CMM 3.0 client expecting legacy result
objects cannot directly consume current `msfeatures`, annotated-feature
arrays, or `msmsList` objects.

Some CE-MS request DTOs do retain CMM 3.0-style snake_case names. For example,
the RMT routes accept `masses`, `tolerance_mode`, `rmt`, and `ion_mode`; the
marker routes also accept their migration-time or RMT fields in snake_case.
This is **partial payload-level continuity only**: the routes, validation,
field values, and responses are those of this branch, and it does not make the
old CMM 3.0 API generally compatible.

#### Current endpoints retaining CMM-style field names

These are current route paths, not the old CMM 3.0 paths. The listed request
properties are accepted by this branch:

| Current route | CMM-style properties still accepted |
|---|---|
| `POST /api/cems-search` | `mz_values`, `effective_mobilities`, `mz_tolerance`, `mz_tolerance_mode`, `eff_mob_tolerance`, `eff_mob_tolerance_mode`, `buffer_code`, `polarity`, `ionization_mode` |
| `POST /api/cems-rmt-search` | `masses`, `tolerance`, `tolerance_mode`, `rmt`, `rmt_tolerance`, `rmt_tolerance_mode`, `buffer`, `polarity`, `rmt_reference`, `ion_mode` |
| `POST /api/cems-1-marker` and `/api/cems-2-marker` | `masses`, `tolerance`, `tolerance_mode`, `mt`, `mt_tolerance`, `mt_tolerance_mode`, `buffer`, `polarity`, `ion_mode`, plus marker fields |
| `POST /api/cems-rmt-1-marker` and `/api/cems-rmt-2-marker` | `masses`, `tolerance`, `tolerance_mode`, `rmt`, `rmt_tolerance`, `rmt_tolerance_mode`, `buffer`, `polarity`, `rmt_reference`, `ion_mode`, plus marker fields |

`chemical_alphabet` is also available on these CE-MS requests. The current
routes still require all other branch-specific fields, enums, and validation
rules described below; this list is not a guarantee of full CMM 3.0 payload
compatibility.

### 1.3 Legacy capabilities without a corresponding current endpoint

The search controllers and request models in this branch do not expose
dedicated CMM 3.0 routes or request options for:

- `/api/spectralquality`;
- `/api/pathwaydisplayer`;
- `/api/oxidationlong` and `/api/oxidationshort`;
- `GET /api/test` (the metadata statistics route is not a health-check substitute);
- neutral-mass search mode;
- a peptide-specific search option;
- Metlin or MINE database filters.

The current API does have related but different capabilities: browse and
compound annotations can include pathway data; MS searches can filter by
database, formula alphabet, and lipid-only versus all-compound selection; and
LC-MS/MS can score MS/MS matches using retention time. These are not equivalent
to the missing CMM 3.0 features listed above. `NEUTRAL` exists in the shared
ionization enum, but search services reject it for the mass-spectrometry
searches described here.

The legacy operation paths are declared in the original
[API controller](https://github.com/albertogilf/ceuMassMediator/blob/f8ff0f1a7c4ff0d2dd933afcd63682dc8ab2ef4e/ceu-mass-mediator-v2.0/src/java/services/rest/api/APIServices.java).
The original [database enum](https://github.com/albertogilf/ceuMassMediator/blob/f8ff0f1a7c4ff0d2dd933afcd63682dc8ab2ef4e/ceu-mass-mediator-v2.0/src/java/services/rest/api/request/Database.java)
and [metabolite enum](https://github.com/albertogilf/ceuMassMediator/blob/f8ff0f1a7c4ff0d2dd933afcd63682dc8ab2ef4e/ceu-mass-mediator-v2.0/src/java/services/rest/api/request/MetabolitesType.java)
establish the older selection options. Current responses may include `rtPred`,
but this branch exposes no standalone retention-time prediction endpoint.

## 2. Calling the API

- **Base path:** every route is under `/api`; configure the host/base URL in
  the client or deployment.
- **Content type:** send `Content-Type: application/json` for `POST` requests.
- **Search requests:** use `POST` with a JSON body. Metadata and compound lookup
  use `GET`.
- **Naming:** most routes use camelCase. The CE-MS request DTOs explicitly
  accept snake_case properties such as `mz_values`, `buffer_code`, and
  `ion_mode`.
- **Enum parsing:** most Java enums require their current enum name, usually
  uppercase. Some fields have explicit case-insensitive parsing; each is noted
  in the endpoint section.
- **Unknown properties:** do not rely on extra or legacy fields being rejected;
  they may be ignored and therefore have no effect.
- **Adducts:** use canonical names returned by `GET /api/adducts` or
  `GET /api/metadata/ccs-adducts`. Search requests do not support an `"all"`
  adduct shortcut.
- **Authentication and CORS:** no authentication or CORS configuration is
  defined in this repository. Deployment-level proxies may add behavior not
  documented here.
- **Limits:** m/z tolerances are limited to 100 PPM or 100 MDA in services
  using the shared tolerance guard. GC-MS applies a separate retention-index
  tolerance limit of 100 percent. No general batch-size limit is declared.
- **Success:** implemented operations return HTTP 200 with JSON. No-match
  searches return their usual response envelope with empty annotation lists;
  compound lookup returns 404 when the id does not exist.
- **Ordering and paging:** feature lists follow input order. Candidate ordering
  is not generally guaranteed; only GC-MS explicitly sorts by descending
  cosine score, and CE-MS ranks candidates within each adduct group. No route
  accepts pagination parameters or returns a continuation token.
- **Adduct spelling:** canonical names are case-sensitive; surrounding spaces
  are trimmed. The adduct must belong to the requested ionization mode.
- **Database filters:** selections are combined with OR. `ALL` requires at
  least one supported external database identifier; it is not an unfiltered
  scan of every compound. CCS, GC-MS, MS/MS, and CE-MS requests do not accept
  the generic `databases` or `metaboliteType` filters.

For local development, the [POM](pom.xml) requires **Java 25** (the README's
Java 24 requirement is stale), Maven, and a populated PostgreSQL database.
Configure `application-local.yml` from the provided template, then run
`mvn spring-boot:run`. Alternatively use the `prod` profile and
`CMM_DB_URL`, `CMM_DB_USERNAME`, `CMM_DB_PASSWORD`. With no port/context-path
override, the local URL is `http://localhost:8080/api`.

```bash
export CMM_BASE_URL='http://localhost:8080'
curl --fail-with-body "$CMM_BASE_URL/api/adducts"
curl --fail-with-body "$CMM_BASE_URL/api/compounds/batch-search" \
  -H 'Content-Type: application/json' \
  --data-binary @src/test/resources/json/msSearch/requestMSSearchBatch1.json
```

### 2.1 Units and tolerance windows

| Measurement | Convention implemented in this branch |
|---|---|
| m/z and neutral mass | Numeric values; `MDA` is milliDaltons (`tolerance / 1000` Da), `PPM` uses `reference × tolerance / 1,000,000`. |
| Precursor/MS search window | Applied around the adduct-derived **neutral mass**; fragment comparison uses fragment m/z. |
| CCS | `PERCENTAGE` gives ±`ccs × tolerance / 100`; `ABSOLUTE` uses the same units as the submitted/library CCS. The DTO does not encode a unit. |
| CE mobility | `PERCENTAGE` gives ±`abs(mobility) × tolerance / 100`; `ABSOLUTE` uses the library mobility units. |
| Relative migration time | Dimensionless ratio; percentage or absolute ratio tolerance. |
| Migration times | Marker services use a common time scale (their tolerance helper names it minutes); absolute tolerance uses that scale. |
| CE temperature | Celsius; positive numeric value, rounded with `Math.round` for database lookup; select a supported condition from metadata. |
| Retention time | Required for LC scoring, but no unit conversion or positivity constraint is applied. Use a consistent chromatographic time scale across features. |
| GC retention index | Positive index; its tolerance is always a percentage, not an absolute index difference. |

One-marker calibration uses `markerMobility + (capillary_length² /
capillary_voltage) × (1/time − 1/marker_time)` with the numbers supplied.
The [one-marker schema](src/main/resources/json-schemas/CEMS1Marker_request.schema.json)
describes capillary length in **mm**, voltage in **kV**, and migration times
in **minutes**. The implementation uses those numbers directly without unit
conversion; match the curated mobility convention. The checked-in marker
fixture uses length `1000`, voltage `30`, and marker time `14.24`.

### 2.2 Common errors

The global exception handler returns Spring `ProblemDetail` responses
(normally `application/problem+json`) with `type`, `title`, `status`, and
`detail` and normally `instance`; validation errors also include an `errors`
object. Example:

```json
{
  "type": "about:blank",
  "title": "Validation failed",
  "status": 400,
  "detail": "Request validation failed",
  "instance": "/api/compounds/simple-search",
  "errors": {"mz": "must be greater than 0"}
}
```

| HTTP status | Title | Typical cause |
|---|---|---|
| 400 | `Validation failed` | Bean-validation error; response includes an `errors` object keyed by field. |
| 400 | `Invalid request payload` | Malformed JSON or invalid enum/property value during deserialization. |
| 400 | `Invalid request` | An `IllegalArgumentException`; `detail` contains the exception message. |
| Varies | `Request failed` | A `ResponseStatusException`; status and `detail` reflect the service error. |
| 500 | `Internal server error` | Unexpected exception; `detail` is `Unexpected server error`. |

Compound lookup uses 404 / `Request failed` for an unknown positive id.
This table describes the explicitly handled exceptions in
[GlobalExceptionHandler](src/main/java/ceu/biolab/cmm/config/GlobalExceptionHandler.java).
Other MVC failures (for example a query-parameter type mismatch) are not
individually mapped there and can reach the generic 500 handler; clients
should use both status and body rather than assuming every invalid request
is handled uniformly.

### 2.3 Common enum values

| Property | Values |
|---|---|
| `mzToleranceMode`, `toleranceModePrecursorIon`, `toleranceModeFragments` | `PPM`, `MDA` |
| `ionizationMode` | `POSITIVE`, `NEGATIVE`; `NEUTRAL` is not accepted by these search services |
| `databases` | `ALL`, `HMDB`, `LIPIDMAPS`, `KEGG`, `INHOUSE`, `ASPERGILLUS`, `FAHFA`, `CHEBI`, `PUBCHEM`, `NPATLAS` |
| `metaboliteType` | `ALL`, `ONLYLIPIDS` |
| `formulaType` | `CHNOPS`, `CHNOPSD`, `CHNOPSCL`, `CHNOPSCLD`, `ALL`, `ALLD` |
| LC-MS `chemicalAlphabet` / `formulaType` | A `FormulaType` enum. Normally use `CHNOPS`, `CHNOPSCL`, or `ALL` and set `deuterium: true` to append D. Already D-suffixed values require `deuterium: false`. |
| `CIDEnergy` | `LOW`, `MED`, `HIGH`, `ALL` |
| `scoreType` | `COSINE`, `MODIFIED_COSINE` |
| MS/MS `spectrumSource` | `ALL`, `experimental`, `predicted` (case-insensitive) |
| CCS `ccsToleranceMode` | `PERCENTAGE`, `ABSOLUTE` |
| CCS `bufferGas` | `N2`, `HE` |
| GC-MS `derivatizationMethod` | `METHYL_CHLOROFORMATE`, `TMS`, `TBDMS_DERIVATIZATION` |
| GC-MS `columnType` | `SEMISTANDARD_NON_POLAR`, `STANDARD_NON_POLAR`, `STANDARD_POLAR` |
| CE-MS `polarity` | `Direct`, `Reverse` (case-insensitive) |
| CE-MS `ionization_mode` / `ion_mode` | Positive or negative ionization; setters normalize the input |
| CE-MS mobility, migration-time, and RMT tolerance modes | `PERCENTAGE` or `ABSOLUTE`; the corresponding setters accept common case variations |

`experimentParameters` is an optional object used by scoring workflows. Its
recognized properties are `ionMode` and `modifierType`; for example:

```json
{
  "ionMode": "POSITIVE",
  "modifierType": "NONE"
}
```

Modifier strings for the LC-MS search are `none`, `NH3`, `HCOO`, `CH3COO`,
`HCOONH3`, and `CH3COONH3`.
In `experimentParameters`, `modifierType` instead uses the enum names `NONE`,
`NH3`, `HCOO`, `CH3COO`, `HCOONH3`, `CH3COONH3`. Both properties are
case-sensitive, so LC-MS `modifiersType: "none"` and nested
`modifierType: "NONE"` intentionally differ.

## 3. Endpoint reference

Each alias below uses exactly the same body and response as its primary
route. Paths are case-sensitive. Response keys are also case-sensitive;
`msfeatures`, `imFeatures`, and `ceFeatures` deliberately have different casing.

| Method | Primary path | Aliases | Success body | Detail |
|---|---|---|---|---|
| GET | `/api/adducts` | `/api/get/adducts`, `/api/metadata/adducts` | `{positive, negative}` | [3.1](#31-adduct-catalog) |
| GET | `/api/metadata/ccs-adducts` | `/api/get/ccs-adducts` | `{positive, negative}` | [3.2](#32-ccs-adduct-catalog) |
| GET | `/api/metadata/ce-ms-buffers` | `/api/get/ce-ms-buffers` | Buffer array | [3.3](#33-ce-ms-buffers-and-condition-options) |
| GET | `/api/metadata/ce-ms-options` | `/api/get/ce-ms-options` | `{conditions}` | [3.3](#33-ce-ms-buffers-and-condition-options) |
| GET | `/api/metadata/stats` | `/api/get/stats` | Count object | [3.4](#34-database-statistics) |
| POST | `/api/compounds/simple-search` | — | `{msfeatures}` | [3.5](#35-single-feature-ms-search) |
| POST | `/api/compounds/batch-search` | — | `{msfeatures}` | [3.6](#36-batch-ms-search) |
| POST | `/api/lcms-search` | `/api/batch-advanced-search` | Annotated-feature array | [3.7](#37-lc-ms-annotation-and-scoring) |
| POST | `/api/score-annotations` | — | Annotated-feature array | [3.8](#38-score-annotations-without-searching) |
| POST | `/api/msms-search` | `/api/MSMSSearch` | `{msmsList, experimentalSpectrum}` | [3.9](#39-msms-search) |
| POST | `/api/lcmsms-search` | — | `{msmsFeatures, …}` | [3.10](#310-lc-msms-search-and-retention-time-scoring) |
| POST | `/api/browse-search` | `/api/browseSearch` | `{compoundlist}` | [3.11](#311-browse-compounds) |
| POST | `/api/imms-search` | `/api/ccs` | `{imFeatures}` | [3.12](#312-ccs--ion-mobility-search) |
| POST | `/api/lcimms-search` | `/api/ccs/lcms-score` | `{imFeatures}` | [3.13](#313-lc-im-ms-search-and-scoring) |
| POST | `/api/gcms-search` | `/api/gcms` | GC-MS result object | [3.14](#314-gc-ms-search) |
| POST | `/api/cems-search` | `/api/CEMSSearch` | `{ceFeatures}` | [3.15](#315-ce-ms-search-by-effective-mobility) |
| POST | `/api/cems-rmt-search` | `/api/CEMSRMTSearch` | `{ceFeatures}` | [3.16](#316-ce-ms-search-by-relative-migration-time) |
| POST | `/api/cems-1-marker` | `/api/CEMS1Marker` | `{ceFeatures}` | [3.17](#317-ce-ms-marker-searches-using-migration-time) |
| POST | `/api/cems-2-marker` | `/api/CEMS2Marker` | `{ceFeatures}` | [3.17](#317-ce-ms-marker-searches-using-migration-time) |
| POST | `/api/cems-rmt-1-marker` | `/api/CEMSRMT1Marker` | `{ceFeatures}` | [3.18](#318-ce-ms-marker-searches-using-relative-migration-time) |
| POST | `/api/cems-rmt-2-marker` | `/api/CEMSRMT2Marker` | `{ceFeatures}` | [3.18](#318-ce-ms-marker-searches-using-relative-migration-time) |
| GET | `/api/compounds/{compoundId}` | — | Compound object | [3.19](#319-compound-lookup-by-id) |

### 3.1 Adduct catalog

**`GET /api/adducts`**

Aliases: `GET /api/get/adducts`, `GET /api/metadata/adducts`.

Returns the canonical positive- and negative-mode adducts, ordered by search
priority:

```json
{
  "positive": ["[M+H]+", "[M+Na]+"],
  "negative": ["[M-H]-", "[M+Cl]-"]
}
```

The abbreviated arrays above illustrate the response shape, not the complete
catalog. Fetch this route instead of hard-coding the full list.

### 3.2 CCS adduct catalog

**`GET /api/metadata/ccs-adducts`**

Alias: `GET /api/get/ccs-adducts`.

Returns the positive and negative canonical adducts present in the CCS data,
using the same response shape as `/api/adducts`. This can be a narrower list
than the general adduct catalog.

### 3.3 CE-MS buffers and condition options

**`GET /api/metadata/ce-ms-buffers`**

Alias: `GET /api/get/ce-ms-buffers`.

Returns available buffer records as an array of objects with `id`, `code`, and
`description`.

**`GET /api/metadata/ce-ms-options`**

Alias: `GET /api/get/ce-ms-options`.

Optional query parameters:

| Parameter | Meaning |
|---|---|
| `buffer` | Filter by buffer code |
| `temperature` | Integer (`Long`) temperature filter; decimals do not bind. |
| `polarity` | Filter by CE polarity (`Direct` or `Reverse`) |
| `ionization_mode` | Filter by ionization mode |

Returns `{"conditions":[...]}`. Each condition contains a condition `key`,
buffer, temperature, polarity, ionization mode, `markerCompounds`, and
`rmtReferenceCompounds`. A compound option contains `compoundId` and `name`.
Filters may be omitted to return all available conditions.
`buffer` is trimmed and uppercased; blank string filters are omitted.
Polarity and ionization filters are case-insensitive; neutral ionization is
rejected. Use option **names**, not `compoundId`, for marker and RMT-reference
request fields. An unmatched condition filter returns `{"conditions":[]}`.

### 3.4 Database statistics

**`GET /api/metadata/stats`**

Alias: `GET /api/get/stats`.

Returns the database counts `compounds`, `gcmsSpectra`, `msmsSpectra`,
`ccsRecords`, and `cemsRecords`. This is a statistics endpoint, not a health
check or version endpoint.

### 3.5 Single-feature MS search

**`POST /api/compounds/simple-search`**

Searches compounds by one measured m/z and the requested adducts, databases,
and compound selection.

| Property | Type | Required | Description |
|---|---|---:|---|
| `mz` | number | yes | Measured m/z; must be positive. |
| `mzToleranceMode` | enum | yes | `PPM` or `MDA`. |
| `tolerance` | number | yes | Positive; maximum 100 in the selected mode. |
| `ionizationMode` | enum | yes | `POSITIVE` or `NEGATIVE`. |
| `adductsString` | string array | yes | Non-empty canonical adduct names. |
| `databases` | enum array | yes | Non-empty database selection. |
| `metaboliteType` | enum | yes | `ALL` or `ONLYLIPIDS`. |
| `formulaType` | enum | no | Optional chemical formula filter. |
| `detectedAdduct` | string | no | Additional canonical adduct candidate; it does not replace `adductsString` or restrict results to that adduct. |

Response: an object containing `msfeatures`, an array with one annotated
feature. Each feature contains the measured feature and annotations grouped by
adduct. Example request:
[requestMSSearchSimple1.json](src/test/resources/json/msSearch/requestMSSearchSimple1.json).

```json
{
  "mz": 178.1219,
  "mzToleranceMode": "PPM",
  "tolerance": 10,
  "ionizationMode": "POSITIVE",
  "adductsString": ["[M+H]+", "[M+Na]+"],
  "databases": ["HMDB"],
  "metaboliteType": "ALL"
}
```

`formulaType` and `detectedAdduct` may be omitted or set to `null`. Formula
filters compare parsed chemical elements; compounds without a usable formula
are retained. `ALL`/`ALLD` disable that element filter. A valid no-match request
still returns the feature and adduct groups with empty `annotations` lists.

### 3.6 Batch MS search

**`POST /api/compounds/batch-search`**

Performs the single-feature MS search for each submitted m/z, sequentially.
Use the same properties and constraints as the single-feature route, except:

| Property | Type | Required | Description |
|---|---|---:|---|
| `mzValues` | positive-number array | yes | Non-empty list of measured m/z values. Null entries are rejected. |

The response is the same `msfeatures` object, with one result feature per input
m/z in request order. The current implementation does not impose a list-size
cap. Example request:
[requestMSSearchBatch1.json](src/test/resources/json/msSearch/requestMSSearchBatch1.json).

### 3.7 LC-MS annotation and scoring

**`POST /api/lcms-search`**

Alias: `POST /api/batch-advanced-search`.

Annotates a batch of LC-MS features, detects likely adducts from each composite
spectrum, searches the selected databases, and scores the resulting
annotations.

| Property | Type | Required | Description |
|---|---|---:|---|
| `mz` | positive-number array | yes | Measured feature m/z values. |
| `mzToleranceMode` | enum | yes | `PPM` or `MDA`. |
| `tolerance` | number | yes | Positive; maximum 100. |
| `ionizationMode` | enum | yes | `POSITIVE` or `NEGATIVE`. |
| `adductsString` | string array | yes | Non-empty canonical adduct names. |
| `databases` | enum array | yes | Non-empty database selection. |
| `metaboliteType` | enum | yes | `ALL` or `ONLYLIPIDS`. |
| `retentionTimes` | number array | yes | One retention time per m/z, in the same order. |
| `compositeSpectrum` | map array | yes | One non-empty m/z-to-intensity map per feature. Keys are m/z values represented as JSON object keys; intensities must be non-negative. |
| `chemicalAlphabet` | enum | yes, or use `formulaType` | Formula alphabet such as `CHNOPS`. Both setters populate the same field; supply only one. |
| `deuterium` | boolean | no | Whether the formula alphabet includes deuterium; defaults to `false`. |
| `modifiersType` | string | yes | One of the LC-MS modifier values in section 2.3. |
| `detectedAdduct` | string | no | Accepted by the DTO but ignored by this workflow: the service calculates detection from each composite spectrum. |

The m/z, retention-time, and composite-spectrum arrays must have matching
lengths. The response is a JSON array of annotated features, in request order.
The former CMM 3.0 auxiliary arrays for non-significant features are not part
of this request contract. Example request:
[requestLCMSSearch.json](src/test/resources/json/lcmsSearch/requestLCMSSearch.json).

```json
{
  "mz": [400.3432],
  "mzToleranceMode": "PPM",
  "tolerance": 10,
  "ionizationMode": "POSITIVE",
  "adductsString": ["[M+H]+", "[M+Na]+"],
  "databases": ["HMDB"],
  "metaboliteType": "ALL",
  "retentionTimes": [18.842525],
  "compositeSpectrum": [{"400.3432": 307034.88, "422.3251": 1000}],
  "chemicalAlphabet": "CHNOPS",
  "deuterium": false,
  "modifiersType": "none"
}
```

### 3.8 Score annotations without searching

**`POST /api/score-annotations`**

Re-scores annotations supplied by the caller; it does not search the compound
database.

| Property | Type | Required | Description |
|---|---|---:|---|
| `features` | annotated-feature array | yes | Non-empty list in the current `AnnotatedFeature` shape. |
| `experimentParameters` | object or `null` | no | Optional experiment ion mode and modifier type. |

Returns the feature array with scores applied. Rules are loaded from the
lipid rule workbook, but the service creates score containers for both lipid
and non-lipid compounds. Only features with retention time enter rule scoring;
plain MS features are skipped. Missing or inapplicable scores may be `null`.
Example request:
[request_simple.json](src/test/resources/json/scoreAnnotations/request_simple.json).

Provide `feature.mzValue`, `feature.rtValue` (or `retentionTime`),
`annotationsByAdducts[].adduct`, and `annotations[].compound` with the
properties needed by the rules. No database lookup fills missing compound
data. Omit `scores` or send `[]`: score entries deserialize through the
`IScore` interface, which has no JSON subtype mapping. This is not a general
round-trip endpoint for arbitrary previously scored responses. Its feature
deserializer recognizes MS/LC-MS properties only; it does not preserve CCS or
CE-specific feature fields.

### 3.9 MS/MS search

**`POST /api/msms-search`**

Alias: `POST /api/MSMSSearch`.

Searches experimental or predicted tandem-MS library spectra for one precursor.

| Property | Type | Required | Description |
|---|---|---:|---|
| `CIDEnergy` | enum | yes | `LOW`, `MED`, `HIGH`, or `ALL` (10, 20, 40 V, or all). |
| `precursorIonMZ` | number | yes | Positive precursor m/z. |
| `tolerancePrecursorIon` | number | yes | Positive; maximum 100. |
| `toleranceModePrecursorIon` | enum | yes | `PPM` or `MDA`. |
| `toleranceFragments` | number | yes | Positive; maximum 100. |
| `toleranceModeFragments` | enum | yes | `PPM` or `MDA`. |
| `ionizationMode` | enum | yes | `POSITIVE` or `NEGATIVE`. |
| `adducts` | string array | yes | Non-empty canonical adduct names. |
| `fragmentsMZsIntensities` | spectrum object | yes | `{"peaks":[{"mz": number, "intensity": number}]}`; at least one peak; positive m/z and non-negative intensity. Optional inner `precursorMz` does not override top-level `precursorIonMZ`. |
| `scoreType` | enum | yes | `COSINE` or `MODIFIED_COSINE`. |
| `spectrumSource` | enum/string | no | Defaults to `ALL` when omitted; explicit `null` is invalid. `ALL`, `experimental`, or `predicted`; case-insensitive. |

Response: `msmsList` with matching library annotations and
`experimentalSpectrum` with the submitted spectrum representation. Example
request:
[request1.json](src/test/resources/json/msmsSearch/request1.json).

Only hits with similarity ≥0.5 are returned. The repository keeps the highest
scoring library spectrum **per compound id**, across adducts, sources, and
energies; ties and final hit ordering are unspecified. `spectrumSource` in
each hit identifies the retained library spectrum. This route does not accept
a generic database selection or an instrument filter.

### 3.10 LC-MS/MS search and retention-time scoring

**`POST /api/lcmsms-search`**

Uses the MS/MS search settings in section 3.9, with retention-time scoring.
Submit exactly one feature form or one batch form.

**Single feature properties:** `precursorIonMZ`, `fragmentsMZsIntensities`,
`rtValue`.

**Batch properties:** `precursorIonMZValues`,
`fragmentsMZsIntensitiesList`, and `rtValues`. All three lists must be
non-empty and have the same length.

Both forms also require `CIDEnergy`, both tolerances and modes,
`ionizationMode`, `scoreType`, **`spectrumSource`**, and a non-empty **`adducts`**
list. Unlike plain MS/MS, this DTO has no default spectrum source. Although
`adducts` lacks `@NotEmpty` here, the service requires it. `experimentParameters`
is optional and used for LC scoring; missing experiment ion mode defaults to
the top-level request ionization mode.
The response contains `msmsFeatures`; each entry has `feature`, `msmsList`, and
`experimentalSpectrum`. For a one-feature request, the response also includes
the top-level `msmsList` and `experimentalSpectrum`. Example batched request:
[request_lcmsms_score_multiple_features.json](src/test/resources/json/msmsSearch/request_lcmsms_score_multiple_features.json).

The one-feature top-level fields are also emitted for a batch of length one;
they are absent for larger batches. Each feature is searched and scored
independently, so RT comparisons are not performed jointly across all batch
features. The similarity threshold and best-per-compound behavior are the
same as plain MS/MS.

### 3.11 Browse compounds

**`POST /api/browse-search`**

Alias: `POST /api/browseSearch`.

Searches compound names and/or formulas across selected databases.

| Property | Type | Required | Description |
|---|---|---:|---|
| `compoundName` | string | one search term required | If used, at least three non-space characters. `exactName: false` uses case-insensitive contains; `true` uses exact matching. |
| `formula` | string | one search term required | Non-blank formula; matched with SQL `LIKE`. |
| `databases` | enum array | yes | Non-empty database selection. |
| `metaboliteType` | enum | no | Defaults to `ALL` when omitted; explicit `null` is invalid. `ALL` or `ONLYLIPIDS`. |
| `exactName` | boolean | no | Defaults to `false`. |
| `formulaType` | enum | no | Optional formula-element filter. |

Response: an object containing `compoundlist`, an array of compounds. The
repository query does not declare a result cap. Example request:
[request1.json](src/test/resources/json/browseSearch/request1.json).

When both a usable name and formula are supplied, both filters must match
(AND). Names shorter than three trimmed characters are ignored if a valid
formula is supplied. `exactName: true` uses case-sensitive SQL `LIKE` without
adding wildcards; `false` uses `ILIKE` with surrounding `%`. SQL `%` and `_`
wildcards supplied by the caller retain their meaning, including with
`exactName: true`. Formula uses case-sensitive `LIKE` after trimming. Results
are deduplicated by compound and have no promised sort order.

### 3.12 CCS / ion-mobility search

**`POST /api/imms-search`**

Alias: `POST /api/ccs`.

Searches compounds by paired m/z and collisional cross-section measurements.

| Property | Type | Required | Description |
|---|---|---:|---|
| `mzValues` | positive-number array | yes | Measured m/z values. |
| `mzTolerance` | number | yes | Positive; maximum 100 in the selected m/z mode. |
| `mzToleranceMode` | enum | yes | `PPM` or `MDA`. |
| `ccsValues` | positive-number array | yes | One CCS value per m/z value. |
| `ccsTolerance` | number | yes | Positive CCS tolerance. |
| `ccsToleranceMode` | enum | yes | `PERCENTAGE` or `ABSOLUTE`. |
| `ionizationMode` | enum | yes | `POSITIVE` or `NEGATIVE`. |
| `bufferGas` | enum | yes | `N2` or `HE`. |
| `adducts` | string array | yes | Non-empty canonical adducts available for this mode. |

The `mzValues` and `ccsValues` arrays must have matching lengths. Response:
`{"imFeatures":[...]}`. Example request:
[request1.json](src/test/resources/json/ccsSearch/request1.json).
CCS is not bounded by a maximum percentage in this implementation. Each
candidate compound includes `dbCcs`; the annotation's mass error is still
calculated on neutral mass, not CCS. No-match features are retained.

### 3.13 LC-IM-MS search and scoring

**`POST /api/lcimms-search`**

Alias: `POST /api/ccs/lcms-score`.

Uses the CCS request fields in section 3.12 and additionally requires
`rtValues`, a non-empty retention-time list with one value per feature.
Optional `experimentParameters` enables experiment-aware scoring. Optional
`compositeSpectrum` supplies one m/z-to-intensity map per feature; when present,
the list length must match `mzValues`, and each map must contain at least one
peak. The composite spectra can help prioritize adduct detection.
Empty or `null` `compositeSpectrum` means no composite spectra; when supplied,
keys must be positive m/z values and intensities non-negative. Detected
adducts are moved to the front of each feature's group list; all requested
adducts are still searched.

Response: `{"imFeatures":[...]}` with LC-IM-MS feature information and
annotations/scores. Example request:
[request_lcimms_score_with_composite_spectrum.json](src/test/resources/json/ccsSearch/request_lcimms_score_with_composite_spectrum.json).

### 3.14 GC-MS search

**`POST /api/gcms-search`**

Alias: `POST /api/gcms`.

Matches an experimental GC-MS spectrum against the configured spectral library
using retention index and GC method settings.

| Property | Type | Required | Description |
|---|---|---:|---|
| `gcmsSpectrumExperimental` | spectrum object | yes | `{"spectrum":[{"mzValue": number, "intensity": number}]}`. |
| `retentionIndex` | number | positive | Experimental retention index. |
| `retentionIndexTolerance` | number | positive | Retention-index tolerance; service limit is 100 percent. |
| `derivatizationMethod` | enum | no | Defaults to `METHYL_CHLOROFORMATE`. Also `TMS` or `TBDMS_DERIVATIZATION`. Explicit `null` is invalid. |
| `columnType` | enum | no | Defaults to `SEMISTANDARD_NON_POLAR`. Also `STANDARD_NON_POLAR` or `STANDARD_POLAR`. Explicit `null` is invalid. |

Response includes the submitted `gcmsSpectrumExperimental`, matching
`gcmsAnnotations`, and `riexperimental`. An annotation includes the GC-MS
compound, cosine score, experimental retention index, and retention-index
difference. Example request:
[requestGCMSSearch.json](src/test/resources/json/gcmsSearch/requestGCMSSearch.json).

Peak m/z must be positive and intensity non-negative. Supply a non-empty
spectrum for useful results; this DTO does not enforce a minimum peak count.
The service uses a fixed 100 mDa fragment-matching tolerance, retains each
compound's best library-spectrum cosine score, filters at ≥0.3 by default
(`cmm.gcms.score-threshold`, configurable from 0 to 1), and sorts descending.

### 3.15 CE-MS search by effective mobility

**`POST /api/cems-search`**

Alias: `POST /api/CEMSSearch`.

| Property | JSON name | Type | Required | Description |
|---|---|---|---:|---|
| `bufferCode` | `buffer_code` | string | yes | CE-MS buffer code; see the buffers metadata endpoint. |
| `polarity` | `polarity` | string | yes | `Direct` or `Reverse`. |
| `chemicalAlphabet` | `chemical_alphabet` | string | no | Optional formula alphabet. |
| `ionizationMode` | `ionization_mode` | string | yes | Positive or negative ionization; parsed case-insensitively. |
| `adducts` | `adducts` | string array | yes | Non-empty canonical adducts. |
| `mzValues` | `mz_values` | positive-number array | yes | Input m/z values. |
| `effectiveMobilities` | `effective_mobilities` | positive-number array | yes | One mobility per m/z. |
| `mzTolerance` | `mz_tolerance` | number | yes | Positive m/z tolerance, at most 100 in the selected mode. |
| `mzToleranceMode` | `mz_tolerance_mode` | string | yes | `PPM` or `MDA`; parsed case-insensitively. |
| `effectiveMobilityTolerance` | `eff_mob_tolerance` | number | yes | Positive mobility tolerance. |
| `effectiveMobilityToleranceMode` | `eff_mob_tolerance_mode` | string | yes | `PERCENTAGE` or `ABSOLUTE`. |
| `temperature` | `temperature` | number | yes | Positive temperature. |

The m/z and mobility arrays must correspond feature-by-feature. Response:
`{"ceFeatures":[...]}`. Example request:
[CEMSSearch_request1.json](src/test/resources/json/cemsSearch/CEMSSearch_request1.json).

```json
{
  "buffer_code": "FORMIC_ACID_1M",
  "temperature": 20,
  "polarity": "Direct",
  "ionization_mode": "positive",
  "adducts": ["[M+H]+"],
  "mz_values": [291.1299],
  "effective_mobilities": [20.0],
  "mz_tolerance": 10,
  "mz_tolerance_mode": "ppm",
  "eff_mob_tolerance": 10,
  "eff_mob_tolerance_mode": "percentage",
  "chemical_alphabet": "CHNOPS"
}
```

Use the **JSON name** column for requests. Jackson also exposes
`effectiveMobilityToleranceMode` as an enum-valued alternative to
`eff_mob_tolerance_mode`; this alternative requires uppercase `PERCENTAGE` or
`ABSOLUTE`. Supply only one of these properties. Mobility tolerances using
the snake_case setter also accept `percent` or `%` for percentage mode.
A missing, blank,
`ALL`, or `ALLD` chemical alphabet disables element filtering; formulas that
cannot be parsed are retained. Within an adduct, the service keeps one
candidate per compound id, sorts by `abs(candidateMass − targetMass) +
abs(candidateMobility − targetMobility)`, and assigns one-based ranks. This
ranking is not a spectral or calibrated statistical score.

### 3.16 CE-MS search by relative migration time

**`POST /api/cems-rmt-search`**

Alias: `POST /api/CEMSRMTSearch`.

| JSON property | Type | Required | Description |
|---|---|---|---|
| `masses` | number array | yes | Positive measured m/z values, not neutral masses. |
| `tolerance` | number | yes | Positive m/z tolerance, at most 100. |
| `tolerance_mode` | string | yes | `ppm` or `mda`, case-insensitive. |
| `rmt` | number array | yes | Positive relative migration times, one per m/z. |
| `rmt_tolerance` | number | yes | Positive percentage or absolute ratio tolerance. |
| `rmt_tolerance_mode` | string | yes | `percentage` (also `percent`, `%`) or `absolute`, case-insensitive. |
| `buffer` | string | yes | Non-blank buffer code. |
| `temperature` | number | yes | Positive temperature. |
| `polarity` | string | yes | `Direct` or `Reverse`, case-insensitive. |
| `rmt_reference` | string | yes | Non-blank reference compound **name**, selected from condition metadata. |
| `ion_mode` | string | yes | `positive` or `negative`, case-insensitive. |
| `adducts` | string array | yes | Non-empty canonical adducts. |
| `chemical_alphabet` | string | no | Optional element filter; same behavior as effective-mobility search. |

The `masses` and `rmt` arrays correspond by index. Response:
`{"ceFeatures":[...]}`. Example request:
[CEMSRMT_request1.json](src/test/resources/json/cemsSearch/CEMSRMT_request1.json).
An unknown `rmt_reference` produces HTTP 400. This route searches stored RMT
records directly; it does not run marker calibration. The returned feature
uses `effectiveMobility: 0.0` as a placeholder, not a measured mobility. RMT
information is carried by the annotations (section 4.2).

### 3.17 CE-MS marker searches using migration time

**One marker:** `POST /api/cems-1-marker` (alias `/api/CEMS1Marker`).

**Two markers:** `POST /api/cems-2-marker` (alias `/api/CEMS2Marker`).

Both routes use `masses`, `tolerance`, `tolerance_mode`, `mt`,
`mt_tolerance`, `mt_tolerance_mode`, `buffer`, `temperature`, `polarity`,
`chemical_alphabet` (optional), `ion_mode`, and non-empty `adducts`.
`masses` and `mt` are positive-number arrays of corresponding features.
Tolerance modes are m/z `ppm`/`mda` and migration-time `percentage`/`absolute`.

The one-marker request additionally requires `marker`, `marker_time`,
`capillary_length`, and `capillary_voltage`. The two-marker request instead
requires `marker1`, `marker1_time`, `marker2`, and `marker2_time`.

| JSON property | Type | Constraint |
|---|---|---|
| `masses`, `mt` | number arrays | Non-empty, equal length, positive, no null elements. `masses` contains measured m/z. |
| `tolerance`, `tolerance_mode` | number, string | `0 < tolerance ≤ 100`; `ppm` or `mda`, case-insensitive. |
| `mt_tolerance`, `mt_tolerance_mode` | number, string | Positive tolerance; `percentage` (also `percent`, `%`) or `absolute`, case-insensitive. |
| `buffer`, `polarity`, `temperature`, `ion_mode`, `adducts` | As in RMT table | All required. |
| `chemical_alphabet` | string | Optional; defaults to `ALL` if missing. |
| `marker` | string | One-marker only: non-blank compound name. |
| `marker_time`, `capillary_length`, `capillary_voltage` | numbers | One-marker only: all positive and required. |
| `marker1`, `marker2` | strings | Two-marker only: both non-blank compound names. |
| `marker1_time`, `marker2_time` | numbers | Two-marker only: positive, required, and different from each other. |

Marker mobility must exist for the requested buffer, temperature, and polarity;
otherwise the service returns 400. Names are matched case-insensitively.
The services convert migration times to effective mobilities, derive one
shared percentage mobility tolerance using the largest propagated relative
deviation across features, and delegate to effective-mobility search. They do
not query migration-time windows directly.

Each route returns `{"ceFeatures":[...]}`. Example requests:
[CEMS1Marker_request1.json](src/test/resources/json/cemsMarkers/CEMS1Marker_request1.json),
[CEMS2Marker_request1.json](src/test/resources/json/cemsMarkers/CEMS2Marker_request1.json).

### 3.18 CE-MS marker searches using relative migration time

**One marker:** `POST /api/cems-rmt-1-marker` (alias `/api/CEMSRMT1Marker`).

**Two markers:** `POST /api/cems-rmt-2-marker` (alias `/api/CEMSRMT2Marker`).

Both routes require the RMT-search fields from section 3.16 plus marker
information. For one marker, supply `marker`, `marker_time`,
`capillary_length`, and `capillary_voltage`. For two markers, supply
`marker1`, `marker1_time`, `marker2`, and `marker2_time`. The two-marker DTO
also accepts `marker` as an alias for `marker1`.
Marker strings must be non-blank and numeric marker times/capillary values
must be positive. Two marker times must differ. Both lists must be non-empty,
have equal lengths, and contain only positive non-null numbers.

The services look up both marker and reference mobilities for the requested
condition, calculate the reference migration time, convert each RMT to a
migration time, and then delegate to effective-mobility search. Missing
calibration records, a zero denominator, or a non-positive calculated
reference time produce 400. These routes return calibrated mobility in the
feature rather than the direct RMT route's zero placeholder.

Each route returns `{"ceFeatures":[...]}`. Example requests:
[CEMSRMT1Marker_request1.json](src/test/resources/json/cemsMarkers/CEMSRMT1Marker_request1.json),
[CEMSRMT2Marker_request1.json](src/test/resources/json/cemsMarkers/CEMSRMT2Marker_request1.json).

### 3.19 Compound lookup by id

**`GET /api/compounds/{compoundId}`**

Returns a compound by its positive integer CMM compound id. Invalid
non-positive ids fail request validation (400). The response is the compound
object from section 4.3, including available database identifiers and pathways.
An unknown positive id returns 404 / `Request failed` with
`detail: "No compound found with id <id>"`. Ids use Java's signed 32-bit integer
range; the path does not accept external HMDB/KEGG identifiers.

## 4. Response data shapes

### 4.1 MS and LC-MS annotated feature

MS search responses contain a list of annotated features; LC-MS returns the
annotated-feature list directly. An annotated feature is organized around a
measured feature and groups annotations by adduct:

```json
{
  "feature": {
    "mzValue": 800.5,
    "rtValue": 6.0
  },
  "annotationsByAdducts": [
    {
      "adduct": "[M+H]+",
      "annotations": [
        {
          "compound": {
            "compoundId": 1001,
            "compoundName": "example",
            "formula": "C36H70NO8P"
          },
          "massErrorPpm": -1.2,
          "scores": [
            {
              "ionizationScore": 1.0,
              "adductRelationScore": null,
              "rtScore": null,
              "finalScore": 1.0
            }
          ]
        }
      ]
    }
  ]
}
```

This is a schematic example; actual compound properties depend on the result.
Score members may be `null` when the associated rule was not applied. The
compound object can include formula, mass, identifiers, lipid properties,
classifications, and pathways; fields without data may be `null` or absent
depending on the DTO.

`feature.intensity` may be a number or `null`; plain MS search currently sets
it to `0.0`. LC responses serialize both `rtValue` and `retentionTime` with
the same numeric value. Unscored annotations normally have `scores: []`.

`massErrorPpm = (candidateNeutralMass − targetNeutralMass) /
targetNeutralMass × 1,000,000`. Positive values mean a heavier candidate.
This differs from MS/MS `deltaPpmPrecursorIon`'s convention below.

Score objects contain `ionizationScore`, `adductRelationScore`, `rtScore`, and
`finalScore`, each a number or `null`. They are rule-based values, not
identification probabilities. The final score combines available components
as a weighted geometric mean; RT weight depends on applicable comparisons.
Rules can compare features submitted together to the LC-MS and LC-IM-MS
workflows. Repeated in-memory scoring appends score objects; it does not
replace the existing list.

### 4.2 CCS, CE-MS, GC-MS, and MS/MS responses

| Workflow | Object structure and fields |
|---|---|
| CCS | `imFeatures[]` uses the annotated-feature structure. `feature` has `mzValue`, `ccsValue`, `intensity`; LC-IM-MS adds `rtValue` and `retentionTime`. The annotation's `compound` extends the CMM compound with `dbCcs`. |
| CE-MS | `ceFeatures[]` has `feature: {mzValue, effectiveMobility, intensity}` and `annotationsByAdducts[]: {adduct, annotations}`. Each annotation has `compound`, `rank`, `massErrorPpm`, `mzCalc`, `neutralMassCalc`, `mobilityErrorPct`, `rmtErrorPct`, `relativeMt`, `absoluteMt`. Inapplicable fields are `null`. There is no `scores` list in this DTO. |
| GC-MS | Top-level `gcmsSpectrumExperimental`, `gcmsAnnotations[]`, `riexperimental`. Each annotation has `gcmsCompound`, `gcmsCosineScore`, `experimentalRI`, `deltaRI`. The compound adds `dbRI`, `derivatizationMethod`, `gcColumn`, and `gcmsspectrum` (lowercase JSON key). |
| MS/MS | Top-level `msmsList[]`, `experimentalSpectrum`. Each hit has `msmsId`, `compound`, `adduct`, `spectrumSource`, `deltaPpmPrecursorIon`, `msmsCosineScore`, `collisionEnergy`, `spectrum`, and optional non-empty `scores`. |
| LC-MS/MS | `msmsFeatures[]: {feature, msmsList, experimentalSpectrum}`. The feature is LC-MS; hit/spectrum fields match MS/MS. A response with exactly one feature also includes top-level `msmsList` and `experimentalSpectrum`. |

MS/MS spectra are `{precursorMz, peaks: [{mz, intensity}]}`. GC-MS spectra
are `{spectrumId, spectrum: [{mzValue, intensity}]}`; these two representations
are not interchangeable. Experimental MS/MS `precursorMz` is set from the
top-level query. GC-MS `spectrumId` defaults to `-1` for a submitted spectrum.

`deltaPpmPrecursorIon = (queryMz − libraryMz) / libraryMz × 1,000,000`.
CE `mobilityErrorPct` and `rmtErrorPct` compare library versus submitted values
as `(library − submitted) / submitted × 100`; they are signed.
GC `deltaRI` is the absolute index difference. `msmsCosineScore` contains
whichever similarity was requested, including `MODIFIED_COSINE`.

### 4.3 Compound model

Compound-bearing routes serialize domain objects, not one uniform minimal
DTO. Fields can be null where data is unavailable, and subclasses add fields.
Base fields are declared in
[Compound](src/main/java/ceu/biolab/cmm/shared/domain/compound/Compound.java),
and external identifiers in
[CMMCompound](src/main/java/ceu/biolab/cmm/shared/domain/compound/CMMCompound.java).

| Fields | JSON types / meaning |
|---|---|
| `compoundId`, `compoundName`, `casId` | Integer CMM id, name string, optional CAS string. |
| `formula`, `mass` | Molecular formula string and numeric monoisotopic mass. |
| `chargeType`, `chargeNumber` | Integer database charge fields. |
| `formulaType`, `compoundType` | Formula enum or null; `NON_LIPID` / `LIPID`. |
| `logP`, `rtPred` | Optional numeric properties from the database; `rtPred` is not a live prediction response. |
| `inchi`, `inchiKey`, `smiles`, `mol2` | Optional structural strings. `mol2` may be null on routes that do not load it. |
| `lipidType`, `numChains`, `numCarbons`, `doubleBonds` | Optional lipid class string and integer counts. |
| `lipidMapsClassifications` | Array of `{category, mainClass, subClass, classLevel4}` strings. Some workflows normalize labels to classification codes. |
| `biologicalActivity`, `meshNomenclature`, `iupacClassification` | Optional strings. |
| `pathways` | Array of `{pathwayId, pathwayName, pathwayMap}`; these set-backed arrays have no ordering guarantee. |
| `keggID`, `lmID`, `hmdbID`, `agilentID`, `inHouseID`, `knapsackID`, `aspergillusWebName` | Optional string identifiers/names, when a CMM compound is returned. |
| `pcID`, `chebiID`, `aspergillusID`, `npatlasID`, `fahfaID`, `ohPositionID` | Optional integer database identifiers. Preserve uppercase `ID` in JSON property names. |

CE-MS also maps to `CMMCompound` and includes available external identifiers,
but initializes `pathways` to an empty set and `mol2` to null. Clients should
identify a compound with `compoundId` and tolerate additional/null properties
in workflow-specific responses.

## 5. Request and response fixtures

The JSON files below are checked-in examples of the current branch's request
and response formats. They are illustrative test data; individual fixtures
may be used by tests that require the project database.

| Workflow | Fixture location |
|---|---|
| Simple and batch MS search | [msSearch fixtures](src/test/resources/json/msSearch/) |
| LC-MS search | [lcmsSearch fixtures](src/test/resources/json/lcmsSearch/) |
| MS/MS and LC-MS/MS | [msmsSearch fixtures](src/test/resources/json/msmsSearch/) |
| Browse | [browseSearch fixtures](src/test/resources/json/browseSearch/) |
| CCS and LC-IM-MS | [ccsSearch fixtures](src/test/resources/json/ccsSearch/) |
| GC-MS | [gcmsSearch fixtures](src/test/resources/json/gcmsSearch/) |
| CE-MS effective mobility and RMT | [cemsSearch fixtures](src/test/resources/json/cemsSearch/) |
| CE-MS migration-time and RMT markers | [cemsMarkers fixtures](src/test/resources/json/cemsMarkers/) |
| Score annotations | [scoreAnnotations fixtures](src/test/resources/json/scoreAnnotations/) |
| Metadata | [metadata fixtures](src/test/resources/json/metadata/) |

## 6. Implementation sources and maintenance

The reference follows controller mappings, Jackson-visible DTO properties,
service/repository validation, SQL filtering, response domain objects, and
fixtures. For changes, review the complete call chain, not annotations alone:
several service requirements/defaults differ from DTO annotations.

| Workflow | Controller | Request/response models and behavior |
|---|---|---|
| General adduct catalog | [AdductController](src/main/java/ceu/biolab/cmm/metadata/controller/AdductController.java) | [AdductCatalogResponse](src/main/java/ceu/biolab/cmm/metadata/dto/AdductCatalogResponse.java), [AdductService](src/main/java/ceu/biolab/cmm/shared/service/adduct/AdductService.java), [CSV catalogs](src/main/resources/adducts/) |
| Metadata | [MetadataController](src/main/java/ceu/biolab/cmm/metadata/controller/MetadataController.java) | [DTOs](src/main/java/ceu/biolab/cmm/metadata/dto/), [MetadataService](src/main/java/ceu/biolab/cmm/metadata/service/MetadataService.java), [MetadataRepository](src/main/java/ceu/biolab/cmm/metadata/repository/MetadataRepository.java) |
| MS / compound lookup | [CompoundController](src/main/java/ceu/biolab/cmm/msSearch/controller/CompoundController.java) | [DTOs](src/main/java/ceu/biolab/cmm/msSearch/dto/), [CompoundRepository](src/main/java/ceu/biolab/cmm/msSearch/repository/CompoundRepository.java) |
| LC-MS | [BatchAdvancedSearchController](src/main/java/ceu/biolab/cmm/lcmsSearch/controller/BatchAdvancedSearchController.java) | [BatchAdvancedSearchRequestDTO](src/main/java/ceu/biolab/cmm/lcmsSearch/dto/BatchAdvancedSearchRequestDTO.java), [BatchAdvancedSearchService](src/main/java/ceu/biolab/cmm/lcmsSearch/service/BatchAdvancedSearchService.java) |
| Scoring | [ScoreAnnotationsController](src/main/java/ceu/biolab/cmm/scoreAnnotations/controller/ScoreAnnotationsController.java) | [ScoreAnnotationsRequest](src/main/java/ceu/biolab/cmm/scoreAnnotations/dto/ScoreAnnotationsRequest.java), [ScoreAnnotationsService](src/main/java/ceu/biolab/cmm/scoreAnnotations/service/ScoreAnnotationsService.java), [feature deserializer](src/main/java/ceu/biolab/cmm/shared/domain/msFeature/AnnotatedFeature.java), [score model](src/main/java/ceu/biolab/cmm/scoreAnnotations/domain/CompoundScores.java), [rule workbook](src/main/resources/ceu/biolab/cmm/scoreAnnotations/model/ScoreLipidsRules.drl.xlsx) |
| MS/MS / LC-MS/MS | [MSMSSearchController](src/main/java/ceu/biolab/cmm/MSMSSearch/controller/MSMSSearchController.java) | [DTOs](src/main/java/ceu/biolab/cmm/MSMSSearch/dto/), [MSMSSearchService](src/main/java/ceu/biolab/cmm/MSMSSearch/service/MSMSSearchService.java), [MSMSSearchRepository](src/main/java/ceu/biolab/cmm/MSMSSearch/repository/MSMSSearchRepository.java) |
| Browse | [BrowseSearchController](src/main/java/ceu/biolab/cmm/browseSearch/controller/BrowseSearchController.java) | [BrowseSearchRequest](src/main/java/ceu/biolab/cmm/browseSearch/dto/BrowseSearchRequest.java), [BrowseSearchRepository](src/main/java/ceu/biolab/cmm/browseSearch/repository/BrowseSearchRepository.java) |
| CCS / LC-IM-MS | [CompoundCcsController](src/main/java/ceu/biolab/cmm/ccsSearch/controller/CompoundCcsController.java) | [DTOs](src/main/java/ceu/biolab/cmm/ccsSearch/dto/), [CcsSearchService](src/main/java/ceu/biolab/cmm/ccsSearch/service/CcsSearchService.java) |
| GC-MS | [CompoundGCMSController](src/main/java/ceu/biolab/cmm/gcmsSearch/controller/CompoundGCMSController.java) | [GCMSSearchRequestDTO](src/main/java/ceu/biolab/cmm/gcmsSearch/dto/GCMSSearchRequestDTO.java), [GCMSSearchService](src/main/java/ceu/biolab/cmm/gcmsSearch/service/GCMSSearchService.java) |
| CE effective mobility | [CemsSearchController](src/main/java/ceu/biolab/cmm/CEMSSearch/controller/CemsSearchController.java) | [CemsSearchRequestDTO](src/main/java/ceu/biolab/cmm/CEMSSearch/dto/CemsSearchRequestDTO.java), [CemsSearchService](src/main/java/ceu/biolab/cmm/CEMSSearch/service/CemsSearchService.java) |
| CE RMT | [CemsRmtSearchController](src/main/java/ceu/biolab/cmm/CEMSSearch/controller/CemsRmtSearchController.java) | [CemsRmtSearchRequestDTO](src/main/java/ceu/biolab/cmm/CEMSSearch/dto/CemsRmtSearchRequestDTO.java), [CemsRmtSearchService](src/main/java/ceu/biolab/cmm/CEMSSearch/service/CemsRmtSearchService.java) |
| CE markers | [CemsMarkersController](src/main/java/ceu/biolab/cmm/CEMSMarkers/controller/CemsMarkersController.java) | [Four DTOs](src/main/java/ceu/biolab/cmm/CEMSMarkers/dto/), [marker services](src/main/java/ceu/biolab/cmm/CEMSMarkers/service/), [marker lookup](src/main/java/ceu/biolab/cmm/CEMSMarkers/repository/CemsMarkersRepository.java) |

Supporting SQL lives under [sql/](src/main/resources/sql/), shared enums under
[shared/domain/](src/main/java/ceu/biolab/cmm/shared/domain/), and m/z limits
in [MzToleranceLimits](src/main/java/ceu/biolab/cmm/shared/validation/MzToleranceLimits.java).
There is no OpenAPI/Swagger dependency, generated spec, or documentation route
configured in this branch. The OpenAPI configuration in PR #41 is separate
from the current implementation.

The [CE JSON schemas](src/main/resources/json-schemas/) are supporting
artifacts, not runtime request validators. They differ from the code: for
example, schemas allow zero tolerances or require `chemical_alphabet` where
DTOs/services do not, and the response schema mentions unimplemented fields
such as `score` and `isotopicMatch`. Use this reference and the implementation
for the branch contract. Fixtures are examples, not guarantees of identical
compound matches across database versions.

Verification for this documentation included reflection checks covering all
22 operations / 40 routes, local links and section anchors, and Jackson/Bean
Validation checks for 17 request fixtures and three inline request examples.
The existing
`MSMSSearchControllerValidationTest`, `MSMSSearchRequestDTOTest`,
`AdductControllerTest`, and `GlobalExceptionHandlerTest` (12 tests passed).
Database-backed integration tests and public-server requests were not run;
library contents, deployment behavior, and scientific equivalence to CMM 3.0
have not been established by these checks.
