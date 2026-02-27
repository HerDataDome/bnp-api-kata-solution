**🏨 Restful-Booker API Automation Framework**

This repository contains a robust, enterprise-grade API test automation framework built for the automationintesting.online Booking API.

**🛠️ Technology Stack**

- **Language:** Java 17 (LTS)

- **Build Tool:** Maven

- **API Client:** Rest-Assured

- **BDD Framework:** Cucumber Java & JUnit

- **Data Management:** Jackson Databind & Lombok

- **Reporting:** Allure (Cucumber JVM)

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

mvn test "-Dcucumber.filter.tags=@smoke"

**Run the Full Regression Suite:**

mvn test "-Dcucumber.filter.tags=@regression"

**Run the Negative Tests:**

mvn test "-Dcucumber.filter.tags=@negative"

**Run the A Specific Suite with Allure Report:**

mvn test "-Dcucumber.filter.tags=@regression" allure:report

**Generate and View Allure Report:**

mvn allure:serve

**🚀 Viewing the Allure Report**

The committed `allure-report/` folder contains the full test results.

To view: `npx serve allure-report` then open http://localhost:3000

**📊 API Behavior & Specification Discrepancy Report**

During the development of this framework, significant drift was discovered between the provided OpenAPI/YAML specification and the live API\'s behavior.

Because API documentation frequently falls behind live system updates, this framework was deliberately architected to assert the **actual live behavior** of the API to ensure pipeline stability, while explicitly logging contract deviations as Allure step findings.

**1. Specification Discrepancies (Documentation Drift)**

- **Response Structure Flattening (POST /booking)**

  - **Documented:** The YAML indicates the POST response should return a nested booking wrapper.

  - **Live API:** Returns a flat JSON object where bookingid and details exist at the root level.

- **PII Data Stripping (Missing Email/Phone)**

  - **Documented:** The schema states email and phone are required response fields.

  - **Live API:** Intentionally strips email and phone from POST, GET, and PUT responses (likely a later security update). The framework explicitly logs this omission without failing the tests.

- **Status Code Discrepancies**

  - **Live API:** Correctly aligns with REST semantics, returning 201 Created for POST and 200 OK for DELETE (deviating from the YAML docs).

- **Boundary Value Mismatch (firstname & lastname)**
  - **Documented:** The schema defines a strict maximum length of 18 characters for both `firstname` and `lastname`.

  - **Live API:** The validation layer is significantly looser, accepting strings up to 30 characters (rejecting at 31 with a `"size must be between 3 and 30"` validation error).  

**2. Functional API Anomalies**

- **PUT Updates Silently Ignore roomid**

  - **Live API:** Returns a {"success": true} response when a new roomid is submitted via PUT. However, secondary verification via GET reveals the roomid remains permanently unchanged.

- **Missing Endpoint: PATCH /booking/{id}**

  - **Live API:** The endpoint returns 405 Method Not Allowed. An explicit @bug scenario was written to assert this missing implementation.

- **Lenient Boolean Coercion**

  - **Live API:** When passing an integer 1 for the depositpaid boolean field, the API leniently coerces the binary 1 into true rather than rejecting it with a 400 Bad Request.

**3. Critical Stability Issues**

- **Authentication Parser Crash (500 Internal Server Error)**

  - **Live API:** When sending a malformed token value that still includes the prefix (e.g., Cookie: token=invalid_garbage), the server fails to decrypt it and throws a 500 Internal Server Error. The framework isolates this into dedicated @bug scenarios to document the crash without causing flaky pipeline failures.
