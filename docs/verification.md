# Newcomer RTP pilot verification

## Source and local checks

- Canonical repository: https://github.com/wsg138/EnthusiaTeleport ; default main fetched and inspected at 2dbc81995e96e7e43041a12905ea5f1c4dad4aa3.
- Isolated branch: codex/newcomer-rtp-pilot. No edits to prior Hub or monorepo checkouts.
- Maven 3.9.11, JDK 23, compiler release 21, declared Paper API 1.21.11-R0.1-SNAPSHOT.
- Initial offline dependency resolution lacked JetBrains annotations 26.0.2-1; online resolution succeeded. JDK 25 then failed the unchanged Mockito/Byte Buddy fixture. JDK 23 passed the existing 17 tests; no mocking dependency upgrade or experimental VM flag was introduced.
- PROVE: newcomer-red.log captured missing-policy compilation errors before policy implementation.
- Focused policy green: three tests passed (newcomer-green.log).
- Original allowance `mvn -B -ntp -o clean verify`: 26 tests, zero failures/errors/skips (pilot-final-verify.log).
- On-screen follow-up `mvn -B -ntp -o clean verify`: 31 tests, zero failures/errors/skips (notice-final-verify.log). Five notice tests cover updating counts, higher/unlimited effective limits, permission loss, exact expiry, disabled gates/invalid timestamps and quit/clear cleanup. Config tests cover notice default and opt-out under the still-default-off pilot.
- Initial clean test compilation hit a JDK 23 diagnostic-formatting failure with a deprecated PlayerQuitEvent constructor in the new fixture. A mocked event removed that fixture call; the full clean build succeeded. This is test/toolchain refinement, not gameplay regression evidence.
- New coverage: disabled/invalid/unknown eligibility; exact expiry; higher finite and negative computed limits; config defaults and safe duration bounds; existing UUID counts retained through flush/reconstruction and disabling/re-enabling; legacy constructor; exhausted queued search rejected before destination/teleport access.
- `git diff --check`: passed.
- Latest unmerged/local test artifact only: target/EnthusiaTeleport-1.2.10-SNAPSHOT.jar. SHA-256 AB66B7212CE5DD52AA242BA1F410DB12974B43797771C9ABE3C600E517CB5043. This is not a production artifact.

The exact new policy initially had no implementation; adapter/config tests were added during refinement. The queued-search fixture uses the existing private search type because this repository has no runtime scheduler harness; it verifies the early rejection path, not actual Paper asynchronous scheduling.

## Integration and runtime boundaries

No Companion-plugin compile-time API was changed. CombatLogX remains authoritative and RTP permission remains opt-in. No aNewbie API call, protection modification, database schema, use-count reset, or new persistence writer was added. Safety, warmup and successful-use accounting remain in the existing platform adapter.

Plan data collection was inspected through authenticated read-only UI. SMP recorded an additional session and increasing playtime/mob kills. That confirms current collection activity, not complete historical coverage, UUID funnel reconciliation, player departure causes or client acceptance. Documentation/measurement observations require no behavioral engine work; the policy change has separate behavioral proof and tests above.

Project-local EARS/state tools are absent; no automated SPEAR-tool result is claimed. Requirements/tasks/evidence are maintained in docs/requirements.md, docs/tasks.md and this file.

The monorepo owns release builds through plugins/enthusia-teleport. Its pin must be updated through a separate normal PR after canonical merge, with combined checks and clean merged-source build evidence before any authorized production upload. No pin, deployment, reload, restart, merge, player communication or automation was performed here.

Fresh-account first-play timestamps, effective permissions, queue/warmup runtime behavior, failed/cancelled teleports, expiry/rollback and Java/Bedrock player journeys remain explicit runtime acceptance gates in docs/retention-pilot.md. Local checks do not establish retention improvement.
