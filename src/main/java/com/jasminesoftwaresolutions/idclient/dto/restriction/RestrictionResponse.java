package com.jasminesoftwaresolutions.idclient.dto.restriction;

import com.google.gson.annotations.SerializedName;

import java.util.UUID;

/**
 * Response DTO matching
 * {@code RestRestrictionController.RestrictionResponse} from id:interfaces.
 */
public class RestrictionResponse {
    private UUID id;
    private String type;

    @SerializedName("target_type")
    private String targetType;

    @SerializedName("target_id")
    private String targetId;

    @SerializedName("event_id")
    private UUID eventId;

    @SerializedName("duration_minutes")
    private Integer durationMinutes;

    @SerializedName("created_at")
    private long createdAt;

    @SerializedName("expires_at")
    private Long expiresAt;

    public RestrictionResponse() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }

    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }

    public UUID getEventId() { return eventId; }
    public void setEventId(UUID eventId) { this.eventId = eventId; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public Long getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Long expiresAt) { this.expiresAt = expiresAt; }
}
