package com.auction.observer;

import com.auction.model.Item;

/**
 * Observer Pattern - interface ma moi "nguoi nghe" phai implement.
 * Vi du: DashboardController la Observer, AuctionManager la Subject.
 */
public interface AuctionObserver {
    /** Goi khi co san pham moi hoac gia thay doi. */
    void onProductUpdated(Item item);

    /** Goi khi co phien dau gia ket thuc. */
    void onAuctionEnded(Item item, String winnerName);

    /** Goi khi danh sach san pham thay doi (them/xoa). */
    void onProductListChanged();
}