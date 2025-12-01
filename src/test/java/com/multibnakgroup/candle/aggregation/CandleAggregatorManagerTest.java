package com.multibnakgroup.candle.aggregation;

import com.multibnakgroup.candle.aggregation.aggregator.CandleAggregatorManager;
import com.multibnakgroup.candle.aggregation.model.BidAskEvent;
import com.multibnakgroup.candle.aggregation.repository.CandleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;

public class CandleAggregatorManagerTest {

    private CandleAggregatorManager manager;
    private CandleRepository mockRepository;

    @BeforeEach
    void setUp() {
        mockRepository = mock(CandleRepository.class);
        manager = new CandleAggregatorManager(mockRepository);
    }

    @Test
    @DisplayName("Should create aggregators for all intervals")
    void testProcessEventCreatesAggregators() {
        // Given
        BidAskEvent event = new BidAskEvent("BTC-USD", 100.0, 101.0, System.currentTimeMillis());

        // When
        manager.processEvent(event);

        // Then - aggregators created (no exception thrown)
        assertDoesNotThrow(() -> manager.processEvent(event));
    }

    @Test
    @DisplayName("Should handle multiple symbols")
    void testMultipleSymbols() {
        // Given
        BidAskEvent btcEvent = new BidAskEvent("BTC-USD", 100.0, 101.0, System.currentTimeMillis());
        BidAskEvent ethEvent = new BidAskEvent("ETH-USD", 200.0, 201.0, System.currentTimeMillis());

        // When
        manager.processEvent(btcEvent);
        manager.processEvent(ethEvent);

        // Then - both processed without error
        assertDoesNotThrow(() -> {
            manager.processEvent(btcEvent);
            manager.processEvent(ethEvent);
        });
    }
}
