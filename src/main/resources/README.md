# Resource Directory Overview

- `application.yml`: Base configuration shared across profiles with production-friendly defaults.
- `application-dev.yml`: Local development overrides (SQL logging, default credentials, relaxed JWT secret).
- `application-prod.yml`: Production-specific overrides that enforce secure JWT configuration and tighter logging.
- `db/migration`: Flyway migration scripts for evolving the relational schema.
- `static` and `templates`: Static assets and Thymeleaf views served by the web tier.
- `META-INF`: Spring metadata files (e.g., `spring.factories`) for framework bootstrapping hooks.
