package server;
import org.eclipse.jetty.websocket.api.Session;

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
}
