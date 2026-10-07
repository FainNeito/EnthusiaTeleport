package org.enthusia.teleport.domain.rtp;

/** Optional finite allowance; owns no platform state or usage counter. */
public final class NewcomerRtpPolicy {
    private NewcomerRtpPolicy() {
    }

    public static int limit(int existingLimit, boolean enabled, int newcomerTotal,
                            long windowMillis, long firstPlayedMillis, long nowMillis) {
        if (!enabled || existingLimit < 0 || newcomerTotal <= 0 || windowMillis <= 0
                || firstPlayedMillis <= 0 || nowMillis < firstPlayedMillis
                || nowMillis - firstPlayedMillis >= windowMillis) {
            return existingLimit;
        }
        return Math.max(existingLimit, newcomerTotal);
    }
}
