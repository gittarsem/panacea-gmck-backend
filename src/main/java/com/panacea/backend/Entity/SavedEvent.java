package com.panacea.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "saved_events",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_saved_event_user_event",
                        columnNames = {"user_email", "event_id"}
                )
        },
        indexes = {
                @Index(name = "idx_saved_events_user_email", columnList = "user_email")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavedEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Email / username of the authenticated user.
     *
     * We intentionally store the authenticated principal here
     * instead of depending on your existing User entity structure.
     */
    @Column(name = "user_email", nullable = false, length = 255)
    private String userEmail;

    /**
     * Frontend PANACEA event ID.
     */
    @Column(name = "event_id", nullable = false, length = 150)
    private String eventId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(nullable = false, length = 100)
    private String type;

    @Column(nullable = false, length = 255)
    private String date;

    @Column(nullable = false, length = 100)
    private String time;

    @Column(nullable = false, length = 255)
    private String venue;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "saved_at", nullable = false)
    private LocalDateTime savedAt;

    @PrePersist
    public void onCreate() {
        if (savedAt == null) {
            savedAt = LocalDateTime.now();
        }
    }
}