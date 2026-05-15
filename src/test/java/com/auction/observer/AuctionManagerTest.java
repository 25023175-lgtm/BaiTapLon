package com.auction.observer;

import com.auction.model.Item;
import com.auction.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class AuctionManagerTest {

    // Observer gia lap de dem so lan duoc goi
    static class FakeObserver implements AuctionObserver {
        int updatedCount = 0;
        int endedCount = 0;
        int listChangedCount = 0;
        String lastWinner = null;

        @Override
        public void onProductUpdated(Item item) { updatedCount++; }

        @Override
        public void onAuctionEnded(Item item, String winnerName) {
            endedCount++;
            lastWinner = winnerName;
        }

        @Override
        public void onProductListChanged() { listChangedCount++; }
    }

    private AuctionManager manager;
    private FakeObserver observer;

    @BeforeEach
    void setUp() {
        manager = AuctionManager.getInstance();
        observer = new FakeObserver();
    }

    @Test
    void testSingleton() {
        AuctionManager a = AuctionManager.getInstance();
        AuctionManager b = AuctionManager.getInstance();
        assertSame(a, b); // Phai la cung 1 instance
    }

    @Test
    void testAddAndNotifyObserver() {
        manager.addObserver(observer);
        manager.notifyObservers();
        assertTrue(observer.listChangedCount >= 1);
        manager.removeObserver(observer);
    }

    @Test
    void testRemoveObserver() {
        manager.addObserver(observer);
        manager.removeObserver(observer);
        manager.notifyObservers();
        assertEquals(0, observer.listChangedCount);
    }

    @Test
    void testNotifyProductUpdated() {
        manager.addObserver(observer);
        Item item = new Product();
        item.setName("Test Item");
        manager.notifyProductUpdated(item);
        assertEquals(1, observer.updatedCount);
        manager.removeObserver(observer);
    }

    @Test
    void testNotifyAuctionEnded() {
        manager.addObserver(observer);
        Item item = new Product();
        item.setName("Test Item");
        manager.notifyAuctionEnded(item, "Nguyen Van A");
        assertEquals(1, observer.endedCount);
        assertEquals("Nguyen Van A", observer.lastWinner);
        manager.removeObserver(observer);
    }

    @Test
    void testAddSameObserverTwice() {
        manager.addObserver(observer);
        manager.addObserver(observer); // Them lan 2 -> khong duoc them trung
        manager.notifyObservers();
        assertEquals(1, observer.listChangedCount); // Chi duoc goi 1 lan
        manager.removeObserver(observer);
    }
}