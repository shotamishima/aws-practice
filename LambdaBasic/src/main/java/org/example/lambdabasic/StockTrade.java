package org.example.lambdabasic


public class StockTrade {

    public enum TradeType {
        BUY, SELL
    }

    public String tickerSymbol;
    public TradeType tradeType;
    public double price;
    public long quantity;
    public long id;

    public StockTrade() {}

    public StockTrade(String tickerSymbol, TradeType tradeType, double price, long quantity,
            long id) {
        this.tickerSymbol = tickerSymbol;
        this.tradeType = tradeType;
        this.price = price;
        this.quantity = quantity;
        this.id = id;
    }
}
