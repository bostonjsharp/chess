package server;
import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    private final Map<Integer, Set<ClientConnection>> connections = new ConcurrentHashMap<>();

    public void add(Integer gameID, String username, Session session) {
        connections.computeIfAbsent(gameID, id -> ConcurrentHashMap.newKeySet()).add(new ClientConnection(username, session));
    }

    public void remove(Integer gameID, Session session) {
        var gameConnections = connections.get(gameID);
        if(gameConnections != null){
            gameConnections.removeIf(connection -> connection.session().equals(session));
        }
    }

    public void broadcastExceptRoot(Integer gameID, Session rootSession, String message) throws IOException{
        var gameConnections = connections.get(gameID);
        if(gameConnections != null){
            for(var connection : gameConnections){
                if(connection.session().isOpen() && !connection.session().equals(rootSession)){
                    connection.session().getRemote().sendString(message);
                }
            }
        }
    }
}
