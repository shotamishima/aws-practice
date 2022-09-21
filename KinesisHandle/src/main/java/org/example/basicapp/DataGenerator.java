package org.example.basicapp;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Generates random data by picking randomly from a collection of data.
 */

public class DataGenerator {

    private static final List<StockPrice> STOCK_PRICES = new ArrayList<StockPrice>();
    static {
        STOCK_PRICES.add(new StockPrice("AAPL", 119.72));
        STOCK_PRICES.add(new StockPrice("XOM", 91.56));
        STOCK_PRICES.add(new StockPrice("GOOG", 527.83));
        STOCK_PRICES.add(new StockPrice("BRK.A", 223999.88));
        STOCK_PRICES.add(new StockPrice("MSFT", 42.36));
        STOCK_PRICES.add(new StockPrice("WFC", 54.21));
        STOCK_PRICES.add(new StockPrice("JNJ", 99.78));
        STOCK_PRICES.add(new StockPrice("WMT", 85.91));
        STOCK_PRICES.add(new StockPrice("CHL", 66.96));
    }

    private static final double MAX_DEVIATION = 0.2;
    private static final int MAX_QUANTITY = 1000;
    private static final double PROBABILITY_SELL = 0.4;
    private final Random random = new Random();
    private AtomicLong id = new AtomicLong(1);

    /**
     * Return a random stock trade with a unique id every time.
     */
    public StockTrade getRandomData() {
        StockPrice stockPrice = STOCK_PRICES.get(random.nextInt(STOCK_PRICES.size()));
        double devation = (random.nextDouble() - 0.5) * 2.0 * MAX_DEVIATION;
        double price = stockPrice.price * (1 + devation);
        price = Math.round(price * 100.0) / 100.0;

        StockTrade.TradeType tradeType = StockTrade.TradeType.BUY;
        if (random.nextDouble() < PROBABILITY_SELL) {
            tradeType = StockTrade.TradeType.SELL;
        }

        long quantity = random.nextInt(MAX_QUANTITY) + 1;

        return new StockTrade(stockPrice.tickerSymbol, tradeType, price, quantity,
                id.getAndIncrement());
    }

    private static class StockPrice {
        String tickerSymbol;
        double price;

        StockPrice(String tickerSymString, double price) {
            this.tickerSymbol = tickerSymString;
            this.price = price;
        }
    }
}
