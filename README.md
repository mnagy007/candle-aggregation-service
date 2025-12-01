# Candle Aggregation Service

A Spring Boot service that converts real-time market data (bid/ask prices) into candlestick charts (OHLC format).

## What It Does

- Receives market price data streams
- Aggregates data into candles for multiple timeframes (1s, 5s, 1m, 5m, 15m, 1h)
- Stores candle history in memory
- Provides REST API to fetch historical candles

## Quick Start

### Requirements

- Java 17+
- Maven

### Run

```bash
# Clone and build
git clone <repo-url>
cd candle-aggregation-service
mvn clean install

# Start the service
mvn spring-boot:run
```

Service starts at `http://localhost:8080`

 ### Health Check API at 'http://localhost:8080/actuator/health'

## API Usage

### Get Candles

```bash
curl "http://localhost:8080/api/candles?symbol=BTC-USD&interval=1m&limit=10"
```

**Parameters:**
- `symbol` - Trading pair (BTC-USD, ETH-USD, SOL-USD, AAPL)
- `interval` - Time period (1s, 5s, 1m, 5m, 15m, 1h)
- `from` - Start time (optional, milliseconds)
- `to` - End time (optional, milliseconds)
- `limit` - Max candles (optional, default 100)

**Response:**
```json
[
  {
    "time": 1700000000000,
    "open": 95000.50,
    "high": 95150.25,
    "low": 94980.00,
    "close": 95100.75,
    "volume": 125
  }
]
```

## Project Structure

```
src/main/java/com/multibankgroup/candle/aggregation/
├── model/              # Data models (BidAskEvent, Candle)
├── aggregator/         # Core logic (aggregation + management)
├── repository/         # Data storage
├── service/            # Event processing
└── controller/         # REST API
```

## Testing

```bash
# Run all tests
mvn test

# Run with coverage
mvn test jacoco:report
```

## How It Works

1. **EventProcessor** generates simulated market data every 100ms
2. **CandleAggregator** collects events and calculates OHLC per time window
3. **CandleAggregatorManager** closes windows and saves completed candles
4. **CandleRepository** stores data in memory (ConcurrentHashMap)
5. **CandleController** serves data via REST API

## Configuration

Edit `src/main/resources/application.yml`:

```yaml
server:
  port: 8080

logging:
  level:
    com.trading.candle: INFO
```


