package com.multibnakgroup.candle.aggregation;

import com.multibnakgroup.candle.aggregation.aggregator.CandleAggregator;
import com.multibnakgroup.candle.aggregation.model.BidAskEvent;
import com.multibnakgroup.candle.aggregation.model.Candle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CandleAggregatorTest {

    private CandleAggregator aggregator;
    private static final String SYMBOL = "BTC-USD";
    private static final String INTERVAL = "1m";
    private static final long INTERVAL_MILLIS = 60000L;

    @BeforeEach
    void setUp() {
        aggregator = new CandleAggregator(SYMBOL, INTERVAL, INTERVAL_MILLIS);
    }

    @Test
    @DisplayName("Should calculate correct OHLC from single event")
    void testSingleEvent() {
        // Given
        BidAskEvent event = new BidAskEvent(SYMBOL, 100.0, 101.0, System.currentTimeMillis());
        double expectedMidPrice = 100.5;

        // When
        aggregator.processEvent(event);
        Candle candle = aggregator.closeWindow();

        // Then
        assertNotNull(candle);
        assertEquals(expectedMidPrice, candle.open());
        assertEquals(expectedMidPrice, candle.high());
        assertEquals(expectedMidPrice, candle.low());
        assertEquals(expectedMidPrice, candle.close());
        assertEquals(1L, candle.volume());
    }

    @Test
    @DisplayName("Should calculate correct OHLC from multiple events")
    void testMultipleEvents() {
        // Given
        long now = System.currentTimeMillis();
        BidAskEvent event1 = new BidAskEvent(SYMBOL, 100.0, 101.0, now);      // Mid: 100.5
        BidAskEvent event2 = new BidAskEvent(SYMBOL, 102.0, 103.0, now + 100); // Mid: 102.5
        BidAskEvent event3 = new BidAskEvent(SYMBOL, 98.0, 99.0, now + 200);   // Mid: 98.5
        BidAskEvent event4 = new BidAskEvent(SYMBOL, 101.0, 102.0, now + 300); // Mid: 101.5

        // When
        aggregator.processEvent(event1);
        aggregator.processEvent(event2);
        aggregator.processEvent(event3);
        aggregator.processEvent(event4);
        Candle candle = aggregator.closeWindow();

        // Then
        assertNotNull(candle);
        assertEquals(100.5, candle.open());   // First price
        assertEquals(102.5, candle.high());   // Highest price
        assertEquals(98.5, candle.low());     // Lowest price
        assertEquals(101.5, candle.close());  // Last price
        assertEquals(4L, candle.volume());
    }

    @Test
    @DisplayName("Should return null when closing window with no events")
    void testCloseEmptyWindow() {
        // When
        Candle candle = aggregator.closeWindow();

        // Then
        assertNull(candle);
    }

    @Test
    @DisplayName("Should reset state after closing window")
    void testResetAfterClose() {
        // Given
        BidAskEvent event1 = new BidAskEvent(SYMBOL, 100.0, 101.0, System.currentTimeMillis());
        BidAskEvent event2 = new BidAskEvent(SYMBOL, 200.0, 201.0, System.currentTimeMillis());

        // When
        aggregator.processEvent(event1);
        Candle candle1 = aggregator.closeWindow();

        aggregator.processEvent(event2);
        Candle candle2 = aggregator.closeWindow();

        // Then
        assertNotNull(candle1);
        assertNotNull(candle2);
        assertEquals(100.5, candle1.open());
        assertEquals(200.5, candle2.open());
        assertTrue(candle2.time() > candle1.time());
    }

    @Test
    @DisplayName("Should handle concurrent event processing")
    void testConcurrentProcessing() throws InterruptedException {
        // Given
        int threadCount = 10;
        int eventsPerThread = 100;
        Thread[] threads = new Thread[threadCount];

        // When
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < eventsPerThread; j++) {
                    BidAskEvent event = new BidAskEvent(
                            SYMBOL,
                            100.0 + threadId,
                            101.0 + threadId,
                            System.currentTimeMillis()
                    );
                    aggregator.processEvent(event);
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        Candle candle = aggregator.closeWindow();

        // Then
        assertNotNull(candle);
        assertEquals(threadCount * eventsPerThread, candle.volume());
    }
}
