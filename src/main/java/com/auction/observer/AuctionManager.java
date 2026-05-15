package com.auction.observer;

import com.auction.model.Item;
import com.uet.auction.DataManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * AuctionManager - ket hop Singleton + Observer + Subject.
 * - Singleton: chi co mot instance quan ly toan bo phien dau gia.
 * - Subject: luu danh sach Observer va notify khi co thay doi.
 */
public class AuctionManager implements AuctionSubject {

    // ── Singleton ───────────────────────────────────────────────────
    private static AuctionManager instance;

    private AuctionManager() {}

    public static synchronized AuctionManager getInstance() {
        if (instance == null) {
            instance = new AuctionManager();
        }
        return instance;
    }

    // ── Observer list ────────────────────────────────────────────────
    private final List<AuctionObserver> observers = new ArrayList<>();

    @Override
    public synchronized void addObserver(AuctionObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public synchronized void removeObserver(AuctionObserver observer) {
        observers.remove(observer);
    }

    @Override
    public synchronized void notifyObservers() {
        for (AuctionObserver obs : observers) {
            obs.onProductListChanged();
        }
    }

    public synchronized void notifyProductUpdated(Item item) {
        for (AuctionObserver obs : observers) {
            obs.onProductUpdated(item);
        }
    }

    public synchronized void notifyAuctionEnded(Item item, String winnerName) {
        for (AuctionObserver obs : observers) {
            obs.onAuctionEnded(item, winnerName);
        }
    }

    // ── Tu dong ket thuc phien dau gia ──────────────────────────────
    private ScheduledExecutorService scheduler;

    /**
     * Khoi dong scheduler kiem tra phien ket thuc moi 30 giay.
     */
    public void startAuctionScheduler() {
        if (scheduler != null && !scheduler.isShutdown()) return;

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "AuctionScheduler");
            t.setDaemon(true);
            return t;
        });

        scheduler.scheduleAtFixedRate(() -> {
            try {
                List<Item> ended = DataManager.closeExpiredAuctions();
                for (Item item : ended) {
                    String winner = DataManager.getWinnerForItem(item.getId());
                    notifyAuctionEnded(item, winner);
                    System.out.println("[AuctionManager] Phien da ket thuc: "
                            + item.getName() + " | Winner: " + winner);
                }
                if (!ended.isEmpty()) {
                    notifyObservers();
                }
            } catch (Exception e) {
                System.err.println("[AuctionManager] Loi scheduler: "
                        + e.getMessage());
            }
        }, 10, 30, TimeUnit.SECONDS);

        System.out.println("[AuctionManager] Scheduler da khoi dong.");
    }

    public void stopScheduler() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }
}