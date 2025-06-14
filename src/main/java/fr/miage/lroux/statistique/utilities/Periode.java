package fr.miage.lroux.statistique.utilities;

import fr.miage.lroux.statistique.entity.CarSnapshot;
import fr.miage.lroux.statistique.entity.StationSnapshot;
import fr.miage.lroux.statistique.entity.TimeGranularity;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.time.*;
import java.util.*;
import java.util.function.Function;

public class Periode {

    public ZonedDateTime getPeriodStart(Object periodKey, TimeGranularity granularity) {
        return switch (granularity) {
            case HOUR -> ((ZonedDateTime) periodKey);
            case DAY -> ((LocalDate) periodKey).atStartOfDay(ZoneId.systemDefault());
            case WEEK -> ((YearWeek) periodKey).atDay(DayOfWeek.MONDAY).atStartOfDay(ZoneId.systemDefault());
            case MONTH -> ((YearMonth) periodKey).atDay(1).atStartOfDay(ZoneId.systemDefault());
            case YEAR -> ((Year) periodKey).atDay(1).atStartOfDay(ZoneId.systemDefault());
        };
    }

    // ////////////////////////////////////////////
    public static <T> List<ZonedDateTime> generatePeriodsAfterFirstSnapshot(
            List<T> snapshots,
            ZonedDateTime end,
            TimeGranularity granularity,
            Function<T, Instant> timestampExtractor
    ) {
        if (snapshots == null || snapshots.isEmpty()) {
            return Collections.emptyList();
        }

        // Trouver le plus ancien snapshot
        Instant firstTimestamp = snapshots.stream()
                .map(timestampExtractor)
                .min(Comparator.naturalOrder())
                .orElseThrow();

        ZonedDateTime start = ZonedDateTime.ofInstant(firstTimestamp, ZoneId.systemDefault());

        return generatePeriods(start, end, granularity);
    }

    public static List<ZonedDateTime> generatePeriods(
            ZonedDateTime start,
            ZonedDateTime end,
            TimeGranularity granularity
    ) {
        List<ZonedDateTime> periods = new ArrayList<>();
        ZonedDateTime current = getPeriodStart(start, granularity);

        while (current.isBefore(end)) {
            periods.add(current);
            current = incrementPeriod(current, granularity);
        }

        // S’assurer que la fin exacte est incluse (et pas juste le début de sa période)
        if (periods.isEmpty() || !periods.get(periods.size() - 1).equals(end)) {
            periods.add(end);
        }

        return periods;
    }

    public static ZonedDateTime getPeriodStart(ZonedDateTime date, TimeGranularity granularity) {
        return switch (granularity) {
            case HOUR -> date.truncatedTo(ChronoUnit.HOURS);
            case DAY -> date.truncatedTo(ChronoUnit.DAYS);
            case WEEK -> date.with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1)
                    .truncatedTo(ChronoUnit.DAYS);
            case MONTH -> date.withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);
            case YEAR -> date.withDayOfYear(1).truncatedTo(ChronoUnit.DAYS);
        };
    }

    public static ZonedDateTime incrementPeriod(ZonedDateTime date, TimeGranularity granularity) {
        return switch (granularity) {
            case HOUR -> date.plusHours(1);
            case DAY -> date.plusDays(1);
            case WEEK -> date.plusWeeks(1);
            case MONTH -> date.plusMonths(1);
            case YEAR -> date.plusYears(1);
        };
    }

    public static String formatPeriodLabel(ZonedDateTime periodStart, TimeGranularity granularity) {
        return switch (granularity) {
            case HOUR -> periodStart.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH'h'"));
            case DAY -> periodStart.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            case WEEK -> {
                WeekFields weekFields = WeekFields.of(Locale.getDefault());
                int weekNumber = periodStart.get(weekFields.weekOfWeekBasedYear());
                yield periodStart.getYear() + "-W" + String.format("%02d", weekNumber);
            }
            case MONTH -> periodStart.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            case YEAR -> String.valueOf(periodStart.getYear());
        };
    }
}
