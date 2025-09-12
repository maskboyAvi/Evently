package com.evently.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seat", uniqueConstraints = @UniqueConstraint(columnNames = { "event_id", "label" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(nullable = false, length = 32)
    private String label;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    @Builder.Default
    private SeatStatus status = SeatStatus.AVAILABLE;
}
