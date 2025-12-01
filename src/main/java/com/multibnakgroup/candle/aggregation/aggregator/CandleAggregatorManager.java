package com.multibnakgroup.candle.aggregation.aggregator;

import com.multibnakgroup.candle.aggregation.repository.CandleRepository;
import com.multibnakgroup.candle.aggregation.model.BidAskEvent;
import com.multibnakgroup.candle.aggregation.model.Candle;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CandleAggregatorManager {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CandleAggregatorManager.class);

    private final CandleRepository candleRepository;
    private final Map<String, Map<String, CandleAggregator>> aggregators = new ConcurrentHashMap<>();

    // Supported intervals in milliseconds
    private static final Map<String, Long> INTERVALS = Map.of(
            "1s", 1000L,
            "5s", 5000L,
            "1m", 60000L,
            "5m", 300000L,
            "15m", 900000L,
            "1h", 3600000L
    );

    public CandleAggregatorManager(CandleRepository candleRepository) {
        this.candleRepository = candleRepository;
    }

    public void processEvent(BidAskEvent event) {
        Map<String, CandleAggregator> symbolAggregators =
                aggregators.computeIfAbsent(event.symbol(), k -> new ConcurrentHashMap<>());

        // Process event for all intervals
        for (Map.Entry<String, Long> entry : INTERVALS.entrySet()) {
            String interval = entry.getKey();
            long intervalMillis = entry.getValue();

            CandleAggregator aggregator = symbolAggregators.computeIfAbsent(
                    interval,
                    k -> new CandleAggregator(event.symbol(), interval, intervalMillis)
            );

            aggregator.processEvent(event);
        }
    }

    @Scheduled(fixedRate = 1000) // Check every second
    public void closeWindows() {
        log.info("");
        for (Map<String, CandleAggregator> symbolAggregators : aggregators.values()) {
            for (CandleAggregator aggregator : symbolAggregators.values()) {
                Candle candle = aggregator.closeWindow();
                if (candle != null) {
                    candleRepository.save(aggregator.getSymbol(), aggregator.getInterval(), candle);
                }
            }
        }
    }
}
