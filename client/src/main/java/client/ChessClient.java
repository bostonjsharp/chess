package client;

import chess.ChessGame;
import model.GameData;
import com.google.gson.Gson;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.ServerMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ChessClient implements ServerMessageObserver{

    private boolean running = true;
    private boolean inGame = false;
    private final Scanner scanner = new Scanner(System.in);
    private final ServerFacade server = new ServerFacade("http://localhost:8080");
    private String authToken = null;
    private String username = null;
    private List<GameData> listedGames = new ArrayList<>();
    private WebSocketCommunicator webSocketCommunicator;
    private ChessGame currentGame = null;
    private Integer currentGameID = null;
    private ChessGame.TeamColor currentPlayerColor = null;
    private final Gson gson = new Gson();

    public void run() {
        System.out.println("""
                ♔ Welcome to 240 Chess ♚
                """);
        while(running) {
            if (!inGame){
                printMenu();
            } else {
                printGameMenu();
            }
            System.out.print(">>> ");
            String input = scanner.nextLine();
            String result = eval(input);
            System.out.println(result);
            System.out.println();
        }
    }

    public String eval(String input){
        String lower = input.trim().toLowerCase();


        if (inGame) {
            return switch (lower){
                case "leave" -> leaveGame();
                case "help" -> gameHelp();
                default -> "Unknown command, type help if needed.";
            };
        }
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
                case "create" -> createGame();
                case "list" -> listGames();
                case "join" -> joinGame();
                case "observe" -> observeGame();
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
            return e.getMessage();
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
            return e.getMessage();
        }
    }

    private String logout() {
        try{
            server.logout(authToken);
            authToken = null;
            username = null;
            return "Logged out. Goodbye!";
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private String createGame() {
        try{
            String gameName = prompt("game name");
            int gameID = server.createGame(gameName, authToken);
            listedGames = new ArrayList<>(server.listGames(authToken).games());
            return "Game created!";
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private String listGames(){
        try {
            var result = server.listGames(authToken);
            listedGames = new ArrayList<>(result.games());
            if (listedGames.isEmpty()){
                return "No games found!";
            }
            StringBuilder out = new StringBuilder();
            for (int i = 0; i < listedGames.size(); i++){
                GameData game = listedGames.get(i);
                out.append(i +1)
                    .append(". ")
                    .append(game.gameName())
                        .append(" > White: ")
                        .append(game.whiteUsername() == null ? "-" : game.whiteUsername())
                        .append(" > Black: ")
                        .append(game.blackUsername() == null ? "-" : game.blackUsername())
                        .append("\n");
            }
            return out.toString();
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private String joinGame() {
        try{
            if (listedGames.isEmpty()) {
                return "No games listed :(";
            }
            int gameNumber = Integer.parseInt(prompt("game number"));
            String colorChoice = prompt("White or Black?").toUpperCase();

            if(gameNumber < 1 || gameNumber > listedGames.size()) {
                return "Invalid game number...";
            }
            GameData chosenGame = listedGames.get(gameNumber - 1);
            ChessGame.TeamColor color = ChessGame.TeamColor.valueOf(colorChoice);
            server.joinGame(chosenGame.gameID(), colorChoice, authToken);
            BoardPrinter printer = new BoardPrinter();
            printer.drawBoard(chosenGame.game(), color);
            inGame = true;
            return "Joined game " + chosenGame.gameName() + " as " + colorChoice + "!";
        } catch (IllegalArgumentException e) {
            return "Invalid entry... Please try again!";
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private String observeGame() {
        try {
            if (listedGames.isEmpty()) {
                return "No games listed...";
            }
            int gameNumber = Integer.parseInt(prompt("game number"));
            if (gameNumber < 1 || gameNumber > listedGames.size()) {
                return "Invalid game number... Try again :(";
            }

            GameData chosenGame = listedGames.get(gameNumber - 1);
            BoardPrinter printer = new BoardPrinter();
            printer.drawBoard(chosenGame.game(), ChessGame.TeamColor.WHITE);
            inGame = true;
            return "Observing game " + chosenGame.gameName() + "!";
        } catch (NumberFormatException e) {
            return "Invalid game number...  :(";
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private String leaveGame() {
        inGame = false;
        return "Left the game.";
    }

    public void notify(ServerMessage message, String messageText){
        switch (message.getServerMessageType()) {
            case LOAD_GAME -> {
                LoadGameMessage loadGameMessage = gson.fromJson(messageText, LoadGameMessage.class);
                currentGame = loadGameMessage.getGame();
                drawCurrentBoard();
            }
            case NOTIFICATION -> {
                NotificationMessage notificationMessage = gson.fromJson(messageText, NotificationMessage.class);
                System.out.println(notificationMessage.getMessage());
            }
            case ERROR -> {
                ErrorMessage errorMessage = gson.fromJson(messageText, ErrorMessage.class);
                System.out.println(errorMessage.getErrorMessage());
            }
        }
    }

    private void drawCurrentBoard(){
        if(currentGame == null){
            return;
        }
        BoardPrinter printer = new BoardPrinter();

        if(currentPlayerColor == ChessGame.TeamColor.BLACK){
            printer.drawBoard(currentGame, ChessGame.TeamColor.BLACK);
        } else{
            printer.drawBoard(currentGame, ChessGame.TeamColor.WHITE);
        }
    }

    private void connectGame(int gameID, ChessGame.TeamColor color) throws Exception{
        currentGameID = gameID;
        currentPlayerColor = color;
        webSocketCommunicator = new WebSocketCommunicator("http://localhost:8080", this);
        UserGameCommand connectCommand = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID);
        webSocketCommunicator.sendCommand(connectCommand);
    }

    private String gameHelp() {
        return """
                leave  -leave the current game
                help   -show this help message
                """;
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
                    create    (Create Game)
                    list      (List Games)
                    join      (Join Game)
                    observe   (Observe Game)
                    logout    (Sign Out)
                    help      (Get Help)
                    quit      (Exit)
                    """);

        }
    }

    private void printGameMenu() {
        System.out.println("""
                leave     (Leave Game)
                help      (Get Help)
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
