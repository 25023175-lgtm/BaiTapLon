package com.auction.factory;

import com.auction.model.Admin;
import com.auction.model.Bidder;
import com.auction.model.Seller;
import com.auction.model.User;

/**
 * Factory Method Pattern - tao dung subclass User theo role.
 */
public class UserFactory {

    public static User create(String username, String password,
                              String email, String fullName, String role) {
        // Normalize role: bo khoang trang, capitalize chu cai dau
        // De xu ly ca truong hop DB luu "bidder", "BIDDER", "Bidder"...
        String normalizedRole = normalizeRole(role);

        return switch (normalizedRole) {
            case "Bidder" -> new Bidder(username, password, email, fullName);
            case "Seller" -> new Seller(username, password, email, fullName);
            case "Admin"  -> new Admin(username, password, email, fullName);
            // Mac dinh tra ve Bidder thay vi throw exception
            // Tranh truong hop DB co role la null hoac ky tu la
            default -> new Bidder(username, password, email, fullName);
        };
    }

    public static User createFromDb(int id, String username, String password,
                                    String email, String fullName,
                                    String role, double balance) {
        User user = create(username, password, email, fullName, role);
        user.setId(id);
        user.setBalance(balance);
        return user;
    }

    /**
     * Chuan hoa role: trim + capitalize chu cai dau.
     * "bidder" -> "Bidder", "SELLER" -> "Seller", " Admin " -> "Admin"
     */
    private static String normalizeRole(String role) {
        if (role == null || role.trim().isEmpty()) return "Bidder";
        String trimmed = role.trim();
        return trimmed.substring(0, 1).toUpperCase()
                + trimmed.substring(1).toLowerCase();
    }
}