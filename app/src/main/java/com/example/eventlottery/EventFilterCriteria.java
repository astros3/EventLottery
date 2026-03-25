package com.example.eventlottery;

import java.io.Serializable;

/**
 * US 01.01.04–01.01.06: Keyword search plus optional filters for the entrant dashboard.
 */
public class EventFilterCriteria implements Serializable {

    private static final long serialVersionUID = 1L;

    private String keyword = "";
    /** Inclusive start of selected "event from" day (local), or null if unset */
    private Long eventDateFromMillis;
    /** Inclusive end of selected "event to" day (local), or null if unset */
    private Long eventDateToMillis;
    private boolean registrationOpenOnly;
    /** Minimum {@link Event#getCapacity()}; null = no minimum */
    private Integer minCapacity;
    private boolean hideFullWaitingList;

    public static EventFilterCriteria empty() {
        return new EventFilterCriteria();
    }

    public String getKeyword() {
        return keyword != null ? keyword : "";
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword != null ? keyword.trim() : "";
    }

    public Long getEventDateFromMillis() {
        return eventDateFromMillis;
    }

    public void setEventDateFromMillis(Long eventDateFromMillis) {
        this.eventDateFromMillis = eventDateFromMillis;
    }

    public Long getEventDateToMillis() {
        return eventDateToMillis;
    }

    public void setEventDateToMillis(Long eventDateToMillis) {
        this.eventDateToMillis = eventDateToMillis;
    }

    public boolean isRegistrationOpenOnly() {
        return registrationOpenOnly;
    }

    public void setRegistrationOpenOnly(boolean registrationOpenOnly) {
        this.registrationOpenOnly = registrationOpenOnly;
    }

    public Integer getMinCapacity() {
        return minCapacity;
    }

    public void setMinCapacity(Integer minCapacity) {
        this.minCapacity = minCapacity;
    }

    public boolean isHideFullWaitingList() {
        return hideFullWaitingList;
    }

    public void setHideFullWaitingList(boolean hideFullWaitingList) {
        this.hideFullWaitingList = hideFullWaitingList;
    }

    /** True if any non-default filter is active (keyword counts). */
    public boolean hasActiveFilters() {
        if (!getKeyword().isEmpty()) return true;
        if (eventDateFromMillis != null || eventDateToMillis != null) return true;
        if (registrationOpenOnly) return true;
        if (minCapacity != null && minCapacity > 0) return true;
        if (hideFullWaitingList) return true;
        return false;
    }
}
