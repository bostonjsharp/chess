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
            AuthData authData = authDAO.getAuth(command.getAuthToken());
            if(authData == null){
                sendError(context, "invalid auth token");
                return;
            }
            GameData gameData = gameDAO.getGame(command.getGameID());
            if(gameData == null) {
                sendError(context,"Game Not Found...");
                return;
            }

            String username = authData.username();
            String role = getRole(gameData,username);
            connectionManager.add(command.getGameID(), username, context.session);
            LoadGameMessage loadGameMessage = new LoadGameMessage(gameData.game());
            context.send(gson.toJson(loadGameMessage));
            String notification;
            String white = gameData.whiteUsername();
            String black = gameData.blackUsername();

            if(role.equals("WHITE")){
                white = null;
                notification = username + " joined the game as White";
            } else if (role.equals("BLACK")){
                black = null;
                notification = username + " joined the game as Black";
            } else {
                notification = username + " joined the game as an observer";
            }
            if (role.equals("WHITE") || role.equals("BLACK")){
                GameData newGame = new GameData(gameData.gameID(), white, black, gameData.gameName(), gameData.game());
                gameDAO.updateGame(newGame);
            }
            NotificationMessage notificationMessage = new NotificationMessage(notification);
            connectionManager.broadcastExceptRoot(command.getGameID(), context.session, gson.toJson(notificationMessage));
        } catch (Exception e) {
            sendError(context, e.getMessage());
        }
    }

    private void leave(WsMessageContext context, UserGameCommand command){
        try{
            AuthData authData = authDAO.getAuth(command.getAuthToken());
            if(authData == null){
                sendError(context, "invalid auth token");
                return;
            }
            GameData gameData = gameDAO.getGame(command.getGameID());
            if(gameData == null){
                sendError(context, "game not found");
                return;
            }
            String username = authData.username();
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
            AuthData authData = authDAO.getAuth(command.getAuthToken());
            if(authData == null){
                sendError(context, "invalid auth token");
                return;
            }
            GameData gameData = gameDAO.getGame(command.getGameID());
            if(gameData == null){
                sendError(context, "game not found");
                return;
            }
            String username = authData.username();
            String role = getRole(gameData, username);
            ChessGame game = gameData.game();

            if(role.equals("OBSERVER")){
                sendError(context, "Observers can't make moves");
                return;
            }
            if(role.equals("WHITE") && game.getTeamTurn() != ChessGame.TeamColor.WHITE){
                sendError(context ,"Not your turn");
                return;
            }
            if(role.equals("BLACK") && game.getTeamTurn() != ChessGame.TeamColor.BLACK){
                sendError(context, "not your turn");
                return;
            }
            game.makeMove(command.getMove());
            GameData newGame = new GameData(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), game);
            gameDAO.updateGame(newGame);
            LoadGameMessage loadGameMessage = new LoadGameMessage(game);
            connectionManager.broadcast(command.getGameID(), gson.toJson(loadGameMessage));
            String notification;
            if(role.equals("WHITE")){
                notification = username + " made a move as White!";
            } else {
                notification = username + " made a move as Black!";
            }

            NotificationMessage notificationMessage = new NotificationMessage(notification);
            connectionManager.broadcastExceptRoot(command.getGameID(), context.session, gson.toJson(notificationMessage));
            
        } catch (Exception e) {
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
}
