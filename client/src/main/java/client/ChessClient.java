package client;

import java.util.Scanner;

public class ChessClient {

    private boolean running = true;
    private final Scanner scanner = new Scanner(System.in);

    public void run() {
        System.out.println("""
                ♔ Welcome to 240 Chess ♚
                """);
        printMenu();
        while(running) {
            System.out.print(">>> ");
            String input = scanner.nextLine();
            String result = eval(input);
            System.out.println(result);
        }
    }

    public String eval(String input){
        String lower = input.trim().toLowerCase();

        return switch(lower){
            case "register" -> register();
            case "login" -> login();
            case "help" -> help();
            case "quit" -> quit();
            default -> "Unknown command, type help if needed.";
        };
    }

    private String prompt(String output) {
        System.out.print(output + ": ");
        return scanner.nextLine();
    }

    private String register(){
        String username = prompt("username");
        String password = prompt("password");
        String email = prompt("email");

        return "to be implemented";
    }

    private String login() {
        String username = prompt("username");
        String password = prompt("password");

        return "to be implemented";
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
