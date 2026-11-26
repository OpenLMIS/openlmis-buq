1.2.2-SNAPSHOT (WIP)
==================

Improvements:
* [OLMIS-8464](https://openlmis.atlassian.net/browse/OLMIS-8464): BUQ right checks now apply to the program and facility a request acts on. Before, a user holding a BUQ right for any program or facility passed the check everywhere.
  * Prepare, view, save, submit, authorize, delete, download and the most recent rejection check the right for the bottom-up quantification's stored program and facility, never the ids in the request body.
  * The search endpoint (`GET /api/bottomUpQuantifications`) returns only the bottom-up quantifications of the programs and facilities the user holds a BUQ right for, including the facilities under the user's supervisory nodes. MANAGE_BUQ and service tokens still see all of them.
  * Approve, reject and final approve require the right through a role at the supervisory node the bottom-up quantification currently sits at, for its program, as the approval lists already did. A right at a parent node no longer approves out of turn.
  * The approval statistics, approval lists, supervised geographic zones and cost calculation require the right for the requested program.
  * Final approve checks every given bottom-up quantification before approving any.
  * Roles that worked only because of the missing scope now get HTTP 403. Check BUQ role assignments (program, home facility, supervisory node) before upgrading. MANAGE_BUQ is unchanged.
  * The API definition now declares the 403 responses, the `/forApproval` endpoint, the `id` query parameter and array response of `/finalApprove`, the rejection body of `/reject`, and the `facility` query parameter of the search endpoint.

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
