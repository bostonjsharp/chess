package server;
import org.eclipse.jetty.websocket.api.Session;

public record ClientConnection(String username, Session session) {
}
