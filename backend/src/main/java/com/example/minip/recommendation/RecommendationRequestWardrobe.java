package com.example.minip.recommendation;

import com.example.minip.wardrobe.WardrobeItem;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "recommendation_request_wardrobe", schema = "public")
public class RecommendationRequestWardrobe {
    @EmbeddedId
    private RecommendationRequestWardrobeId id;

    @MapsId("requestId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "request_id", nullable = false)
    private RecommendationJob request;

    @MapsId("wardrobeItemId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wardrobe_item_id", nullable = false)
    private WardrobeItem wardrobeItem;

    protected RecommendationRequestWardrobe() {
    }

    public RecommendationRequestWardrobe(RecommendationJob request, WardrobeItem wardrobeItem) {
        this.id = new RecommendationRequestWardrobeId(request.getId(), wardrobeItem.getId());
        this.request = request;
        this.wardrobeItem = wardrobeItem;
    }
}
