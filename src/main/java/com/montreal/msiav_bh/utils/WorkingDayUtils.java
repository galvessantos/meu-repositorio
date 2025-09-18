package com.montreal.msiav_bh.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WorkingDayUtils {

    public static boolean isWorkingDay(LocalDate date) {
        if (date.getDayOfWeek().getValue() >= 6) {
            return false;
        }

        Set<LocalDate> holidays = getHolidays(date.getYear());
        return !holidays.contains(date);
    }

    private static Set<LocalDate> getHolidays(int year) {
        Set<LocalDate> holidays = new HashSet<>();

        fixedHolidays(year, holidays);
        mobileHolidays(year, holidays);
        return holidays;
    }

    private static void mobileHolidays(int year, Set<LocalDate> holidays) {
        LocalDate easter = calculateEaster(year);
        holidays.add(easter);

        LocalDate carnival = easter.minusDays(48);
        holidays.add(carnival);

        LocalDate passionOfChrist = easter.minusDays(2);
        holidays.add(passionOfChrist);

        LocalDate corpusChrist = easter.plusDays(60);
        holidays.add(corpusChrist);
    }

    private static void fixedHolidays(int year, Set<LocalDate> holidays) {
        LocalDate newYear = LocalDate.of(year, 1, 1);
        holidays.add(newYear);

        LocalDate tiradentes = LocalDate.of(year, 4, 21);
        holidays.add(tiradentes);

        LocalDate laborDay = LocalDate.of(year, 5, 1);
        holidays.add(laborDay);

        LocalDate independenceDay = LocalDate.of(year, 9, 7);
        holidays.add(independenceDay);

        LocalDate ourLadyOfAparecida = LocalDate.of(year, 10, 12);
        holidays.add(ourLadyOfAparecida);

        LocalDate allSoulsDay = LocalDate.of(year, 11, 2);
        holidays.add(allSoulsDay);

        LocalDate proclamationOfRepublic = LocalDate.of(year, 11, 15);
        holidays.add(proclamationOfRepublic);

        LocalDate christmas = LocalDate.of(year, 12, 25);
        holidays.add(christmas);
    }

    private static LocalDate calculateEaster(int year) {
        int a = year % 19;
        int b = year / 100;
        int c = year % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;

        int monthAndDayBaseCalculate = h + l - 7 * m + 114;
        int mes = monthAndDayBaseCalculate / 31;
        int dia = (monthAndDayBaseCalculate % 31) + 1;
        return LocalDate.of(year, mes, dia);
    }
}
