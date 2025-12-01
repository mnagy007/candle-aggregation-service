package com.multibnakgroup.candle.aggregation.controller;

import com.multibnakgroup.candle.aggregation.repository.CandleRepository;
import com.multibnakgroup.candle.aggregation.model.Candle;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/candles")
public class CandleController {

    private final CandleRepository candleRepository;

    public CandleController(CandleRepository candleRepository) {
        this.candleRepository = candleRepository;
    }

    @GetMapping("/history")
    public ResponseEntity<List<Candle>> getCandles(
            @RequestParam String symbol,
            @RequestParam String interval,
            @RequestParam(required = false) Long from,
            @RequestParam(required = false) Long to,
            @RequestParam(required = false, defaultValue = "100") Integer limit) {

        List<Candle> candles = candleRepository.findCandles(symbol, interval, from, to, limit);
        return ResponseEntity.ok(candles);
    }
}
