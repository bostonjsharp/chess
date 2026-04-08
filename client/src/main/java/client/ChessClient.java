package client;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import model.GameData;
import com.google.gson.Gson;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.ServerMessage;
import websocket.commands.MakeMoveCommand;
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
            }
            System.out.print(">>> ");
            String input = scanner.nextLine();
            String result = eval(input);
            System.out.println(result);
            System.out.println();
        }
    }

    public String eval(String input){
        String trimmed = input.trim();
        if (trimmed.isEmpty()){
            return "Unknown command, please type help if needed!";
        }
        String[] parts = trimmed.split("\\s+");
        String command = parts[0].toLowerCase();


        if (inGame) {
            return switch (command){
                case "redraw" -> {
                    drawCurrentBoard();
                    yield "";
                }
                case "highlight" -> highlightMoves(parts);
                case "move" -> makeMove(parts);
                case "leave" -> leaveGame();
                case "resign" -> resignGame();
                case "help" -> gameHelp();
                default -> "Unknown command, type help if needed.";
            };
        }
        if (authToken == null) {
            return switch (command) {
                case "register" -> register();
                case "login" -> login();
                case "help" -> help();
                case "quit" -> quit();
                default -> "Unknown command, type help if needed.";
            };
        } else {
            return switch (command) {
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
            closeWebSocketIfOpen();
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
            connectGame(chosenGame.gameID(), color);
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
            connectGame(chosenGame.gameID(), null);
            inGame = true;
            return "Observing game " + chosenGame.gameName() + "!";
        } catch (NumberFormatException e) {
            return "Invalid game number...  :(";
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private String leaveGame() {
        try{
            if(webSocketCommunicator != null) {
                UserGameCommand leaveCommand = new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, currentGameID);
                webSocketCommunicator.sendCommand(leaveCommand);
            }
        } catch (Exception e) {
            return e.getMessage();
        }
        inGame = false;
        currentGame = null;
        currentPlayerColor = null;
        currentGameID = null;
        return "Left the game.";
    }

    public void notify(ServerMessage message, String messageText){
        if (currentGameID == null) {
            return;
        }
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

        printGameMenu();
    }

    private String makeMove(String[] parts){
        try{
            if(webSocketCommunicator == null || currentGameID == null){
                return "You're not currently in a game, sorry!";
            }
            if (parts.length < 3 || parts.length > 4){
                return "Must be in this form: move <start> <end> [promotion]";
            }

            ChessPosition start = parsePosition(parts[1]);
            ChessPosition end = parsePosition(parts[2]);
            ChessPiece.PieceType promotion = null;
            if (parts.length == 4){
                promotion = parsePromotionPiece(parts[3]);
            }

            ChessMove move = new ChessMove(start, end, promotion);
            MakeMoveCommand command = new MakeMoveCommand(authToken, currentGameID, move);
            webSocketCommunicator.sendCommand(command);
            return "";
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private String resignGame() {
        try{
            if (webSocketCommunicator == null || currentGameID == null){
                return "You aren't currently in a game...";
            }
            String confirmation = prompt("Are you sure you want to resign? This cannot be undone. (y/n)").trim().toLowerCase();
            if(!confirmation.equals("y")){
                return "Resignation cancelled";
            }
            UserGameCommand resignCommand = new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, currentGameID);
            webSocketCommunicator.sendCommand(resignCommand);
            return "Resignation sent!";
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private ChessPosition parsePosition(String input){
        if(input == null || input.length() != 2){
            throw new IllegalArgumentException("Invalid position... Please use a format like d5");
        }
        char cols = Character.toLowerCase(input.charAt(0));
        char rows = input.charAt(1);

        if(cols < 'a' || cols > 'h' || rows < '1' || rows > '8'){
            throw new IllegalArgumentException("Invalid position... Please use a format like d5");
        }
        int actualCol = cols - 'a' + 1;
        int actualRow = rows - '0';
        return new ChessPosition(actualRow, actualCol);
    }

    private ChessPiece.PieceType parsePromotionPiece(String input) {
        return switch (input.toLowerCase()){
            case "queen", "q" -> ChessPiece.PieceType.QUEEN;
            case "rook", "r" -> ChessPiece.PieceType.ROOK;
            case "bishop", "b" -> ChessPiece.PieceType.BISHOP;
            case "knight", "n" -> ChessPiece.PieceType.KNIGHT;
            default -> throw new IllegalArgumentException("Invalid promotion piece...");
        };
    }

    private String highlightMoves(String[] parts){
        try{
            if (currentGame == null){
                return "You are not currently in a game...";
            }
            if(parts.length != 2){
                return "Please use the format: highlight <position>";
            }
            ChessPosition position = parsePosition(parts[1]);
            ChessGame.TeamColor perspective = (currentPlayerColor == ChessGame.TeamColor.BLACK)
                    ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;
            var legalMoves = currentGame.validMoves(position);
            BoardPrinter printer = new BoardPrinter();
            printer.drawHighlights(currentGame, perspective, position, legalMoves);
            printGameMenu();
            return "";
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private String gameHelp() {
        return """
                move <start> <end> [promotion]  -make a move
                redraw                          -redraw the board
                highlight <position>            -highlight legal moves
                resign                          -resign the game
                leave                           -leave the current game
                help                            -show this help message
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
                move <start> <end> [promotion]      (Make a Move)
                redraw                              (Redraw Board)
                highlight <position>                (Highlight Legal Moves)
                resign                              (Resign Game)
                help                                (Get Help)
                leave                               (Leave Game)
                """);
    }

    private String help() {
        return """
                Type out the word associated with the command you would like to do!
                """;
    }

    private String quit() {
        try {
            closeWebSocketIfOpen();
        } catch (Exception ignored) {
        }
        running = false;
        return "Have a nice day!";
    }

    private void closeWebSocketIfOpen() throws Exception {
        if (webSocketCommunicator != null) {
            webSocketCommunicator.close();
            webSocketCommunicator = null;
        }
    }

    private void connectGame(int gameID, ChessGame.TeamColor color) throws Exception{
        closeWebSocketIfOpen();
        currentGameID = gameID;
        currentPlayerColor = color;
        webSocketCommunicator = new WebSocketCommunicator("http://localhost:8080", this);
        UserGameCommand connectCommand = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID);
        webSocketCommunicator.sendCommand(connectCommand);
    }
}
