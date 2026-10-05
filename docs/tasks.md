# Retention pilot tasks

- [x] SPEC: inspect authoritative main and write RET-001..003. Clean isolated clone of wsg138/EnthusiaTeleport, origin/main 2dbc81995e96e7e43041a12905ea5f1c4dad4aa3, branch codex/newcomer-rtp-pilot. Original dirty Hub checkout preserved.
- [x] PROVE: new policy tests initially failed test compilation because NewcomerRtpPolicy did not exist (newcomer-red.log). The existing 17 tests passed on Java 23 targeting Java 21 before implementation. This is new-feature absence evidence, not fabricated historical regression evidence.
- [x] ENGINE: disabled-by-default configurable newcomer allowance uses the existing counter; queued searches recheck the limit after chunk lookup.
- [x] ARCH: original RtpSettings constructor retained; pure domain policy owns no Paper/provider/persistence dependencies; existing permission, rank resolution, combat checks, queue budgets, safe-location logic and successful-teleport callback retained.
- [x] RET-004 SPEC/PROVE: user requested aNewbie-style on-screen discovery. Read-only production config confirms action bar and green boss bar enabled. Prior PR had no on-screen notice (source absence evidence; no fabricated red test).
- [x] RET-004 ENGINE/ARCH: separate Adventure boss bar on existing main-thread task coordinator; permission/window/count gates, owned-bar cleanup, compatible newcomer constructor and configurable text. No companion API or action-bar writes.
- [x] REFINE-local: 31 tests passed with Maven clean verify, zero failures/errors/skips; five notice tests plus config option coverage. Git whitespace check passed. See docs/verification.md.
- [ ] REVIEW: upstream PR #16 published; exact-head checks/review inspected in the delivery turn. Hosted CI and review approval remain pending. No merge authorization.
- [ ] RUNTIME: merged-source/monorepo release checks, explicitly authorized deployment, fresh Java/Bedrock acceptance, baseline and pilot observation. Not established by local tests.

No project-local EARS validator or SPEAR state helper exists in this repository. The existing SPEAR Paper brownfield workflow was located and read; this requirement/task/evidence record is maintained without claiming unavailable tooling passed.
