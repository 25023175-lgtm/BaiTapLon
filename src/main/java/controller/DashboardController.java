package com.auction.auctionweb.controller;

import com.auction.auctionweb.model.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpSession;

@Controller
public class DashboardController {

    @GetMapping("/admin/dashboard")
    public String adminDashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        return "admin-dashboard";
    }

    @GetMapping("/seller/dashboard")
    public String sellerDashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"SELLER".equals(user.getRole())) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        return "seller-dashboard";
    }

    @GetMapping("/buyer/dashboard")
    public String buyerDashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"BUYER".equals(user.getRole())) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        return "buyer-dashboard";
    }
}
