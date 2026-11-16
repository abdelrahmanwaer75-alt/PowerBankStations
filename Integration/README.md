# Integration

The Spring Boot API is the frontend gateway and system-of-record. It forwards route analysis and image inspections to the Python services. The manual CSE111 structures are independent course demonstrations and do not replace database operations.

## Default local endpoints

- Frontend: serve `CSE211_Web_Programming/frontend/` using a static server.
- Spring Boot API: `http://localhost:8080/api`.
- AI service: `http://localhost:8001`.
- Image-processing service: `http://localhost:8002`.

Configure `AI_SERVICE_URL` and `IMAGE_PROCESSING_SERVICE_URL` for the Java backend. Downstream requests use a 3-second connect timeout and a 15-second read timeout. If a Python service is unavailable, the Java API returns a gateway error instead of a success-shaped fallback.

The route graph and available-station coordinates supplied in tests are synthetic. Image results are visual candidates only. Neither service provides verified real-world routing, battery-health assessment, or electrical diagnosis. See the module READMEs for endpoint schemas and limitations.
