package com.multibnakgroup.candle.aggregation;

import com.multibnakgroup.candle.aggregation.model.Candle;
import com.multibnakgroup.candle.aggregation.repository.CandleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CandleRepositoryTest {

    private CandleRepository repository;
    private static final String SYMBOL = "BTC-USD";
    private static final String INTERVAL = "1m";

    @BeforeEach
    void setUp() {
        repository = new CandleRepository();
    }

    @Test
    @DisplayName("Should save and retrieve candle")
    void testSaveAndRetrieve() {
        // Given
        Candle candle = new Candle(1000L, 100.0, 105.0, 95.0, 102.0, 10L);

        // When
        repository.save(SYMBOL, INTERVAL, candle);
        List<Candle> candles = repository.findCandles(SYMBOL, INTERVAL, null, null, null);

        // Then
        assertEquals(1, candles.size());
        assertEquals(candle, candles.get(0));
    }

    @Test
    @DisplayName("Should retrieve candles in time range")
    void testFindCandlesInRange() {
        // Given
        repository.save(SYMBOL, INTERVAL, new Candle(1000L, 100.0, 105.0, 95.0, 102.0, 10L));
        repository.save(SYMBOL, INTERVAL, new Candle(2000L, 102.0, 107.0, 97.0, 104.0, 15L));
        repository.save(SYMBOL, INTERVAL, new Candle(3000L, 104.0, 109.0, 99.0, 106.0, 20L));
        repository.save(SYMBOL, INTERVAL, new Candle(4000L, 106.0, 111.0, 101.0, 108.0, 25L));

        // When
        List<Candle> candles = repository.findCandles(SYMBOL, INTERVAL, 2000L, 3000L, null);

        // Then
        assertEquals(2, candles.size());
        assertEquals(2000L, candles.get(0).time());
        assertEquals(3000L, candles.get(1).time());
    }

    @Test
    @DisplayName("Should apply limit correctly")
    void testLimit() {
        // Given
        for (int i = 1; i <= 10; i++) {
            repository.save(SYMBOL, INTERVAL,
                    new Candle(i * 1000L, 100.0, 105.0, 95.0, 102.0, 10L));
        }

        // When
        List<Candle> candles = repository.findCandles(SYMBOL, INTERVAL, null, null, 5);

        // Then
        assertEquals(5, candles.size());
        assertEquals(6000L, candles.get(0).time()); // Last 5 candles
        assertEquals(10000L, candles.get(4).time());
    }

    @Test
    @DisplayName("Should return empty list for unknown symbol")
    void testUnknownSymbol() {
        // When
        List<Candle> candles = repository.findCandles("UNKNOWN", INTERVAL, null, null, null);

        // Then
        assertTrue(candles.isEmpty());
    }

    @Test
    @DisplayName("Should handle multiple symbols and intervals")
    void testMultipleSymbolsAndIntervals() {
        // Given
        repository.save("BTC-USD", "1m", new Candle(1000L, 100.0, 105.0, 95.0, 102.0, 10L));
        repository.save("BTC-USD", "5m", new Candle(1000L, 100.0, 110.0, 90.0, 105.0, 50L));
        repository.save("ETH-USD", "1m", new Candle(1000L, 200.0, 205.0, 195.0, 202.0, 20L));

        // When
        List<Candle> btc1m = repository.findCandles("BTC-USD", "1m", null, null, null);
        List<Candle> btc5m = repository.findCandles("BTC-USD", "5m", null, null, null);
        List<Candle> eth1m = repository.findCandles("ETH-USD", "1m", null, null, null);
        List<String> symbols = repository.getAllSymbols();

        // Then
        assertEquals(1, btc1m.size());
        assertEquals(1, btc5m.size());
        assertEquals(1, eth1m.size());
        assertEquals(2, symbols.size());
        assertTrue(symbols.contains("BTC-USD"));
        assertTrue(symbols.contains("ETH-USD"));
    }

    @Test
    @DisplayName("Should clear all data")
    void testClear() {
        // Given
        repository.save(SYMBOL, INTERVAL, new Candle(1000L, 100.0, 105.0, 95.0, 102.0, 10L));

        // When
        repository.clear();
        List<Candle> candles = repository.findCandles(SYMBOL, INTERVAL, null, null, null);

        // Then
        assertTrue(candles.isEmpty());
    }
}
