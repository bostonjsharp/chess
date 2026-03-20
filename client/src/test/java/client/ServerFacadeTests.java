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
        facade.register("bost", "bost1", "test@gmail.com");
        assertThrows(Exception.class, () -> facade.register("bost", "bost1", "test@gmail.com"));
    }



}
