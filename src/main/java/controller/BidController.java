package com.auction.auctionweb.controller;

import com.auction.auctionweb.model.Bid;
import com.auction.auctionweb.model.Product;
import com.auction.auctionweb.model.User;
import com.auction.auctionweb.repository.BidRepository;
import com.auction.auctionweb.repository.ProductRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class BidController {

    @Autowired
    private BidRepository bidRepository;

    @Autowired
    private ProductRepository productRepository;

    @PostMapping("/bid/{productId}")
    public String placeBid(@PathVariable Long productId,
                           @RequestParam double amount,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {

        User user = (User) session.getAttribute("user");
        if (user == null || !user.getRole().equals("BUYER")) {
            return "redirect:/login";
        }

        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            return "redirect:/products";
        }

        // Kiểm tra hết giờ chưa
        if (LocalDateTime.now().isAfter(product.getEndTime())) {
            product.setStatus("ENDED");
            productRepository.save(product);
            redirectAttributes.addFlashAttribute("error", "Phiên đấu giá đã kết thúc!");
            return "redirect:/product/" + productId;
        }

        // Kiểm tra giá phải cao hơn giá hiện tại
        if (amount <= product.getCurrentPrice()) {
            redirectAttributes.addFlashAttribute("error", "Giá phải cao hơn " + product.getCurrentPrice() + "!");
            return "redirect:/product/" + productId;
        }

        // Lưu bid
        Bid bid = new Bid();
        bid.setAmount(amount);
        bid.setBidTime(LocalDateTime.now());
        bid.setProduct(product);
        bid.setBidder(user);
        bidRepository.save(bid);

        // Cập nhật giá hiện tại
        product.setCurrentPrice(amount);
        product.setWinner(user);
        productRepository.save(product);

        redirectAttributes.addFlashAttribute("success", "Đặt giá thành công!");
        return "redirect:/product/" + productId;
    }
}
