package server;

import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsMessageContext;
import websocket.commands.UserGameCommand;

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
            System.out.println("WebSocket message error: " + e.getMessage());
        }
    }

    public void onClose(WsCloseContext context){
        System.out.println("Websocket Closed!");
    }
    private void connect(WsMessageContext context, UserGameCommand command){
        System.out.println("CONNECT received");
    }
}
