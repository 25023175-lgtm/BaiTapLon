package com.auction.auctionweb.repository;

import com.auction.auctionweb.model.Product;
import com.auction.auctionweb.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByStatus(String status);
    List<Product> findBySeller(User seller);
}
