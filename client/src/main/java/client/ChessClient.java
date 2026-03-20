package client;

import java.util.Scanner;

public class ChessClient {

    private boolean running = true;
    private final Scanner scanner = new Scanner(System.in);
    private final ServerFacade server = new ServerFacade("http://localhost:8080");
    private String authToken = null;
    private String username = null;

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

        if (authToken == null) {
            return switch (lower) {
                case "register" -> register();
                case "login" -> login();
                case "help" -> help();
                case "quit" -> quit();
                default -> "Unknown command, type help if needed.";
            };
        } else {
            return switch (lower) {
                case "logout" -> logout();
                case "help" -> help();
                case "quit" -> quit();
                default -> "Unknown command, type help if needed.";
            };
        }
    }

    private String prompt(String output) {
        System.out.print(output + ": ");
        return scanner.nextLine();
    }

    private String register(){
        try {
            String username = prompt("username");
            String password = prompt("password");
            String email = prompt("email");
            var authData = server.register(username, password, email);
            this.authToken = authData.authToken();
            this.username = authData.username();
            return "Registered and logged in as " + authData.username();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private String login() {
        try {
            String username = prompt("username");
            String password = prompt("password");
            var authData = server.login(username, password);
            this.authToken = authData.authToken();
            this.username = authData.username();
            return "Logged in as " + authData.username();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private String logout() {
        try{
            server.logout(authToken);
            authToken = null;
            username = null;
            return "Logged out. Goodbye!";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private void printMenu() {
        if (authToken == null) {
            System.out.println("""
                    register  (Create Account)
                    login     (Sign In)
                    help      (Get Help)
                    quit      (Exit)
                    """);
        } else {
            System.out.println("""
                    logout    (Sign Out)
                    help      (Get Help)
                    quit      (Exit)
                    """);

        }
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
