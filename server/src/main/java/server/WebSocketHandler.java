package server;

import chess.ChessGame;
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
            ChessGame.TeamColor playerColor = null;
            if(username.equals(gameData.whiteUsername())){
                playerColor = ChessGame.TeamColor.WHITE;
            } else if (username.equals(gameData.blackUsername())){
                playerColor = ChessGame.TeamColor.BLACK;
            }
            if (playerColor == null) {
                sendError(context, "Observers can't make moves...");
                return;
            }
            if(game.getTeamTurn() != playerColor) {
                sendError(context, "Not your turn :(");
                return;
            }

            game.makeMove(command.getMove());
            GameData newGame = new GameData(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), game);
            gameDAO.updateGame(newGame);
            LoadGameMessage loadGameMessage = new LoadGameMessage(game);
            connectionManager.broadcast(command.getGameID(), gson.toJson(loadGameMessage));
            String notification = username +" made a move";
            NotificationMessage notificationMessage = new NotificationMessage(notification);
            connectionManager.broadcastExceptRoot(command.getGameID(), context.session, gson.toJson(notificationMessage));

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

            ChessGame.TeamColor playerColor = null;
            if (username.equals((gameData.whiteUsername()))){
                playerColor = ChessGame.TeamColor.WHITE;
            } else if (username.equals(gameData.blackUsername())){
                playerColor = ChessGame.TeamColor.BLACK;
            }

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
