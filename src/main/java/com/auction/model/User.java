package com.auction.model;

import java.io.Serializable;

/**
 * Lop truu tuong dai dien cho nguoi dung trong he thong dau gia.
 * Cac vai tro cu the (Bidder, Seller, Admin) phai ke thua lop nay.
 */
public abstract class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String username;
    private String password;
    private String email;
    private String fullName;
    private double balance;
    private String role;

    public User() {}

    public User(String username, String password,
                String email, String fullName, String role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullName = fullName;
        this.balance = 0.0;
        this.role = role;
    }

    /** Mo ta quyen han cua tung vai tro (abstract - tinh truu tuong). */
    public abstract String getPermissionDescription();

    /** Kiem tra quyen - da hinh, moi subclass override rieng. */
    public abstract boolean hasPermission(String action);

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "'}";
    }
}