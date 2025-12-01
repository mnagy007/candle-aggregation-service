package com.multibnakgroup.candle.aggregation.aggregator;

import com.multibnakgroup.candle.aggregation.model.BidAskEvent;
import com.multibnakgroup.candle.aggregation.model.Candle;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CandleAggregator {
    private final String symbol;
    private final String interval;
    private final long intervalMillis;

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private long windowStart;
    private Double open;
    private double high = Double.MIN_VALUE;
    private double low = Double.MAX_VALUE;
    private double close;
    private AtomicLong volume = new AtomicLong(0);

    public CandleAggregator(String symbol, String interval, long intervalMillis) {
        this.symbol = symbol;
        this.interval = interval;
        this.intervalMillis = intervalMillis;
        this.windowStart = System.currentTimeMillis() / intervalMillis * intervalMillis;
    }

    public void processEvent(BidAskEvent event) {
        double midPrice = (event.bid() + event.ask()) / 2.0;

        lock.writeLock().lock();
        try {
            if (open == null) {
                open = midPrice;
            }
            high = Math.max(high, midPrice);
            low = Math.min(low, midPrice);
            close = midPrice;
            volume.incrementAndGet();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Candle closeWindow() {
        lock.writeLock().lock();
        try {
            if (open == null) {
                return null; // No data in this window
            }

            Candle candle = new Candle(
                    windowStart,
                    open,
                    high,
                    low,
                    close,
                    volume.get()
            );

            // Reset for next window
            windowStart += intervalMillis;
            open = null;
            high = Double.MIN_VALUE;
            low = Double.MAX_VALUE;
            close = 0;
            volume.set(0);

            return candle;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public String getSymbol() {
        return symbol;
    }

    public String getInterval() {
        return interval;
    }
}
