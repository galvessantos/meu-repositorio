package com.montreal.msiav_bh.utils;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class WorkingDayUtilsTest {

    public static final int BASE_YEAR = 2025;

    @Test
    void shouldReturnInvalidWorkingDay() {
        LocalDate passionOfChrist = LocalDate.of(BASE_YEAR, 4, 18);
        boolean isWorkingDay = WorkingDayUtils.isWorkingDay(passionOfChrist);

        assertThat(isWorkingDay).isFalse();
    }

    @Test
    void shouldReturnValidWorkingDay() {
        LocalDate workingDay = LocalDate.of(BASE_YEAR, 1, 2);
        boolean isWorkingDay = WorkingDayUtils.isWorkingDay(workingDay);

        assertThat(isWorkingDay).isTrue();
    }

    @Test
    void shouldReturnHoliday() {
        LocalDate carnival = LocalDate.of(BASE_YEAR, 3, 3);
        boolean isWorkingDay = WorkingDayUtils.isWorkingDay(carnival);

        assertThat(isWorkingDay).isFalse();

        LocalDate carnival2 = LocalDate.of(BASE_YEAR, 3, 3);
        isWorkingDay = WorkingDayUtils.isWorkingDay(carnival2);

        assertThat(isWorkingDay).isFalse();

        LocalDate passionOfChrist2026 = LocalDate.of(2026, 4, 3);
        isWorkingDay = WorkingDayUtils.isWorkingDay(passionOfChrist2026);

        assertThat(isWorkingDay).isFalse();

        LocalDate weekend = LocalDate.of(BASE_YEAR, 4, 5);
        isWorkingDay = WorkingDayUtils.isWorkingDay(weekend);

        assertThat(isWorkingDay).isFalse();
    }
}