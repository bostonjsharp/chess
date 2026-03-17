package client;

import java.util.Scanner;

public class ChessClient {

    private boolean running = true;

    public void run() {
        System.out.println("""
                ♔ Welcome to 240 Chess ♚
                """);
        printMenu();
        Scanner scanner = new Scanner(System.in);
        while(running) {
            System.out.print(">>>");
            String input = scanner.nextLine();
            String result = eval(input);
            System.out.println(result);
        }
    }

    public String eval(String input){
        String lower = input.trim().toLowerCase();

        return switch(lower){
            case "register" -> "";
            case "login" -> "";
            case "help" -> help();
            case "quit" -> quit();
            default -> "Unknown command, type help if needed.";
        };
    }

    private void printMenu() {
        System.out.println("""
                register  (Create Account)
                login     (Sign In)
                help      (Get Help)
                quit      (Exit)
                """);
    }

    private String help() {
        return """
                Type out the word associated with the command you would like to do!
                """;
    }

    private String quit() {
        running = false;
        return "Have a nice day!";
    }
}
