import java.util.ArrayList;
import java.util.Scanner;
import java.io.*;
import java.math.BigDecimal;

public class Portfolio {

    ArrayList<Stock> stocks = new ArrayList<>();
    ArrayList<StockPrice> stockPrices = new ArrayList<>();

    // View User Owned Stocks
    void viewPortfolio() {
        loadPortfolioFromFile();

        int i = 1;

        if (stocks.isEmpty()) {
            System.out.println("===================");
            System.out.println("You have No Stocks at the moment.");
        }

        System.out.println("===================");

        for (Stock s : stocks) {
            System.out.println(i + ". Symbol: " + s.getSymbol()
                    + " - Price: $" + s.getPrice()
                    + " - Shares Owned: " + s.getShares());
            i++;
        }
    }

    // View User Statistics
    void viewStatistics() {

        BigDecimal totalInvested = BigDecimal.ZERO;
        BigDecimal currentValue = BigDecimal.ZERO;
        BigDecimal totalSale = BigDecimal.ZERO;
        BigDecimal costOfSoldShares = BigDecimal.ZERO;
        BigDecimal totalGainLoss = BigDecimal.ZERO;

        loadPortfolioFromFile();
        loadStockPrices();

        System.out.println("\n=== Portfolio Statistics ===");

        // Current Holdings
        if (stocks.isEmpty()) {
            System.out.println("No stocks currently held.");
        } else {
            System.out.println("Total Stocks Owned: " + getTotalOwnedStocks());
            System.out.println("Total Shares Owned: " + getTotalOwnedShares());
        }

        // Calculate current portfolio value
        for (Stock s : stocks) {
            currentValue = currentValue.add(
                    getPriceOf(s.getSymbol()).multiply(BigDecimal.valueOf(s.getShares()))
            );
        }

        // Read transaction history
        try (Scanner fileScanner = new Scanner(new File("PortfolioStats.csv"))) {

            while (fileScanner.hasNextLine()) {

                String line = fileScanner.nextLine();
                String[] parts = line.split(",");

                if (parts.length == 4) {

                    String type = parts[0];
                    String symbol = parts[1];
                    BigDecimal buyPrice = new BigDecimal(parts[2]);
                    int shares = Integer.parseInt(parts[3]);

                    if (type.equals("BUY")) {

                        totalInvested = totalInvested.add(
                                buyPrice.multiply(BigDecimal.valueOf(shares))
                        );

                    } else if (type.equals("SELL")) {

                        BigDecimal marketPrice = getPriceOf(symbol);

                        totalSale = totalSale.add(
                                marketPrice.multiply(BigDecimal.valueOf(shares))
                        );

                        costOfSoldShares = costOfSoldShares.add(
                                buyPrice.multiply(BigDecimal.valueOf(shares))
                        );
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("An Error occurred: " + e.getMessage());
        }

        totalGainLoss = totalSale.subtract(costOfSoldShares);

        // Summary
        System.out.println("\nSummary:");
        System.out.printf("Total Invested: $%.2f%n", totalInvested);
        System.out.printf("Current Portfolio Value: $%.2f%n", currentValue);
        System.out.printf("Total Earnings: $%.2f%n", totalSale);
        System.out.printf("Total Gain/Loss: $%.2f%n", totalGainLoss);
    }

    // Count different stocks
    int getTotalOwnedStocks() {

        int i = 0;

        for (Stock s : stocks) {
            i++;
        }

        return i;
    }

    // Count total shares
    int getTotalOwnedShares() {

        int i = 0;

        for (Stock s : stocks) {
            i += s.getShares();
        }

        return i;
    }

    // Save transaction history
    void logTransaction(String type, String symbol, BigDecimal price, int shares) {

        try (FileWriter writer = new FileWriter("PortfolioStats.csv", true)) {

            writer.write(type + "," + symbol + "," + price + "," + shares + "\n");

        } catch (IOException e) {

            System.out.println("Error saving Portfolio Statistics: " + e.getMessage());
        }
    }

    // Add stock
    void addStock(String symbol, BigDecimal price, int shares) {

        loadStockPrices();

        if (isValidSymbol(symbol)) {

            stocks.add(new Stock(symbol, price, shares));

            logTransaction("BUY", symbol, price, shares);

        } else {

            System.out.println("That is not a Valid Stock!");
        }
    }

    // Check stock symbol
    boolean isValidSymbol(String symbol) {

        for (StockPrice sp : stockPrices) {

            if (sp.getSymbol().equalsIgnoreCase(symbol)) {
                return true;
            }
        }

        return false;
    }

    // Sell stock
    void sellStock(String symbol, int shares) {

        loadPortfolioFromFile();
        loadStockPrices();

        boolean found = false;

        for (Stock stock : stocks) {

            if (stock.getSymbol().equalsIgnoreCase(symbol)) {

                found = true;

                if (stock.getShares() >= shares) {

                    BigDecimal sellPrice = getPriceOf(symbol);

                    BigDecimal totalValue = sellPrice.multiply(
                            BigDecimal.valueOf(shares)
                    );

                    System.out.println(shares + " shares of "
                            + symbol + " sold for $" + totalValue);

                    System.out.println("You bought at $"
                            + stock.getPrice() + " per share.");

                    logTransaction("SELL", symbol, stock.getPrice(), shares);

                    int remainingShares = stock.getShares() - shares;

                    if (remainingShares == 0) {

                        stocks.remove(stock);

                    } else {

                        stock.setShares(remainingShares);
                    }

                    savePortfolioToFile();

                } else {

                    System.out.println("You don't have enough shares to sell.");
                }

                return;
            }
        }

        if (!found) {
            System.out.println("Stock not found in your portfolio.");
        }
    }

    // Load current market prices
    void loadStockPrices() {

        stockPrices.clear();

        try {

            Scanner fileScanner = new Scanner(new File("Stockprices.csv"));

            while (fileScanner.hasNextLine()) {

                String line = fileScanner.nextLine();
                String[] parts = line.split(" ");

                if (parts.length == 2) {

                    String symbol = parts[0];
                    BigDecimal price = new BigDecimal(parts[1]);

                    stockPrices.add(new StockPrice(symbol, price));
                }
            }

            fileScanner.close();

        } catch (FileNotFoundException e) {

            System.out.println("Stock price file not found!");
        }
    }

    // Expose owned stocks (used by the GUI to build its holdings table)
    ArrayList<Stock> getStocks() {
        return stocks;
    }

    // Expose the loaded market prices (used by the GUI to build its market table)
    ArrayList<StockPrice> getMarketPrices() {
        return stockPrices;
    }

    // Get current price of stock
    BigDecimal getPriceOf(String symbol) {

        for (StockPrice sp : stockPrices) {

            if (sp.getSymbol().equalsIgnoreCase(symbol)) {
                return sp.getPrice();
            }
        }

        return null;
    }

    // Save portfolio
    void savePortfolioToFile() {

        try {

            FileWriter writer = new FileWriter("Portfolio.csv");

            for (Stock stock : stocks) {

                writer.write(
                        stock.getSymbol() + ","
                                + stock.getPrice() + ","
                                + stock.getShares() + "\n"
                );
            }

            writer.close();

            System.out.println("Portfolio saved.");

        } catch (IOException e) {

            System.out.println("Error saving portfolio: " + e.getMessage());
        }
    }

    // Load portfolio
    void loadPortfolioFromFile() {

        stocks.clear();

        try {

            File file = new File("Portfolio.csv");

            if (!file.exists()) {
                return;
            }

            Scanner scanner = new Scanner(file);

            while (scanner.hasNextLine()) {

                String line = scanner.nextLine();
                String[] parts = line.split(",");

                if (parts.length == 3) {

                    String symbol = parts[0];
                    BigDecimal price = new BigDecimal(parts[1]);
                    int shares = Integer.parseInt(parts[2]);

                    stocks.add(new Stock(symbol, price, shares));
                }
            }

            scanner.close();

        } catch (Exception e) {

            System.out.println("Error loading portfolio: " + e.getMessage());
        }
    }
}