package server;

import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.GameData;
import model.AuthData;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsMessageContext;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

import java.io.IOException;

public class WebSocketHandler {
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;
    private final ConnectionManager connectionManager;
    private final Gson gson = new Gson();
    private record CommandContext(AuthData authData, GameData gameData, String username, ChessGame game){}

    public WebSocketHandler(AuthDAO authDAO, GameDAO gameDAO, ConnectionManager connectionManager){
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
        this.connectionManager = connectionManager;
    }

    public void onMessage(WsMessageContext context){
        try{
            UserGameCommand command = gson.fromJson(context.message(), UserGameCommand.class);
            switch (command.getCommandType()){
                case CONNECT -> connect(context, command);
                case LEAVE -> leave(context, command);
                case MAKE_MOVE -> {
                    MakeMoveCommand moveCommand = gson.fromJson(context.message(), MakeMoveCommand.class);
                    makeMove(context, moveCommand);
                }
                case RESIGN -> resign(context, command);
            }
        } catch (Exception e) {
            sendError(context, e.getMessage());
        }
    }

    public void onClose(WsCloseContext context){
        System.out.println("Websocket Closed!");
    }
    private void connect(WsMessageContext context, UserGameCommand command){
        try{
            CommandContext commandContext = getCommandContext(context, command);
            if (commandContext == null){
                return;
            }
            GameData gameData = commandContext.gameData();
            String username = commandContext.username();
            String role = getRole(gameData,username);
            connectionManager.add(command.getGameID(), username, context.session);
            LoadGameMessage loadGameMessage = new LoadGameMessage(gameData.game());
            context.send(gson.toJson(loadGameMessage));
            String notification;

            if(role.equals("WHITE")){
                notification = username + " joined the game as White";
            } else if (role.equals("BLACK")){
                notification = username + " joined the game as Black";
            } else {
                notification = username + " joined the game as an observer";
            }
            NotificationMessage notificationMessage = new NotificationMessage(notification);
            connectionManager.broadcastExceptRoot(command.getGameID(), context.session, gson.toJson(notificationMessage));
        } catch (Exception e) {
            sendError(context, e.getMessage());
        }
    }

    private void leave(WsMessageContext context, UserGameCommand command){
        try{
            CommandContext commandContext = getCommandContext(context, command);
            if (commandContext == null){
                return;
            }
            String username = commandContext.username();
            GameData gameData = commandContext.gameData();
            String role = getRole(gameData, username);
            connectionManager.remove(command.getGameID(), context.session);
            if(role.equals("WHITE") || role.equals("BLACK")){
                String white = role.equals("WHITE") ? null : gameData.whiteUsername();
                String black = role.equals("BLACK") ? null : gameData.blackUsername();
                gameDAO.updateGame(new GameData(gameData.gameID(), white, black, gameData.gameName(), gameData.game()));
            }
            NotificationMessage notificationMessage;
            if(role.equals("WHITE")){
                notificationMessage = new NotificationMessage(username + " has left the game as white");
            } else if (role.equals("BLACK")){
                notificationMessage = new NotificationMessage(username + " has left the game as black");
            } else {
                notificationMessage = new NotificationMessage(username + " stopped observing the game");
            }
            connectionManager.broadcastExceptRoot(command.getGameID(), context.session, gson.toJson(notificationMessage));
        } catch (Exception e){
            sendError(context, e.getMessage());
        }
    }

    private void makeMove(WsMessageContext context, MakeMoveCommand command){
        try{
            CommandContext commandContext = getCommandContext(context, command);
            if (commandContext == null){
                return;
            }
            String username = commandContext.username();
            GameData gameData = commandContext.gameData();
            ChessGame game = commandContext.game();
            if (game.isGameOver()) {
                sendError(context, "Game is over!");
                return;
            }
            ChessGame.TeamColor playerColor = getPlayerColor(gameData, username);
            if (playerColor == null) {
                sendError(context, "Observers can't make moves...");
                return;
            }
            if(game.getTeamTurn() != playerColor) {
                sendError(context, "Not your turn :(");
                return;
            }

            game.makeMove(command.getMove());
            ChessGame.TeamColor teamTurn = game.getTeamTurn();
            boolean ended = game.isInCheckmate(teamTurn) || game.isInStalemate(teamTurn);
            if (ended) {
                game.setGameOver(true);
            }
            GameData newGame = new GameData(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), game);
            gameDAO.updateGame(newGame);
            LoadGameMessage loadGameMessage = new LoadGameMessage(game);
            connectionManager.broadcast(command.getGameID(), gson.toJson(loadGameMessage));

            String moveText = username + " moved from " + toChessNotation(command.getMove().getStartPosition())
                     + " to " + toChessNotation(command.getMove().getEndPosition());
            if(command.getMove().getPromotionPiece() != null){
                moveText += " promoting to " + command.getMove().getPromotionPiece();
            }
            NotificationMessage notificationMessage = new NotificationMessage(moveText);
            connectionManager.broadcastExceptRoot(command.getGameID(), context.session, gson.toJson(notificationMessage));
            broadcastGameStatus(gameData, game, teamTurn);

        } catch (Exception e) {
            sendError(context, e.getMessage());
        }
    }

    private void resign(WsMessageContext context, UserGameCommand command){
        try{
            CommandContext commandContext = getCommandContext(context, command);
            if (commandContext == null){
                return;
            }
            GameData gameData = commandContext.gameData();
            String username = commandContext.username();
            ChessGame game = gameData.game();

            ChessGame.TeamColor playerColor = getPlayerColor(gameData, username);

            if(playerColor == null){
                sendError(context, "Observers can't resign...");
                return;
            }
            if (game.isGameOver()) {
                sendError(context, "Game is already over");
                return;
            }
            game.setGameOver(true);

            GameData gameUpdate = new GameData(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), game);
            gameDAO.updateGame(gameUpdate);
            String notification;
            if (playerColor == ChessGame.TeamColor.WHITE){
                notification = username + " resigned. Black wins!";
            } else {
                notification = username + " resigned. White wins!";
            }
            NotificationMessage notificationMessage = new NotificationMessage(notification);
            connectionManager.broadcast(command.getGameID(), gson.toJson(notificationMessage));
        } catch (Exception e){
            sendError(context, e.getMessage());
        }
    }

    private String getRole(GameData gameData, String username){
        if(username.equals(gameData.whiteUsername())){
            return "WHITE";
        }
        if (username.equals(gameData.blackUsername())){
            return "BLACK";
        }
        return "OBSERVER";
    }

    private void sendError(WsMessageContext context, String error){
        ErrorMessage errorMessage = new ErrorMessage(error);
        context.send(gson.toJson(errorMessage));
    }

    private ChessGame.TeamColor getPlayerColor(GameData gameData, String username){
        if (username.equals((gameData.whiteUsername()))){
            return ChessGame.TeamColor.WHITE;
        } else if (username.equals(gameData.blackUsername())){
            return ChessGame.TeamColor.BLACK;
        }
        return null;
    }

    private static String getUserForColor(GameData gameData, ChessGame.TeamColor color){
        return color == ChessGame.TeamColor.WHITE ? gameData.whiteUsername() : gameData.blackUsername();
    }

    private void broadcastGameStatus(GameData gameData, ChessGame game, ChessGame.TeamColor color) throws IOException {

        String name = getUserForColor(gameData, color);
        String message = null;
        if(game.isInCheckmate(color)){
            message = name + " is in checkmate";
        } else if (game.isInStalemate(color)){
            message = name + " is in stalemate";
        } else if (game.isInCheck(color)){
            message = name + " is in check";
        }
        if (message != null) {
            connectionManager.broadcast(gameData.gameID(), gson.toJson(new NotificationMessage(message)));
        }
    }

    private String toChessNotation(ChessPosition position){
        char col = (char) ('a' + position.getColumn() -1);
        int row = position.getRow();
        return "" + col + row;
    }

    private CommandContext getCommandContext(WsMessageContext context, UserGameCommand command) {
        AuthData authData = authDAO.getAuth(command.getAuthToken());
        if (authData == null) {
            sendError(context, "invalid auth token");
            return null;
        }

        GameData gameData = gameDAO.getGame(command.getGameID());
        if (gameData == null) {
            sendError(context, "game not found");
            return null;
        }

        String username = authData.username();
        ChessGame game = gameData.game();
        return new CommandContext(authData, gameData, username, game);
    }
}
