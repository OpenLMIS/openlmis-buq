1.2.2-SNAPSHOT (WIP)
==================

New functionality:
* [OLMIS-8467](https://openlmis.atlassian.net/browse/OLMIS-8467): Added the schema for population-based vaccine forecasting: population segments, forecasting settings per program and product (doses per target, coverage, wastage, buffer months), supply ratios, and forecasts with their line items, status changes and rejections, each in its own table. Demo data adds three population segments. No endpoints yet.
* [OLMIS-8468](https://openlmis.atlassian.net/browse/OLMIS-8468): Added the forecasting configuration API: `/api/populationSegments`, `/api/forecastingSettings` and `/api/supplyRatios`, each with create, update, delete, get and search (settings by program and product, supply ratios by vaccine and supply), all requiring MANAGE_BUQ. Request bodies are checked against the same limits as the database. A population segment used by a forecasting setting or a forecast can't be deleted. A forecasting setting needs a product with a commodity type, and a program can forecast only one product per commodity type, because requisitions take the ideal stock amount per commodity type. The supply product of a supply ratio needs a commodity type too.

Improvements:
* [OLMIS-8468](https://openlmis.atlassian.net/browse/OLMIS-8468): A request body that fails validation returns 400 `buq.error.validation.field.invalid` naming the invalid field, instead of Spring's exception text. A body that can't be read, such as an unknown enum value, returns 400 `buq.error.request.body.unreadable` in the usual error format.

1.2.1 / 2026-10-16
==================

Bug fixes:
* [OLMIS-8405](https://openlmis.atlassian.net/browse/OLMIS-8405): A service whose health check fails stays registered in Consul. Consul no longer removes it after 10 minutes in the critical state. The service registers only once, at startup, so after a removal nginx answered HTTP 502 for it until the container was restarted, even when the service itself had recovered. The health check keeps running and still reports the critical state.

1.2.0 / 2026-09-28
==================

Improvements:
* [OLMIS-8280](https://openlmis.atlassian.net/browse/OLMIS-8280) Migrated the SonarCloud analysis to Java 21 by running it through the SonarQube scan action instead of the Gradle plugin, and removed the now-unused Gradle sonar plugin and configuration.
* [OLMIS-8280](https://openlmis.atlassian.net/browse/OLMIS-8280) Removed the axios dependency from the Consul registration script, replacing it with the native Node `http` client (no more axios security advisories to track).
* [MW-1471](https://openlmis.atlassian.net/browse/MW-1471): Exposed the Prometheus metrics endpoint at `/actuator/prometheus`.
* Stabilized consul registration and health checks

Bug fixes:
* [OLMIS-8223](https://openlmis.atlassian.net/browse/OLMIS-8223): Fixed apostrophes being dropped from translated messages.

1.1.0 / 2025-11-27
==================

Changes:
* Upgrade PostgreSQL JDBC driver to 42.6.2 for PostgreSQL 14 compatibility

1.0.2 / 2025-03-31
==================

Minor coverage updates and code analysis improvements.

1.0.1 / 2024-10-31
==================

Bug fixes:
* [OLMIS-7995](https://openlmis.atlassian.net/browse/OLMIS-7995): Supervisory node assignment is now required for authorization process

Improvements:
* [OIS-48](https://openlmis.atlassian.net/browse/OIS-48): Update service base images to versions without known vulnerabilities
* [OIS-44](https://openlmis.atlassian.net/browse/OIS-44): Add transifex configuration

1.0.0 / 2024-04-19
==================

Features
* [OLMIS-7838](https://openlmis.atlassian.net/browse/OLMIS-7838): Initial implementation of openlmis-buq
