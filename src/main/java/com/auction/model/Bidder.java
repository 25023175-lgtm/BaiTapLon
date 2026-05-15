package com.auction.model;

/** Nguoi mua - co the xem va dat gia san pham. */
public class Bidder extends User {
    private static final long serialVersionUID = 1L;

    public Bidder() {
        super();
        setRole("Bidder");
    }

    public Bidder(String username, String password,
                  String email, String fullName) {
        super(username, password, email, fullName, "Bidder");
    }

    @Override
    public String getPermissionDescription() {
        return "Bidder: Xem san pham, dat gia, theo doi phien dau gia";
    }

    @Override
    public boolean hasPermission(String action) {
        return switch (action) {
            case "VIEW_PRODUCTS", "PLACE_BID",
                 "VIEW_CHART", "VIEW_BID_HISTORY" -> true;
            default -> false;
        };
    }
}