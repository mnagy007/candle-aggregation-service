package com.multibnakgroup.candle.aggregation.repository;

import com.multibnakgroup.candle.aggregation.model.Candle;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class CandleRepository {

    private final Map<String, Map<String, NavigableMap<Long, Candle>>> storage =
            new ConcurrentHashMap<>();

    public void save(String symbol, String interval, Candle candle) {
        storage.computeIfAbsent(symbol, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(interval, k -> new TreeMap<>())
                .put(candle.time(), candle);
    }

    public List<Candle> findCandles(String symbol, String interval, Long from, Long to, Integer limit) {
        Map<String, NavigableMap<Long, Candle>> symbolData = storage.get(symbol);
        if (symbolData == null) {
            return Collections.emptyList();
        }

        NavigableMap<Long, Candle> intervalData = symbolData.get(interval);
        if (intervalData == null) {
            return Collections.emptyList();
        }

        // Get candles in time range
        NavigableMap<Long, Candle> rangeMap = intervalData;
        if (from != null && to != null) {
            rangeMap = intervalData.subMap(from, true, to, true);
        } else if (from != null) {
            rangeMap = intervalData.tailMap(from, true);
        } else if (to != null) {
            rangeMap = intervalData.headMap(to, true);
        }

        List<Candle> candles = new ArrayList<>(rangeMap.values());

        // Apply limit
        if (limit != null && limit > 0 && candles.size() > limit) {
            candles = candles.subList(Math.max(0, candles.size() - limit), candles.size());
        }

        return candles;
    }

    public List<String> getAllSymbols() {
        return new ArrayList<>(storage.keySet());
    }

    public void clear() {
        storage.clear();
    }
}
