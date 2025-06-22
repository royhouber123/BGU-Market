package market.domain.purchase;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bid_entities")
public class BidEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "store_id", nullable = false)
    private String storeId;

    @Column(name = "product_id", nullable = false)
    private String productId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "bid_entity_id")
    private List<Bid> bids = new ArrayList<>();

    public BidEntity() {}

    public BidEntity(String storeId, String productId) {
        this.storeId = storeId;
        this.productId = productId;
        this.bids = new ArrayList<>();
    }

    // Getters and setters
    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getStoreId() { return storeId; }

    public String getProductId() { return productId; }

    public List<Bid> getBids() { return bids; }

    public void setBids(List<Bid> bids) { this.bids = bids; }
}
