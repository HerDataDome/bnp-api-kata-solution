## **🏨 Restful-Booker API Automation Framework**

This repository contains a robust, enterprise-grade API test automation framework built for the automationintesting.online Booking API.

## **⚙️ Prerequisites**

- Java 17+
- Maven 3.8+
- The restful-booker-platform API running and accessible
- Environment config at `src/test/resources/config/test.properties`


## **🛠️ Technology Stack**

- **Language:** Java 17 (LTS)

- **Build Tool:** Maven

- **API Client:** Rest-Assured

- **BDD Framework:** Cucumber Java & JUnit

- **Data Management:** Jackson Databind & Lombok

- **Reporting:** Allure (Cucumber JVM)

## **📁 Project Structure**

```
src/test/java/com/booking/
├── clients/          # HTTP clients (AuthClient, BookingClient) — zero boilerplate in step defs
├── config/           # ConfigManager (env properties) and ApiEndpoints (path constants)
├── context/          # ScenarioContext — thread-safe state sharing between steps via PicoContainer
├── dto/              # Request/response POJOs built with Lombok @Builder
├── factory/          # TestDataFactory — all test payload construction in one place
├── hooks/            # Cucumber @Before/@After — setup, teardown, failure logging
└── stepdefinitions/  # Step definitions mapping Gherkin to HTTP calls via clients

src/test/resources/
├── config/           # Environment properties (base URL, credentials)
├── features/         # Gherkin feature files — one per API endpoint
└── schemas/          # JSON Schema files for contract validation tests

test-artifacts/
├── allure-report/    # Committed HTML report — 40/40 passing (view with npx serve)
└── findings/         # API discrepancy evidence with Postman screenshots
```

## **🏗️ Framework Architecture & Design Patterns**

This framework rejects the "scripting" approach in favor of a Domain-Driven, 3-layer architecture. No layer directly instantiates or calls into a non-adjacent layer, mirroring production microservice design.

- **Builder Pattern:** Used via Lombok on all Request POJOs. This eliminates boilerplate and allows intentional omission of fields for negative testing.

- **Client Pattern:** All HTTP communication is abstracted into domain-specific clients (AuthClient, BookingClient). Step definitions contain zero raw Rest-Assured given() calls.

- **Factory Pattern:** TestDataFactory separates *what* data is needed from *how* it is sent.

- **Singleton Pattern:** ConfigManager loads environment properties dynamically. There are zero magic strings or hardcoded URLs in the test code.


## **🧪 Test Data & State Management**

Because this API is a public playground, test data collisions (e.g., 409 Conflict from double-booking a room) are highly likely. To guarantee pipeline stability, the TestDataFactory implements **ThreadLocalRandom** to dynamically generate dates within a safe 6-7 months window and assigns random Room IDs.

State isolation is managed via ScenarioContext , and Cucumber @After hooks ensure every scenario cleans up its own data (via DELETE) to prevent database pollution.


## **📋 Test Coverage Summary**

The framework provides comprehensive functional, security, and boundary-value coverage across all API endpoints. Below is a high-level summary of the automated scenarios:

- **Authentication (/auth/login)**

  - **Happy Path:** Successful token generation with valid administrative credentials.

  - **Security/Negative:** Rejection of empty credentials, missing fields, and invalid usernames/passwords.

- **Create Booking (POST /booking)**

  - **Happy Path:** Successful booking creation with dynamic, collision-free data generation.

  - **Boundary Value Analysis (BVA):** Validates the exact character limits for firstname, lastname, and phone according to the schema (and documenting where the live API deviates).

  - **Business Logic & Formatting:** Rejects malformed emails, missing required fields, and chronologically impossible dates (e.g., checkout before check-in).

- **Read Booking (GET /booking/{id})**

  - **Data Integrity:** Verifies that the fetched payload exactly matches the submitted creation payload.

  - **Security/Negative:** Rejects requests with missing authentication tokens and handles non-existent booking IDs.

- **Update Booking (PUT & PATCH /booking/{id})**

  - **Happy Path:** Replaces an entire booking payload and utilizes chained GET requests to strictly verify database persistence.

  - **Security/Negative:** Documents proper 401 Unauthorized and 403 Forbidden handling for malformed vs. invalid auth cookies.

  - **Contract Verification:** Proves the documented PATCH endpoint is not implemented (405 Method Not Allowed).

- **Delete Booking (DELETE /booking/{id})**

  - **Lifecycle Verification:** Successfully deletes existing bookings and ensures they are no longer retrievable.

  - **Security/Negative:** Verifies strict rejection of unauthorized deletion attempts.


## **🚀 How to Run the Tests**

Tests are categorized using Cucumber tags to support distinct CI pipeline stages.

**Run the Smoke Suite (Fast feedback):**

- mvn test "-Dcucumber.filter.tags=@smoke"

**Run the Full Regression Suite:**

- mvn test "-Dcucumber.filter.tags=@regression"

**Run the Negative Tests:**

- mvn test "-Dcucumber.filter.tags=@negative"

**Run a Specific Suite with Allure Report:**

- mvn test "-Dcucumber.filter.tags=@tag" allure:report

**Generate and View Allure Report:**

- mvn allure:serve

**🚀 Viewing the Allure Report**

- Clone and pull the repository.

- The committed `test-artifacts/allure-report/` folder contains the full test results (40/40 passing).

- To view locally: run `npx serve test-artifacts/allure-report` from the project root, then open the URL shown in the terminal under `Local:`.

> **Or** view the live report (updated on every CI run) at:
> https://herdatadome.github.io/bnp-api-kata-solution/


## **🔄CI/CD**

The project includes a GitHub Actions workflow (.github/workflows/ci.yml) that triggers automatically on any push or pull request to main that modifies src/ or pom.xml.

**What the workflow does**

1.  Checks out the repository and sets up Java 17

2.  Runs the full test suite via mvn clean test

3.  Generates an Allure HTML report via mvn allure:report

4.  Uploads raw results and the HTML report as downloadable artifacts (retained for 30 days)

5.  Deploys the Allure report to GitHub Pages on every successful push to main

**Important note on test results**

- The target API (restful-booker-platform) is a shared, publicly accessible test environment that resets frequently and is occasionally unstable. CI runs against this live environment and may show failures caused by environment resets or conflicts with other users --- not by defects in the framework itself.

- **The authoritative proof of the framework's correctness is the committed report in test-artifacts/allure-report/**, which was generated from a controlled local run and shows 40/40 tests passing. That report reflects the true state of the automation.


**Viewing the live CI report**

- After the workflow runs, the Allure report is deployed to GitHub Pages and accessible at:
  https://herdatadome.github.io/bnp-api-kata-solution/

- Workflow run artifacts (results + HTML report) are also available for 30 days under the **Actions** tab of the repository


## **📋 API Discrepancies & Test Evidence**

During the development of this framework, exploratory testing revealed significant drift between the provided OpenAPI/YAML specification and the live API's actual behavior. 

Because API documentation frequently falls behind live system updates (often due to security patches or agile iterations), this framework was deliberately architected to assert the **actual live behavior** of the API to ensure pipeline stability, while explicitly logging contract deviations as framework findings.

**High-Level Findings:**
* **Critical Stability Bugs:** Unhandled exceptions causing `500 Internal Server Error` on specific authentication failures.
* **Security/PII Updates:** Intentional stripping of PII (Email/Phone) from server responses, which contradicts the OpenAPI schema.
* **Contract Drift:** Missing endpoints (`405 Method Not Allowed` on `PATCH`), ignored payload fields (`roomid` silently ignored on `PUT`), and undocumented HTTP status codes (clean `401`/`403`/`404` error handling that the YAML failed to document).

> 📎 **Detailed Bug Reports & Visual Proof:** > For the complete technical breakdown of these bugs, please view the **[API Discrepancy Report](test-artifacts/API_Behaviour_&_Specification_Discrepancy_Report.pdf)**. 
> For the raw Postman screenshots validating every endpoint deviation, see the **[Exploratory Test Evidence](test-artifacts/ExploratoryAPITesting_Test_Evidence.pdf)**