package com.jasminesoftwaresolutions.idclient.dto.restriction;

import com.google.gson.annotations.SerializedName;

import java.util.UUID;

/**
 * Request payload matching
 * {@code RestRestrictionController.PlaceRestrictionPayload} from id:interfaces.
 */
public class PlaceRestrictionRequest {
    private String type;

    @SerializedName("target_type")
    private String targetType;

    @SerializedName("target_id")
    private String targetId;

    @SerializedName("event_id")
    private UUID eventId;

    @SerializedName("duration_minutes")
    private Integer durationMinutes;

    public PlaceRestrictionRequest() {}

    public PlaceRestrictionRequest(String type, String targetType, String targetId,
                                    UUID eventId, Integer durationMinutes) {
        this.type = type;
        this.targetType = targetType;
        this.targetId = targetId;
        this.eventId = eventId;
        this.durationMinutes = durationMinutes;
    }

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
}
