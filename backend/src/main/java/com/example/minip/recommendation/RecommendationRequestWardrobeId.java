package com.example.minip.recommendation;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class RecommendationRequestWardrobeId implements Serializable {
    @Column(name = "request_id")
    private Long requestId;

    @Column(name = "wardrobe_item_id")
    private Long wardrobeItemId;

    protected RecommendationRequestWardrobeId() {
    }

    public RecommendationRequestWardrobeId(Long requestId, Long wardrobeItemId) {
        this.requestId = requestId;
        this.wardrobeItemId = wardrobeItemId;
    }

    @Override
    public boolean equals(Object value) {
        if (this == value) {
            return true;
        }
        if (!(value instanceof RecommendationRequestWardrobeId other)) {
            return false;
        }
        return Objects.equals(requestId, other.requestId)
            && Objects.equals(wardrobeItemId, other.wardrobeItemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestId, wardrobeItemId);
    }
}
