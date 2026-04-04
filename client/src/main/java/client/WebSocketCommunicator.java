package client;

import com.google.gson.Gson;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.MessageHandler;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;
import java.io.IOException;
import java.net.URI;

public class WebSocketCommunicator extends Endpoint {
    private final Gson gson = new Gson();
    private final ServerMessageObserver observer;
    private Session session;

    public WebSocketCommunicator(String url, ServerMessageObserver observer) throws Exception{
        this.observer = observer;
        URI uri = new URI(url.replace("http", "ws") + "/ws");
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        this.session = container.connectToServer(this, uri);
        this.session.addMessageHandler(new MessageHandler.Whole<String>() {
            @Override
            public void onMessage(String message) {
                ServerMessage serverMessage = gson.fromJson(message, ServerMessage.class);
                observer.notify(serverMessage,message);
            }
        });
    }

    public void onOpen(Session session, EndpointConfig endpointConfig){
        this.session = session;
    }

    public void sendCommand(UserGameCommand command) throws IOException{
        session.getBasicRemote().sendText(gson.toJson(command));
    }

    public void close() throws IOException{
        if(session != null && session.isOpen()){
            session.close();
        }
    }
}
