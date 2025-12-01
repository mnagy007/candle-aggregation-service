package com.multibnakgroup.candle.aggregation.model;

public record BidAskEvent(
        String symbol,
        double bid,
        double ask,
        long timestamp
) {}
