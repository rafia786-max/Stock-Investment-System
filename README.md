# Stock-Investment-System
Features


Buy & sell stocks against a list of tracked market prices
View your portfolio — symbol, shares owned, buy price, current price, and per-stock gain/loss
Portfolio statistics — total invested, current value, and overall gain/loss
Transaction history — every buy/sell is logged to a CSV file
Two interfaces:

Main.java — a menu-driven console app
PortfolioGUI.java — a Swing GUI with a Market tab (browse prices, buy) and a My Portfolio tab (view holdings, sell), with color-coded gains/losses





Project Structure

├── Main.java           # Console entry point
├── PortfolioGUI.java   # Swing GUI entry point
├── Portfolio.java      # Core logic: buying, selling, stats, file I/O
├── Stock.java          # Represents a stock the user owns
├── StockPrice.java     # Represents a symbol's current market price
├── Stockprices.csv     # Available symbols and their prices
├── Portfolio.csv       # Saved holdings (generated at runtime)
└── PortfolioStats.csv  # Transaction log (generated at runtime)

How It Works


Stockprices.csv holds the list of valid symbols and their current price, in SYMBOL PRICE format (space-separated), one per line.
Portfolio.csv stores what you currently own, in SYMBOL,PRICE,SHARES format. It's read on startup and rewritten every time you buy or sell.
PortfolioStats.csv logs every transaction (BUY/SELL) for lifetime statistics, in TYPE,SYMBOL,PRICE,SHARES format.
Money values use BigDecimal throughout to avoid floating-point rounding errors.


Getting Started

Requirements


Java JDK 8 or later


Run the console version

bashjavac *.java
java Main

Run the GUI version

bashjavac *.java
java PortfolioGUI

Both versions read/write the same CSV files, so your portfolio stays in sync no matter which one you use.
