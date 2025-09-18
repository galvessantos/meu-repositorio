package com.montreal.msiav_bh.context;

import lombok.Getter;

import java.time.LocalDate;

public class CacheUpdateContext {
    @Getter
    private boolean hasFilters;
    private final boolean isFullRefresh;
    @Getter
    private LocalDate dataInicio;
    @Getter
    private LocalDate dataFim;
    @Getter
    private String reason;

    public CacheUpdateContext(boolean hasFilters, boolean isFullRefresh, LocalDate dataInicio, LocalDate dataFim, String reason) {
        this.hasFilters = hasFilters;
        this.isFullRefresh = isFullRefresh;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.reason = reason;
    }

    public static CacheUpdateContext fullRefresh() {
        return new CacheUpdateContext(false, true, null, null, "Full refresh without filters");
    }

    public static CacheUpdateContext scheduledRefresh(LocalDate dataInicio, LocalDate dataFim) {
        return new CacheUpdateContext(false, true, dataInicio, dataFim, "Scheduled job refresh");
    }

    public static CacheUpdateContext filteredSearch(LocalDate dataInicio, LocalDate dataFim, String... filters) {
        boolean hasNonDateFilters = false;
        if (filters != null) {
            for (String filter : filters) {
                if (filter != null && !filter.trim().isEmpty()) {
                    hasNonDateFilters = true;
                    break;
                }
            }
        }

        boolean hasCustomDateRange = false;
        if (dataInicio != null && dataFim != null) {
            LocalDate defaultStart = LocalDate.now().minusDays(30);
            LocalDate defaultEnd = LocalDate.now();
            hasCustomDateRange = !dataInicio.equals(defaultStart) || !dataFim.equals(defaultEnd);
        }

        boolean hasFiltersFlag = hasNonDateFilters || hasCustomDateRange;

        return new CacheUpdateContext(hasFiltersFlag, false, dataInicio, dataFim,
                hasFiltersFlag ? "Filtered search" : "Default 30-day period search");
    }

    public boolean isFullRefresh() {
        return isFullRefresh;
    }

    @Override
    public String toString() {
        return "CacheUpdateContext{" +
                "hasFilters=" + hasFilters +
                ", isFullRefresh=" + isFullRefresh +
                ", dataInicio=" + dataInicio +
                ", dataFim=" + dataFim +
                ", reason='" + reason + '\'' +
                '}';
    }
}