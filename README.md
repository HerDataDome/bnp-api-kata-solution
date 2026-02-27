**🏨 Restful-Booker API Automation Framework**

This repository contains a robust, enterprise-grade API test automation framework built for the automationintesting.online Booking API.

**⚙️ Prerequisites**

- Java 17+
- Maven 3.8+
- The restful-booker-platform API running and accessible
- Environment config at `src/test/resources/config/test.properties`


**🛠️ Technology Stack**

- **Language:** Java 17 (LTS)

- **Build Tool:** Maven

- **API Client:** Rest-Assured

- **BDD Framework:** Cucumber Java & JUnit

- **Data Management:** Jackson Databind & Lombok

- **Reporting:** Allure (Cucumber JVM)

**📁 Project Structure**

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

**🏗️ Framework Architecture & Design Patterns**

This framework rejects the "scripting" approach in favor of a Domain-Driven, 3-layer architecture. No layer directly instantiates or calls into a non-adjacent layer, mirroring production microservice design.

- **Builder Pattern:** Used via Lombok on all Request POJOs. This eliminates boilerplate and allows intentional omission of fields for negative testing.

- **Client Pattern:** All HTTP communication is abstracted into domain-specific clients (AuthClient, BookingClient). Step definitions contain zero raw Rest-Assured given() calls.

- **Factory Pattern:** TestDataFactory separates *what* data is needed from *how* it is sent.

- **Singleton Pattern:** ConfigManager loads environment properties dynamically. There are zero magic strings or hardcoded URLs in the test code.


**🧪 Test Data & State Management**

Because this API is a public playground, test data collisions (e.g., 409 Conflict from double-booking a room) are highly likely. To guarantee pipeline stability, the TestDataFactory implements **ThreadLocalRandom** to dynamically generate dates within a safe 6-7 months window and assigns random Room IDs.

State isolation is managed via ScenarioContext , and Cucumber @After hooks ensure every scenario cleans up its own data (via DELETE) to prevent database pollution.


**🚀 How to Run the Tests**

Tests are categorized using Cucumber tags to support distinct CI pipeline stages.

**Run the Smoke Suite (Fast feedback):**

- mvn test "-Dcucumber.filter.tags=@smoke"

**Run the Full Regression Suite:**

- mvn test "-Dcucumber.filter.tags=@regression"

**Run the Negative Tests:**

- mvn test "-Dcucumber.filter.tags=@negative"

**Run the A Specific Suite with Allure Report:**

- mvn test "-Dcucumber.filter.tags=@tag" allure:report

**Generate and View Allure Report:**

- mvn allure:serve

**🚀 Viewing the Allure Report**

- The committed `allure-report/` folder contains the full test results.

- To view: `npx serve allure-report` then open http://localhost:3000


**🔄CI/CD**

The project includes a GitHub Actions workflow (.github/workflows/ci.yml) that triggers automatically on any push or pull request to main that modifies src/ or pom.xml.

**What the workflow does**

1.  Checks out the repository and sets up Java 17

2.  Runs the full test suite via mvn clean test

3.  Generates an Allure HTML report via mvn allure:report

4.  Uploads raw results and the HTML report as downloadable artifacts (retained for 30 days)

5.  Deploys the Allure report to GitHub Pages on every successful push to main

**Important note on test results**

- The target API (restful-booker-platform) is a shared, publicly accessible test environment that resets frequently and is occasionally unstable. CI runs against this live environment and may show failures caused by environment resets or conflicts with other users --- not by defects in the framework itself.

- 

**The authoritative proof of the framework\'s correctness is the committed report in test-artifacts/allure-report/**, which was generated from a controlled local run and shows 40/40 tests passing. That report reflects the true state of the automation.

**Viewing the live CI report**

- After the workflow runs, the Allure report is deployed to GitHub Pages and accessible at:
  https://herdatadome.github.io/bnp-api-kata-solution/

- Workflow run artifacts (results + HTML report) are also available for 30 days under the **Actions** tab of the repository


**📊 API Behavior & Specification Discrepancy Report**

During the development of this automation framework against the automationintesting.online Booking API, significant drift was discovered between the provided OpenAPI/YAML specification and the live API\'s behavior.

Because API documentation frequently falls behind live system updates (often due to security patches or agile iterations), this framework was deliberately architected to assert the **actual live behavior** of the API to ensure pipeline stability, while explicitly logging contract deviations as Allure step findings.

Below is the summary of the discrepancies and stability issues uncovered.

**1. Specification Discrepancies (Documentation Drift)**

These findings indicate areas where the YAML contract is likely outdated compared to the live system\'s current implementation.

- **Response Structure Flattening (POST /booking)**

  - **Documented Spec:** The YAML indicates the POST response should return a nested object (a booking wrapper containing the details, alongside a bookingid).

  - **Live API Behavior:** The API returns a flat JSON object where the bookingid and booking details exist at the same root level.

  - **Analysis:** The framework was adapted to parse the flat object to ensure test stability.

- **PII Data Stripping (Missing Email/Phone)**

  - **Documented Spec:** The schema states email and phone are required fields in the booking response payload.

  - **Live API Behavior:** The API intentionally strips email and phone from the responses of POST /booking, GET /booking/{id}, and PUT /booking/{id}.

  - **Analysis:** This is highly likely a deliberate security update to protect Personally Identifiable Information (PII) that was never updated in the YAML. The framework explicitly logs this omission without failing the tests.

- **Status Code Discrepancies**

  - **Documented Spec:** The YAML states POST returns 200 OK and DELETE returns 201 Created.

  - **Live API Behavior:** The API correctly aligns with standard REST semantics, returning 201 Created for POST and 200 OK for DELETE.

- **Boundary Value Mismatch (firstname & lastname)**

  - **Documented Spec:** The schema defines a strict maximum length of 18 characters for both firstname and lastname.

  - **Live API Behavior:** The validation layer is significantly looser, accepting strings up to 30 characters (rejecting at 31 with a \"size must be between 3 and 30\" validation error).

  - **Analysis:** The framework tests validate actual accepted boundaries, logging the discrepancy.

- **Undocumented Authentication States (403 Forbidden on PUT)** - **Documented Spec:** The YAML bundles all authentication failures under a single 401 Unauthorized response.

  - **Live API Behavior:** The live API intelligently differentiates between a missing/malformed token prefix (401 Unauthorized) and a correctly formatted but invalid token string (403 Forbidden) on the PUT endpoint.

  - **Analysis:** The live API exhibits superior, clean REST security design, but the YAML specification is incomplete. The framework asserts the live 401/403 split.

- **Authentication Error Message Mismatch**

  - **Documented Spec:** The YAML states that a 401 response should return the body {\"error\": \"Unauthorized\"}.

  - **Live API Behavior:** The API actually returns {\"error\": \"Authentication required\"} when a token is missing.

  - **Analysis:** Minor contract deviation. The framework\'s assertions were updated to match the live error message.

- **Undocumented Resource States (404 Not Found on PUT)**

  - **Documented Spec:** The YAML fails to define the expected response when attempting to update a non-existent booking ID.

  - **Live API Behavior:** The API correctly handles this edge case by returning a 404 Not Found with the body {\"error\": \"Failed to update booking\"}.

  - **Analysis:** The API handles the logic correctly, but the documentation is missing.

**2. Functional API Anomalies**

These findings represent unexpected logic handling within the live API that should be reviewed by the backend team.

- **PUT Updates Silently Ignore roomid**

  - **Documented Spec:** A PUT request replaces the entire resource, implying all provided fields (including roomid) should be updated.

  - **Live API Behavior:** The API returns a {\"success\": true} response when a new roomid is submitted via PUT. However, secondary verification via GET reveals the roomid remains unchanged.

  - **Analysis:** The backend appears to lock the roomid upon creation. The framework successfully updates other fields but logs a contract deviation for the ignored roomid.

- **Missing Endpoint: PATCH /booking/{id}**

  - **Documented Spec:** The YAML promises a PATCH endpoint for partial updates.

  - **Live API Behavior:** The endpoint returns a 405 Method Not Allowed.

  - **Analysis:** The endpoint was never implemented in the live environment. An explicit @bug scenario was written to assert this 405 response.

- **Lenient Boolean Coercion**

  - **Live API Behavior:** When passing an integer 1 for the depositpaid boolean field, the API\'s JSON parser (Jackson) leniently coerces the binary 1 into true rather than rejecting the schema mismatch with a 400 Bad Request.

**3. Critical Stability Issues**

- **Authentication Parser Crash (500 Internal Server Error)**

  - **Live API Behavior:** When an API consumer sends a DELETE request with a malformed token value that still includes the token= prefix (e.g., Cookie: token=invalid_garbage), the server fails to decrypt it. Instead of catching the error and returning a 401 Unauthorized or 403 Forbidden (as the PUT endpoint correctly does), the backend code crashes, throwing a 500 Internal Server Error.

  - **Analysis:** This is a severe unhandled exception in the API\'s security filter. The framework isolates this specific malformed input into dedicated @bug scenarios to document the crash without causing flaky pipeline failures.

**📋 Test Evidence**

`test-artifacts/ExploratoryAPITesting_Test_Evidence.pdf` contains Postman 
screenshots evidencing all API discrepancies documented in the section above. 
It covers manual verification of every endpoint (POST, GET, PUT, PATCH, DELETE) 
and provides visual proof for each spec deviation finding including:

- PII stripping from POST and GET responses
- roomid being silently ignored on PUT
- PATCH returning 405 Method Not Allowed
- DELETE returning 200 (not 201 as documented)
- Authentication parser crash (500) on DELETE with malformed token
- The 401/403 split on PUT authentication failures