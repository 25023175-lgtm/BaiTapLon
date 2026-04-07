package com.auction.auctionweb.controller;

import com.auction.auctionweb.model.Product;
import com.auction.auctionweb.model.User;
import com.auction.auctionweb.repository.ProductRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @GetMapping("/product/add")
    public String showAddProduct(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || !user.getRole().equals("SELLER")) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        return "add-product";
    }

    @PostMapping("/product/add")
    public String addProduct(@RequestParam String name,
                             @RequestParam String description,
                             @RequestParam String imageUrl,
                             @RequestParam double startPrice,
                             @RequestParam int duration,
                             HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !user.getRole().equals("SELLER")) {
            return "redirect:/login";
        }

        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setImageUrl(imageUrl);
        product.setStartPrice(startPrice);
        product.setCurrentPrice(startPrice);
        product.setStartTime(LocalDateTime.now());
        product.setEndTime(LocalDateTime.now().plusHours(duration));
        product.setStatus("ACTIVE");
        product.setSeller(user);

        productRepository.save(product);
        return "redirect:/seller/products";
    }

    @GetMapping("/seller/products")
    public String sellerProducts(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || !user.getRole().equals("SELLER")) {
            return "redirect:/login";
        }
        List<Product> products = productRepository.findBySeller(user);
        model.addAttribute("products", products);
        model.addAttribute("user", user);
        return "seller-products";
    }

    @GetMapping("/products")
    public String allProducts(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        model.addAttribute("user", user);
        List<Product> products = productRepository.findByStatus("ACTIVE");
        model.addAttribute("products", products);
        return "products";
    }

    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable Long id, Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        model.addAttribute("user", user);
        Product product = productRepository.findById(id).orElse(null);
        if (product == null) {
            return "redirect:/products";
        }
        model.addAttribute("product", product);
        return "product-detail";
    }
}
