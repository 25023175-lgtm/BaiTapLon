package com.auction.auctionweb.repository;

import com.auction.auctionweb.model.Bid;
import com.auction.auctionweb.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BidRepository extends JpaRepository<Bid, Long> {
    List<Bid> findByProductOrderByAmountDesc(Product product);
}
