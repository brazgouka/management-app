package com.management.timer;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * @param productivityScore share of tracked time that was productive, 0-100, or {@code null} if nothing was tracked
 */
public record TimeStats(
        LocalDate from,
        LocalDate to,
        long totalSeconds,
        Map<Focus, Long> secondsByFocus,
        Integer productivityScore,
        List<ActivityTotal> topActivities) {

    public record ActivityTotal(String activity, Focus focus, long seconds) {
    }
}
