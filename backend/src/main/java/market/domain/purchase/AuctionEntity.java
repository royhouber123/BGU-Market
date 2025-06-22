package market.domain.purchase;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;


@Entity
@Table(name = "auction_entities")
public class AuctionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "store_id", nullable = false)
    private String storeId;

    @Column(name = "product_id", nullable = false)
    private String productId;

    @Column(name = "starting_price", nullable = false)
    private double startingPrice;
    
    @Column(name = "end_time", nullable = false)
    private long endTime;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "auction_id")
    private List<Offer> offers = new ArrayList<>();

    @Column(name = "winning_user_id")
    private String winningUserId;

    @Column(name = "winning_price")
    private Double winningPrice;

    public AuctionEntity() {}

    public AuctionEntity(String storeId, String productId, double startingPrice, long endTime) {
        this.storeId = storeId;
        this.productId = productId;
        this.startingPrice = startingPrice;
        this.endTime = endTime;
        this.offers = new ArrayList<>();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStoreId() { return storeId; }
    public void setStoreId(String storeId) { this.storeId = storeId; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public double getStartingPrice() { return startingPrice; }
    public void setStartingPrice(double startingPrice) { this.startingPrice = startingPrice; }
    
    public long getEndTime() { return endTime;}
    public void setEndTime(long endTime) { this.endTime = endTime;}

    public List<Offer> getOffers() { return offers; }
    public void setOffers(List<Offer> offers) { this.offers = offers; }

    public String getWinningUserId() { return winningUserId; }
    public void setWinningUserId(String winningUserId) { this.winningUserId = winningUserId; }

    public Double getWinningPrice() { return winningPrice; }
    public void setWinningPrice(Double winningPrice) { this.winningPrice = winningPrice; }

}
