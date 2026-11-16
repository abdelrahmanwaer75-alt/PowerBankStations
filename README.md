# PowerBank Stations – Smart Shared Phone Charging System

Academic prototype for a shared phone-charging service. It combines a Spring Boot web API, a static HTML/CSS/JavaScript client, an independent Java data-structures course module, and two Python services.

## Modules

- `CSE211_Web_Programming/` – frontend, Spring Boot REST backend, and MySQL/H2 setup.
- `CSE111_Data_Structures/` – manual BST, FIFO queue, linked list, and sorting implementations in Java.
- `AIE111_Artificial_Intelligence/` – Uniform Cost Search and A* route service over a synthetic graph.
- `CSE281_Digital_Image_Processing/` – classical NumPy/OpenCV image-processing experiments and inspection API.
- `Integration/` – service wiring and integration notes.
- `Documentation/` – architecture, data dictionary, and test-plan notes.

No third-party datasets are included. The route graph is illustrative only; image inspection identifies visual candidates and does not diagnose battery or electrical safety.

## Requirements

- JDK 17+ and Maven 3.9+
- Python 3.10+ and pip
- MySQL 8+ for persistent storage, or H2 for a local development run
- A modern browser and a static file server

## Local setup

Copy `.env.example` to a local environment file or set the variables in your shell. Set an actual local database password; never commit it.

### Database and backend

For MySQL, create the database/user, run `CSE211_Web_Programming/database/schema.sql`, then run `seed.sql` once. The seed includes demo accounts:

- Customer: `customer@example.com` / `DemoCustomer123!`
- Admin: `admin@example.com` / `DemoAdmin123!`

Those seed passwords are stored as PBKDF2 hashes. For a database-free H2 run from PowerShell:

```powershell
Set-Location CSE211_Web_Programming\backend
mvn spring-boot:run "-Dspring-boot.run.profiles=dev"
```

API health: `http://localhost:8080/api/health`. H2 starts empty; the MySQL seed script is not automatically run under the dev profile.

### AI route service

```powershell
Set-Location AIE111_Artificial_Intelligence
python -m pip install -r requirements.txt
python -m uvicorn src.api.main:app --reload --port 8001
```

Health: `http://localhost:8001/health`. The service exposes `/find-route`, `/nearest-available`, and `/compare-algorithms`; `/route` and `/compare` remain compatibility aliases.

### Image-processing service

```powershell
Set-Location CSE281_Digital_Image_Processing
python -m pip install -r requirements.txt
python -m uvicorn src.api.main:app --reload --port 8002
```

Health: `http://localhost:8002/health`. Inspection routes are `/process/powerbank`, `/process/infrastructure`, and `/process/pcb`, with multipart field `file`.

### Frontend

Serve `CSE211_Web_Programming/frontend` using a local static file server and open `customer/login.html` or `admin/admin-login.html`. The browser calls the Spring API; Java forwards AI and inspection requests to the Python services. Service URLs are configurable in `.env.example` and `frontend/assets/config.js`.

## Tests

Run each command from its module folder:

- `CSE211_Web_Programming\backend`: `mvn test`
- `CSE111_Data_Structures`: `mvn test`
- `AIE111_Artificial_Intelligence`: `python -m pytest -q`
- `CSE281_Digital_Image_Processing`: `python -m pytest -q`

The customer and admin folders contain 17 and 21 HTML pages respectively. Several screens remain marked as unsupported workflows in the UI because their full server-side CRUD implementations are not part of this prototype.

## Scope and limitations

This is not a production-ready rental platform. The backend currently demonstrates a subset of the required workflows; authenticated sessions, enforced role-based access, full administrative CRUD, a complete reservation-expiration workflow, and payments are not implemented. Do not expose it publicly or use real personal information. Replace the synthetic route graph and add verified, properly licensed image datasets before academic evaluation.
