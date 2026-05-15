package com.auction.model;

/** Nguoi ban - co the dang san pham va quan ly phien dau gia cua minh. */
public class Seller extends User {
    private static final long serialVersionUID = 1L;

    public Seller() {
        super();
        setRole("Seller");
    }

    public Seller(String username, String password,
                  String email, String fullName) {
        super(username, password, email, fullName, "Seller");
    }

    @Override
    public String getPermissionDescription() {
        return "Seller: Dang san pham, chinh sua, xoa san pham cua minh";
    }

    @Override
    public boolean hasPermission(String action) {
        return switch (action) {
            case "VIEW_PRODUCTS", "ADD_PRODUCT",
                 "DELETE_OWN_PRODUCT", "VIEW_CHART" -> true;
            default -> false;
        };
    }
}