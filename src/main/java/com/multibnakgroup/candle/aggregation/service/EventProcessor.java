package com.multibnakgroup.candle.aggregation.service;

import com.multibnakgroup.candle.aggregation.aggregator.CandleAggregatorManager;
import com.multibnakgroup.candle.aggregation.model.BidAskEvent;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class EventProcessor {

    private final CandleAggregatorManager aggregatorManager;
    private final BlockingQueue<BidAskEvent> eventQueue = new LinkedBlockingQueue<>(10000);
    private final Random random = new Random();

    // Symbol base prices
    private final Map<String, Double> basePrices = Map.of(
            "BTC-USD", 95000.0,
            "ETH-USD", 3500.0,
            "SOL-USD", 200.0,
            "AAPL", 180.0
    );

    public EventProcessor(CandleAggregatorManager aggregatorManager) {
        this.aggregatorManager = aggregatorManager;
        startProcessing();
    }

    private void startProcessing() {
        Thread processorThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    BidAskEvent event = eventQueue.take();
                    aggregatorManager.processEvent(event);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        processorThread.setDaemon(true);
        processorThread.start();
    }

    @Scheduled(fixedRate = 100) // Generate events every 100ms
    public void generateEvents() {
        for (Map.Entry<String, Double> entry : basePrices.entrySet()) {
            String symbol = entry.getKey();
            double basePrice = entry.getValue();

            // Add some random volatility
            double variation = basePrice * 0.001 * (random.nextDouble() - 0.5);
            double bid = basePrice + variation;
            double ask = bid + (basePrice * 0.0001); // Small spread

            BidAskEvent event = new BidAskEvent(
                    symbol,
                    bid,
                    ask,
                    System.currentTimeMillis()
            );

            eventQueue.offer(event);
        }
    }

    public void ingestEvent(BidAskEvent event) {
        eventQueue.offer(event);
    }
}
