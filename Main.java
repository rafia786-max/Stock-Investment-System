import java.util.Scanner;
import java.math.BigDecimal;
import java.util.InputMismatchException;

public class Main{
    public static void main(String [] args){

        Scanner input = new Scanner(System.in);
        Portfolio portfolio = new Portfolio();
        boolean isRunning = true;
        portfolio.loadPortfolioFromFile();
        System.out.print("=====<WELCOME>=====");


        while(isRunning){
            try{
                System.out.printf("\n===================");
                System.out.printf("\n1. View Portfolio\n2. View Portfolio Statistics\n3. Add Stocks\n4. Sell Stocks\n5. Exit\nEnter Your Choice: ");
                int choice = input.nextInt();
                switch(choice){

                    case 1:           // View owned stocks
                        portfolio.viewPortfolio();
                        break;

                    case 2:           // View Stats
                        portfolio.viewStatistics();
                        break;

                    case 3:           // Add Stock
                        char option;
                        do{
                            input.nextLine();
                            System.out.print("Enter stock Symbol: ");
                            String symbol = input.nextLine().toUpperCase();
                            System.out.print("Enter stock Price: ");
                            BigDecimal price = new BigDecimal(input.nextLine());
                            System.out.print("Enter stock Shares: ");
                            int shares = input.nextInt();
                            portfolio.addStock(symbol,price,shares);
                            System.out.println("Would you like to make another entry? (N to Exit): ");
                            option = input.next().toLowerCase().charAt(0);

                        }while(option != 'n');
                        portfolio.savePortfolioToFile();
                        break;

                    case 4:             // Sell Stock
                        char option2;
                        do{
                            input.nextLine();
                            System.out.print("Enter stock Symbol: ");
                            String symbol = input.nextLine();
                            System.out.print("Enter number of shares to sell: ");
                            int shares = input.nextInt();
                            portfolio.sellStock(symbol, shares);
                            System.out.println("Would you like to make another entry? (N to Exit): ");
                            option2 = input.next().toLowerCase().charAt(0);

                        }while(option2 != 'n');
                        portfolio.savePortfolioToFile();
                        break;

                    case 5:             // Exit
                        System.out.println("Goodbye.");
                        System.out.println("===================");
                        isRunning = false;
                        break;

                    default:
                        System.out.println("Invalid input!");

                }   // Switch End

            }   // Try Block End

            catch(InputMismatchException e){
                System.out.printf("Invalid Input! Please Try Again.");
                input.nextLine();
            }
            catch(NumberFormatException e){
                System.out.printf("Invalid Input! Please Try Again.");
                input.nextLine();
            }

        }

        input.close();
    }

}