import java.math.BigDecimal;
public class StockPrice {
    String symbol;
    BigDecimal price;
    public StockPrice(String symbol, BigDecimal price) {
        this.symbol = symbol;
        this.price = price; }
    public String getSymbol() {
        return this.symbol; }
    public BigDecimal getPrice() {
        return this.price;
    }
}

