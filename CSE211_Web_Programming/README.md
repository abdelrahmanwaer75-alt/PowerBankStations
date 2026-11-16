# CSE211 – Web Programming

The web module contains a static HTML/CSS/JavaScript client, a Spring Boot REST API, and MySQL/H2 database scripts for the PowerBank prototype.

## Structure

- `frontend/customer/` – 17 customer-facing HTML entry pages.
- `frontend/admin/` – 21 administrator-facing HTML entry pages.
- `frontend/assets/app.js` – shared page rendering, form handling, and API calls.
- `frontend/assets/config.js` – local service URLs.
- `backend/src/main/java/com/powerbank/` – Spring Boot controllers, JPA entities, repositories, services, validation/error handling, and Python-service integrations.
- `backend/src/main/resources/` – MySQL defaults and the H2 `dev` profile.
- `database/schema.sql`, `database/seed.sql` – MySQL setup and demo records.

## Run and test

1. Copy the root `.env.example` values into your shell environment and set a local MySQL username/password, or use the H2 profile.
2. From `backend`, run `mvn spring-boot:run -Dspring-boot.run.profiles=dev` for H2, or run the default profile after creating the MySQL schema and data.
3. Start the AI and image services as documented in their module READMEs.
4. Serve `frontend/` using a local static web server; open `customer/login.html` or `admin/admin-login.html`.
5. Run backend tests with `mvn test`.

The API is rooted at `/api`. Authentication hashes passwords with PBKDF2 and does not serialize password fields. Demo accounts in `database/seed.sql` use `customer@example.com` / `DemoCustomer123!` and `admin@example.com` / `DemoAdmin123!`.

## Integration endpoints

- `POST /api/ai/find-route`, `/api/ai/nearest-available`, `/api/ai/compare-algorithms`
- `POST /api/inspections/{powerbank|infrastructure|pcb}` (multipart field `file`)
- AI and image service URLs can be set using `AI_SERVICE_URL` and `IMAGE_PROCESSING_SERVICE_URL`; downstream requests use 3-second connect and 15-second read timeouts.

## Limitations

This is an academic prototype, not a production platform. The demo UI and API do not yet implement authenticated sessions or role-based authorization, payment processing, the complete set of admin CRUD operations, or physical station control. Do not expose it to the public internet or use real personal data.
