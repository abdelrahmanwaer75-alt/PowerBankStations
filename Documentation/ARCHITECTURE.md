# Architecture
The application is a modular monorepo. CSE211 owns persistence and business rules. CSE111 provides independent manual data-structure implementations for course demonstration; do not replace DB transaction logic with in-memory structures. AIE111 exposes graph search over an illustrative weighted graph. CSE281 exposes classical image processing and returns candidate regions. Java should validate requests and persist inspection metadata.

## Core workflows
1. Customer registers/logs in.
2. Browse stations and availability.
3. Reserve a free device; transaction marks it RESERVED and creates a pickup code.
4. Start rental only with active reservation and matching code; device becomes RENTED.
5. Submit return to station with remaining capacity.
6. Upload photos to DIP service; review candidates; only approved return workflow should make device AVAILABLE (manual review policy still to be completed).

## Security note
Demo authentication currently compares demo passwords directly. This is intentionally not production-safe. Add BCrypt/JWT or secure sessions, authorization annotations, rate limiting, CSRF strategy, secrets management, and audit coverage before deployment.
