# Deployment

## Local Docker demo

Prerequisites: Docker Desktop (Linux containers) or Docker Engine with Compose.
On PowerShell, run `./scripts/init-demo.ps1` once to generate an ignored `.env` with random secrets.
On other shells, copy `.env.example` to `.env` and replace every placeholder. Generate a JWT key with `openssl rand -base64 32`.
Run `docker compose up --build -d`.
The API listens at http://localhost:8080, Swagger at /swagger-ui/index.html, and health at /actuator/health.
Compose binds the API to localhost and does not expose MySQL outside the container network.

The demo profile creates admin@example.test using DEMO_ADMIN_PASSWORD, one movie, one theatre,
one show scheduled for the next day, and 50 seats. Existing data and admin credentials are retained
on subsequent starts. Create a new show through the admin API when the original show has passed.
Register regular users through POST /users. Registration always creates a USER account.

Use `docker compose down` to stop the demo while preserving its database volume.
Do not remove the volume unless you intend to delete your demo data.

## Hosting

No public deployment is provisioned by these files. Choose a container host and a MySQL 8.4 database,
then configure:

- DB_URL: JDBC connection URL for that database, with TLS settings required by your provider.
- DB_USERNAME / DB_PASSWORD: application database credentials.
- JWT_SECRET: Base64 encoding of at least 32 cryptographically random bytes, supplied by a secret store.
- PORT: HTTP port expected by the host (defaults to 8080).
- BARCODE_URL: optional barcode provider URL (defaults to QuickChart).
- SPRING_PROFILES_ACTIVE: leave the demo profile disabled for non-demo environments.

Build and publish the Dockerfile image to your chosen registry, deploy it, and route HTTPS traffic
to PORT. Configure /actuator/health/readiness for readiness and /actuator/health/liveness for liveness.
Use persistent MySQL storage and keep secrets out of Git and container images.
For a fresh non-demo installation, register an administrator account normally, then have the
database operator explicitly promote that account with
`UPDATE users SET role = 'ADMIN' WHERE email = 'the-selected-admin@example.com';`.
Log in again after changing a role. Tokens expire after one hour; role changes do not revoke existing tokens immediately.

After deploying, verify registration, login, browsing, booking, cancellation, and Swagger,
then add the actual public URL to the README. Do not claim deployment until that verification is complete.

## Existing databases

Fresh databases are created by Flyway V1 and upgraded by V2. Hibernate validates the schema;
it does not alter it. Flyway intentionally refuses to silently adopt an existing untracked schema.

For an existing database: back it up, compare its schema to V1, resolve differences and invalid
amount/status data, and explicitly baseline it at version 1 using Flyway before applying V2.
For a reviewed legacy schema only, one startup with SPRING_FLYWAY_BASELINE_ON_MIGRATE=true and
SPRING_FLYWAY_BASELINE_VERSION=1 can establish that baseline. Remove those overrides immediately afterward.
Do not baseline a database whose schema has not been compared with V1.

V2 converts DOUBLE monetary values to DECIMAL(12,2), rounding legacy values to two decimals,
and adds password hashes and roles. Legacy users have no password hash and cannot log in until
an administrator provisions a BCrypt hash through a controlled migration. There is no password-reset endpoint.

## Windows Docker troubleshooting

If DOCKER_HOST points to an unavailable TCP endpoint, check `docker context ls`.
For Docker Desktop Linux containers, a process-local test setting is:
`$env:DOCKER_HOST = 'npipe:////./pipe/dockerDesktopLinuxEngine'`.

If JAVA_TOOL_OPTIONS forces an outdated Docker API version such as 1.40, remove that override
for the test process or use an API version supported by your Docker daemon.
The tests fail explicitly when Docker is unavailable; they are not silently skipped.
