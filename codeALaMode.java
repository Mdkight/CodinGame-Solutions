import java.util.*;
import java.io.*;
import java.math.*;

/**
 * My solution here was to store the contents of the kitchen in an array of character arrays
 * this maintains the x, y position of everything in the kitchen. the original solution used 
 * the first letter of the objects name to track what items to retrieve, however I soon had 
 * to change this as the problem became more complex outside the wood 1 arena.
 **/
class Player {

    public static void main(String args[]) {
        char[][] kitchenContents = new char[7][11];
        Order currentOrder = null;



        Scanner in = new Scanner(System.in);
        int numAllCustomers = in.nextInt();
        for (int i = 0; i < numAllCustomers; i++) {
            String customerItem = in.next(); // the food the customer is waiting for
            int customerAward = in.nextInt(); // the number of points awarded for delivering the food
        }
        in.nextLine();
        for (int i = 0; i < 7; i++) {
            String kitchenLine = in.nextLine();
            //store the kitchen contents in array
            kitchenContents[i] = kitchenLine.toCharArray();
            
        }

        // game loop
        while (true) {
            Order[] orders = new Order[3];
            int lowestAward = Integer.MAX_VALUE;

            int turnsRemaining = in.nextInt();
            int playerX = in.nextInt();
            int playerY = in.nextInt();
            String playerItem = in.next();
            // System.err.println(playerItem);
            char[] playerItemInitials =itemInitials(playerItem);
            if(playerItemInitials[0] == 'N'){
                currentOrder = null;
                lowestAward = Integer.MAX_VALUE;
            }
            int partnerX = in.nextInt();
            int partnerY = in.nextInt();
            String partnerItem = in.next();
            int numTablesWithItems = in.nextInt(); // the number of tables in the kitchen that currently hold an item
            for (int i = 0; i < numTablesWithItems; i++) {
                int tableX = in.nextInt();
                int tableY = in.nextInt();
                String item = in.next();
                //update the kitchen contents with these items
                kitchenContents[tableX][tableY] = item.charAt(0);
            }
            String ovenContents = in.next(); // ignore until wood 1 league
            int ovenTimer = in.nextInt();
            int numCustomers = in.nextInt(); // the number of customers currently waiting for food
            // System.err.println("current customer number is: " + numCustomers);
            for (int i = 0; i < numCustomers; i++) {
                String customerItem = in.next();
                int customerAward = in.nextInt();
                orders[i] = new Order(customerItem, customerAward);
                // System.err.println("order is: " + orders[i].toString());


                if(customerAward < lowestAward && playerItemInitials[0] == 'N'){
                    currentOrder = orders[i];
                    lowestAward = orders[i].getPrice();
                    // System.err.println("current Order is: " + currentOrder.toString());
                }
            }

            if(currentOrder != null){
                char[] currentOrderInitials = itemInitials(currentOrder.getContents());
                char nextItem = identifyNextItem(playerItemInitials, currentOrderInitials);
                String command = nextCommand(nextItem, kitchenContents);
                System.out.println(command);
            } else {
                System.out.println("WAIT");
            }

            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");


            // MOVE x y
            // USE x y
            // WAIT

        }
    }

    //gets first letter of each item in the string. items split by - 
    public static char[] itemInitials(String items){
        String[] itemsArray = items.split("-");
        char[] itemInitials = new char[itemsArray.length];
        for(int i = 0; i < itemsArray.length; i++){
            itemInitials[i] = itemsArray[i].charAt(0);
        }
        return itemInitials;

    }

    public static char identifyNextItem(char[] handContents, char[] orderContents){
        if(handContents[0] == 'N'){
            return orderContents[0];
        }
        if(handContents.length < orderContents.length){
            return orderContents[handContents.length];
        } else {
            return 'W';
        }

    }

    public static String nextCommand(char itemInitial, char[][] kitchenContents){
        // System.err.println("current itemInitial is: " + itemInitial);
        for(int i = 0; i < 7; i++){
            for(int j = 0; j < 11; j++){
                if(kitchenContents[i][j] == itemInitial){
                    return "USE " + j + " " + i;
                }
            }
        }
        return "WAIT";

    }
}

class Order {
    private String contents;
    private int price;

    public Order(String contents, int price) {
        this.contents = contents;
        this.price = price;
    }

    public String getContents() { return contents; }
    public int getPrice() { return price; }
    
    @Override
    public String toString() {
        return "Contents: " + contents + ", Price: $" + price;
    }
}