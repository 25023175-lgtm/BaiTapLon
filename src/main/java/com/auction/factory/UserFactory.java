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
        // Normalize: trim + capitalize de xu ly ca "bidder", "SELLER"...
        String normalized = normalizeRole(role);

        return switch (normalized) {
            case "Bidder" -> new Bidder(username, password, email, fullName);
            case "Seller" -> new Seller(username, password, email, fullName);
            case "Admin"  -> new Admin(username, password, email, fullName);
            // Vai tro hop le nhung viet sai hoa thuong -> Bidder mac dinh
            // Vai tro hoan toan la -> throw de test bat duoc
            default -> throw new IllegalArgumentException(
                    "Vai tro khong hop le: " + role);
        };
    }

    public static User createFromDb(int id, String username, String password,
                                    String email, String fullName,
                                    String role, double balance) {
        // Khi doc tu DB: neu role la -> fallback Bidder de tranh crash
        User user;
        try {
            user = create(username, password, email, fullName, role);
        } catch (IllegalArgumentException e) {
            System.out.println(">> Role khong hop le trong DB [" + role
                    + "], mac dinh Bidder.");
            user = new Bidder(username, password, email, fullName);
        }
        user.setId(id);
        user.setBalance(balance);
        return user;
    }

    /**
     * Chuan hoa role: trim + capitalize.
     * "bidder" -> "Bidder", "SELLER" -> "Seller"
     * Chuoi rong hoac null -> "" (se throw o switch)
     */
    private static String normalizeRole(String role) {
        if (role == null || role.trim().isEmpty()) return "";
        String t = role.trim();
        return t.substring(0, 1).toUpperCase()
                + t.substring(1).toLowerCase();
    }
}