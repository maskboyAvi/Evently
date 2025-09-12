package com.evently.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EventStat {
    private Long eventId;
    private String name;
    private int capacity;
    private int booked;
    private double utilization; // booked/capacity if capacity>0
}
