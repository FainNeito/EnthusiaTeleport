# Newcomer RTP pilot requirements

## RET-001 - Optional newcomer allowance
WHEN the newcomer RTP pilot is enabled and a player's valid first-play timestamp is within the configured elapsed-time window THE SYSTEM SHALL raise their finite RTP limit to at least the configured newcomer total.

Acceptance: suggested pilot is three total successful uses within 24 hours of first play on this backend. This is a floor, not three additional uses. Missing, future, expired timestamps and disabled/invalid settings grant no bonus. Existing default and rank permissions remain authoritative; the pilot never reduces a limit or converts unlimited access into finite access.

## RET-002 - Persistent usage and boundaries
WHEN a newcomer uses RTP THE SYSTEM SHALL retain the existing UUID usage counter, successful-teleport accounting, permission requirement, combat rules, safety checks and queue budgets.

Acceptance: relogging, reloading and enabling/disabling the pilot do not reset uses. Failed searches and cancelled/failed teleports retain the existing behavior of not consuming a use. The new policy has no platform, scheduling, persistence or protection-plugin dependency.

## RET-003 - Safe configuration compatibility
WHEN an existing installation loads without newcomer settings THE SYSTEM SHALL keep the pilot disabled and preserve the existing RTP settings constructor.

Acceptance: no default gameplay changes; no automatic activation or production edits. Existing uses above the temporarily available limit are preserved when the window expires. The window uses elapsed real time, including offline time, independently of aNewbie's protection timer.
