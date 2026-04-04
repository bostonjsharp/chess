package client;

import com.google.gson.Gson;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

public class WebSocketCommunicator {
    private final Gson gson = new Gson();
    private final ServerMessageObserver observer;

    public WebSocketCommunicator(String url, ServerMessageObserver observer){
        this.observer = observer
    }
}
