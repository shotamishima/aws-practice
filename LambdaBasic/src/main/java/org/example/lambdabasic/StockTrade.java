package org.example.lambdabasic;


public class StockTrade {

    public enum TradeType {
        BUY, SELL
    }

    private String tickerSymbol;
    private TradeType tradeType;
    private double price;
    private long quantity;
    private long id;

    public StockTrade() {}

    public StockTrade(String tickerSymbol, TradeType tradeType, double price, long quantity,
            long id) {
        this.tickerSymbol = tickerSymbol;
        this.tradeType = tradeType;
        this.price = price;
        this.quantity = quantity;
        this.id = id;
    }

    public String getTickerSymbol() {
        return tickerSymbol;
    }

    public TradeType getTradeType() {
        return tradeType;
    }

    public double getPrice() {
        return price;
    }

    public long getQuantity() {
        return quantity;
    }

    public long getId() {
        return id;
    }

    @Override
    public String toString() {
        return String.format(" ID %d: %s %d shares of %s for $%.02f", id, tradeType, quantity,
                tickerSymbol, price);
    }
}
