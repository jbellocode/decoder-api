# ADR-002: RBAC Authorization - Header-Based Mock Authentication

Status: Accepted | Date: 2025-10-30

## Context
The DECODER API manages energy forecasts for multiple buildings. Admin users must access ANY building. Owner users must access ONLY their own buildings. Guests must be denied.

## Decision
Implement Header-Based RBAC with Mock Roles using X-User-Role header (admin|owner|guest).

## RBAC Rules
- Admin: Full access to all buildings
- Owner: Restricted access to own buildings (B001)
- Guest: Denied access, returns 403

## Benefits
- No external auth server needed
- Easy to test (just change header)
- Simple to understand
- Perfect for MVP phase
- Can migrate to JWT later

## Consequences
- Header value NOT verified
- Not production-ready
- Clients can spoof roles

## Future
Replace X-User-Role with Authorization: Bearer <JWT> when moving to production
