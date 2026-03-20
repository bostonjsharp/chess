package client;

import model.AuthData;
import org.junit.jupiter.api.*;
import server.Server;
import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;


    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        facade = new ServerFacade("http://localhost:" + port);
        System.out.println("Started test HTTP server on " + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    void clearDatabase() throws Exception{
        facade.clear();
    }

    @Test
    public void registerSuccess() throws Exception {
        AuthData auth = facade.register("bost", "bost1", "test@gmail.com");

        assertNotNull(auth);
        assertNotNull(auth.authToken());
        assertEquals("bost", auth.username());
    }

    @Test
    public void registerFailure() throws Exception {
        facade.register("bost1", "bost1", "test@gmail.com");
        assertThrows(Exception.class, () -> facade.register("bost1", "bost1", "test@gmail.com"));
    }

    @Test
    public void loginSuccess() throws Exception {
        facade.register("bost2", "bost1", "test@gmail.com");
        AuthData auth = facade.login("bost2", "bost1");

        assertNotNull(auth);
        assertNotNull(auth.authToken());
        assertEquals("bost2", auth.username());
    }

    @Test
    public void loginFailure() throws Exception {
        facade.register("bost3", "bost1", "test@gmail.com");

        assertThrows(Exception.class, () -> facade.login("bost3", "bleh"));
    }

    @Test
    public void logoutSuccess() throws Exception {
        AuthData auth = facade.register("bost4", "pass", "test@gmail.com");

        assertDoesNotThrow(() -> facade.logout(auth.authToken()));
    }

    @Test
    public void logoutFailure() {
        assertThrows(Exception.class, () -> facade.logout("token-imposter-among-us"));
    }

    @Test
    public void listGamesSuccess() throws Exception{
        AuthData auth = facade.register("bost5", "pass", "gmail@test.com");
        var result = facade.listGames(auth.authToken());

        assertNotNull(result);
        assertNotNull(result.games());
    }

    @Test
    public void listGamesFailure() {
        assertThrows(Exception.class, () -> facade.listGames("mis-token"));
    }

    @Test
    public void createGameSuccess() throws Exception{
        AuthData auth = facade.register("bost6", "pass1", "test@test.com");
        int gameID = facade.createGame("fair game", auth.authToken());
        assertTrue(gameID > 0);
    }

    @Test
    public void createGameFailure() {
        assertThrows(Exception.class, () -> facade.createGame("big game", "not-token-well"));
    }


}
