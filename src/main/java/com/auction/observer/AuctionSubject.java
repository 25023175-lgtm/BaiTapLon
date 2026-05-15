package com.auction.observer;

/**
 * Subject interface - doi tuong quan sat (AuctionManager).
 * Quan ly danh sach Observer va thong bao cho ho.
 */
public interface AuctionSubject {
    void addObserver(AuctionObserver observer);
    void removeObserver(AuctionObserver observer);
    void notifyObservers();
}