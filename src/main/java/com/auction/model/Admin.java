package com.auction.model;

/** Quan tri vien - co toan quyen tren he thong. */
public class Admin extends User {
    private static final long serialVersionUID = 1L;

    public Admin() {
        super();
        setRole("Admin");
    }

    public Admin(String username, String password,
                 String email, String fullName) {
        super(username, password, email, fullName, "Admin");
    }

    @Override
    public String getPermissionDescription() {
        return "Admin: Toan quyen - quan ly nguoi dung, san pham, phien dau gia";
    }

    @Override
    public boolean hasPermission(String action) {
        // Admin co toan quyen
        return true;
    }
}