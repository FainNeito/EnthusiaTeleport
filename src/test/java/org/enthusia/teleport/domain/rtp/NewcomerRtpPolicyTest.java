package org.enthusia.teleport.domain.rtp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

final class NewcomerRtpPolicyTest {
    private static final long FIRST = 1_000_000L;
    private static final long WINDOW = 86_400_000L;

    @Test
    void pilotRaisesFiniteLimitWithoutReducingRankOrUnlimitedAccess() {
        assertEquals(3, NewcomerRtpPolicy.limit(1, true, 3, WINDOW, FIRST, FIRST));
        assertEquals(10, NewcomerRtpPolicy.limit(10, true, 3, WINDOW, FIRST, FIRST));
        assertEquals(-1, NewcomerRtpPolicy.limit(-1, true, 3, WINDOW, FIRST, FIRST));
    }

    @Test
    void elapsedWindowIncludesFirstInstantButExcludesExactExpiry() {
        assertEquals(3, NewcomerRtpPolicy.limit(1, true, 3, WINDOW, FIRST, FIRST + WINDOW - 1));
        assertEquals(1, NewcomerRtpPolicy.limit(1, true, 3, WINDOW, FIRST, FIRST + WINDOW));
        assertEquals(1, NewcomerRtpPolicy.limit(1, true, 3, WINDOW, FIRST, FIRST + WINDOW + 1));
    }

    @Test
    void disabledInvalidOrUnknownEligibilityDoesNotGrantBonus() {
        assertEquals(1, NewcomerRtpPolicy.limit(1, false, 3, WINDOW, FIRST, FIRST));
        assertEquals(1, NewcomerRtpPolicy.limit(1, true, 0, WINDOW, FIRST, FIRST));
        assertEquals(1, NewcomerRtpPolicy.limit(1, true, -1, WINDOW, FIRST, FIRST));
        assertEquals(1, NewcomerRtpPolicy.limit(1, true, 3, 0, FIRST, FIRST));
        assertEquals(1, NewcomerRtpPolicy.limit(1, true, 3, WINDOW, 0, FIRST));
        assertEquals(1, NewcomerRtpPolicy.limit(1, true, 3, WINDOW, FIRST + 1, FIRST));
    }
}
