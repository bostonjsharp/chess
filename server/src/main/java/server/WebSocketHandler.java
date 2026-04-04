package server;

import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.GameData;
import model.AuthData;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsMessageContext;
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
            if(command.getCommandType() == UserGameCommand.CommandType.CONNECT){
                connect(context, command);
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
