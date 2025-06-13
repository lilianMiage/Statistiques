package fr.miage.lroux.statistique.utilities;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Locale;

public record YearWeek(int year, int week) {

    public static YearWeek from(LocalDate date) {
        WeekFields wf = WeekFields.of(Locale.getDefault());
        int year = date.get(wf.weekBasedYear());
        int week = date.get(wf.weekOfWeekBasedYear());
        return new YearWeek(year, week);
    }

    public LocalDate atDay(DayOfWeek dayOfWeek) {
        // Retourne le LocalDate correspondant au jour demandé de la semaine
        return LocalDate.of(year, 1, 4) // 4 janvier garanti dans la première semaine ISO
                .with(WeekFields.of(Locale.getDefault()).weekBasedYear(), year)
                .with(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear(), week)
                .with(dayOfWeek);
    }

    @Override
    public String toString() {
        return year + "-W" + String.format("%02d", week);
    }
}