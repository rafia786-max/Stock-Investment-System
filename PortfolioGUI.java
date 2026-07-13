import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;


public class PortfolioGUI extends JFrame {

    // Core logic object - reused as-is, no changes to its rules
    private final Portfolio portfolio = new Portfolio();

    // Tables and their models
    private final DefaultTableModel marketModel =
            new DefaultTableModel(new String[]{"Symbol", "Price"}, 0);
    private final DefaultTableModel holdingsModel =
            new DefaultTableModel(
                    new String[]{"Symbol", "Shares", "Buy Price", "Current Price", "Market Value", "Gain/Loss"}, 0);

    private final JTable marketTable = new JTable(marketModel);
    private final JTable holdingsTable = new JTable(holdingsModel);

    // Summary labels at the bottom of the window
    private final JLabel investedLabel = new JLabel();
    private final JLabel currentValueLabel = new JLabel();
    private final JLabel gainLossLabel = new JLabel();

    // A soft, "chic" color palette
    private static final Color BG_COLOR = new Color(247, 247, 250);
    private static final Color ACCENT_COLOR = new Color(64, 90, 220);
    private static final Color GREEN = new Color(30, 140, 70);
    private static final Color RED = new Color(190, 40, 40);

    public PortfolioGUI() {
        super("Stock Portfolio Tracker");

        // Make tables non-editable and easier to read
        makeTableReadOnly(marketTable);
        makeTableReadOnly(holdingsTable);
        holdingsTable.getColumnModel().getColumn(5)
                .setCellRenderer(new GainLossRenderer());
        marketTable.setAutoCreateRowSorter(true);
        holdingsTable.setAutoCreateRowSorter(true);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(820, 560);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_COLOR);
        setLayout(new BorderLayout(10, 10));

        add(buildTitlePanel(), BorderLayout.NORTH);
        add(buildTabs(), BorderLayout.CENTER);
        add(buildSummaryPanel(), BorderLayout.SOUTH);

        refreshAll();
    }

    // ---------- UI BUILDING ----------

    private JPanel buildTitlePanel() {
        JPanel panel = new JPanel();
        panel.setBackground(BG_COLOR);
        JLabel title = new JLabel("Stock Portfolio Tracker");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(ACCENT_COLOR);
        panel.add(title);
        return panel;
    }

    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Market", buildMarketPanel());
        tabs.addTab("My Portfolio", buildHoldingsPanel());
        return tabs;
    }

    private JPanel buildMarketPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JScrollPane(marketTable), BorderLayout.CENTER);

        JButton buyButton = new JButton("Buy Selected Stock");
        styleButton(buyButton, ACCENT_COLOR);
        buyButton.addActionListener(e -> buyDialog());

        JPanel bottom = new JPanel();
        bottom.setBackground(BG_COLOR);
        bottom.add(buyButton);
        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buildHoldingsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JScrollPane(holdingsTable), BorderLayout.CENTER);

        JButton sellButton = new JButton("Sell Selected Stock");
        styleButton(sellButton, RED);
        sellButton.addActionListener(e -> sellDialog());

        JButton refreshButton = new JButton("Refresh");
        styleButton(refreshButton, ACCENT_COLOR);
        refreshButton.addActionListener(e -> refreshAll());

        JPanel bottom = new JPanel();
        bottom.setBackground(BG_COLOR);
        bottom.add(sellButton);
        bottom.add(refreshButton);
        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buildSummaryPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 20, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 15, 20));
        panel.setBackground(BG_COLOR);

        Font labelFont = new Font("SansSerif", Font.BOLD, 14);
        investedLabel.setFont(labelFont);
        currentValueLabel.setFont(labelFont);
        gainLossLabel.setFont(labelFont);

        panel.add(investedLabel);
        panel.add(currentValueLabel);
        panel.add(gainLossLabel);
        return panel;
    }

    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
    }

    private void makeTableReadOnly(JTable table) {
        table.setDefaultEditor(Object.class, null);
        table.setRowHeight(24);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
    }

    // ---------- DATA REFRESH ----------

    private void refreshAll() {
        refreshMarketTable();
        refreshHoldingsTable();
        refreshSummary();
    }

    private void refreshMarketTable() {
        portfolio.loadStockPrices();
        marketModel.setRowCount(0);

        for (StockPrice sp : portfolio.getMarketPrices()) {
            marketModel.addRow(new Object[]{
                    sp.getSymbol(),
                    "$" + sp.getPrice()
            });
        }
    }

    private void refreshHoldingsTable() {
        portfolio.loadPortfolioFromFile();
        portfolio.loadStockPrices();
        holdingsModel.setRowCount(0);

        for (Stock stock : portfolio.getStocks()) {
            BigDecimal currentPrice = portfolio.getPriceOf(stock.getSymbol());

            // If a held symbol is missing from Stockprices.csv, skip pricing it
            // instead of crashing - shows "N/A" so the row still appears.
            if (currentPrice == null) {
                holdingsModel.addRow(new Object[]{
                        stock.getSymbol(),
                        stock.getShares(),
                        "$" + stock.getPrice(),
                        "N/A",
                        "N/A",
                        "N/A"
                });
                continue;
            }

            BigDecimal marketValue = currentPrice.multiply(BigDecimal.valueOf(stock.getShares()));
            BigDecimal costBasis = stock.getPrice().multiply(BigDecimal.valueOf(stock.getShares()));
            BigDecimal gainLoss = marketValue.subtract(costBasis);

            holdingsModel.addRow(new Object[]{
                    stock.getSymbol(),
                    stock.getShares(),
                    "$" + stock.getPrice(),
                    "$" + currentPrice,
                    "$" + round(marketValue),
                    formatMoney(gainLoss)
            });
        }
    }

    private void refreshSummary() {
        BigDecimal totalInvested = BigDecimal.ZERO;
        BigDecimal currentValue = BigDecimal.ZERO;

        for (Stock stock : portfolio.getStocks()) {
            BigDecimal currentPrice = portfolio.getPriceOf(stock.getSymbol());
            if (currentPrice == null) {
                continue;
            }
            totalInvested = totalInvested.add(stock.getPrice().multiply(BigDecimal.valueOf(stock.getShares())));
            currentValue = currentValue.add(currentPrice.multiply(BigDecimal.valueOf(stock.getShares())));
        }

        BigDecimal gainLoss = currentValue.subtract(totalInvested);

        investedLabel.setText("Total Invested: $" + round(totalInvested));
        currentValueLabel.setText("Current Value: $" + round(currentValue));
        gainLossLabel.setText("Gain/Loss: " + formatMoney(gainLoss));
        gainLossLabel.setForeground(gainLoss.signum() < 0 ? RED : GREEN);
    }

    // ---------- BUY / SELL DIALOGS ----------

    private void buyDialog() {
        int row = marketTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a stock from the Market list first.");
            return;
        }

        // Convert the view row to the model row in case the table is sorted
        int modelRow = marketTable.convertRowIndexToModel(row);
        String symbol = (String) marketModel.getValueAt(modelRow, 0);
        String priceText = ((String) marketModel.getValueAt(modelRow, 1)).replace("$", "");
        BigDecimal price = new BigDecimal(priceText);

        String input = JOptionPane.showInputDialog(this,
                "How many shares of " + symbol + " at $" + price + " would you like to buy?");
        if (input == null || input.trim().isEmpty()) {
            return; // user cancelled
        }

        try {
            int shares = Integer.parseInt(input.trim());
            if (shares <= 0) {
                JOptionPane.showMessageDialog(this, "Enter a positive number of shares.");
                return;
            }

            portfolio.addStock(symbol, price, shares);
            portfolio.savePortfolioToFile();
            refreshAll();
            JOptionPane.showMessageDialog(this, "Bought " + shares + " shares of " + symbol + ".");

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a whole number of shares.");
        }
    }

    private void sellDialog() {
        int row = holdingsTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a stock from My Portfolio first.");
            return;
        }

        int modelRow = holdingsTable.convertRowIndexToModel(row);
        String symbol = (String) holdingsModel.getValueAt(modelRow, 0);

        String input = JOptionPane.showInputDialog(this,
                "How many shares of " + symbol + " would you like to sell?");
        if (input == null || input.trim().isEmpty()) {
            return; // user cancelled
        }

        try {
            int shares = Integer.parseInt(input.trim());
            if (shares <= 0) {
                JOptionPane.showMessageDialog(this, "Enter a positive number of shares.");
                return;
            }

            portfolio.sellStock(symbol, shares);
            refreshAll();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a whole number of shares.");
        }
    }

    // ---------- FORMATTING HELPERS ----------

    private BigDecimal round(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private String formatMoney(BigDecimal value) {
        BigDecimal rounded = round(value);
        String sign = rounded.signum() > 0 ? "+" : "";
        return sign + "$" + rounded;
    }

    // Colors the Gain/Loss column green for profit, red for loss
    private static class GainLossRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String text = String.valueOf(value);
            if (text.startsWith("-")) {
                cell.setForeground(RED);
            } else if (text.startsWith("+")) {
                cell.setForeground(GREEN);
            } else {
                cell.setForeground(Color.DARK_GRAY);
            }
            return cell;
        }
    }

    // ---------- ENTRY POINT ----------

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // fall back to default look and feel if the system one isn't available
        }
        SwingUtilities.invokeLater(() -> new PortfolioGUI().setVisible(true));
    }
}
