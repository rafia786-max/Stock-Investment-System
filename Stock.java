import java.math.BigDecimal;

public class Stock {

    private String symbol;
    private BigDecimal price;
    private int shares;

    Stock(String symbol, BigDecimal price, int shares){

        this.symbol = symbol;
        this.price = price;
        this.shares = shares;
    }

    String getSymbol(){

        return this.symbol;
    }
    int getShares(){

        return this.shares;
    }
    BigDecimal getPrice(){

        return this.price;
    }

    void setShares(int shares){

        this.shares = shares;
    }

}


