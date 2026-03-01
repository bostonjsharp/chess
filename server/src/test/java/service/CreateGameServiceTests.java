package service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import model.AuthData;
import requests.CreateGameRequest;
import results.CreateGameResult;
import service.CreateGameService;
import service.ServiceException;

public class CreateGameServiceTests {

    @Test
    public void createGameSuccess() throws ServiceException {
        MemoryAuthDAO authDAO = new MemoryAuthDAO();
        MemoryGameDAO gameDAO = new MemoryGameDAO();

        authDAO.createAuth(new AuthData("token1", "bost"));
        CreateGameService service = new CreateGameService(authDAO, gameDAO);
        CreateGameResult result = service.createGame(new CreateGameRequest("token1", "Big Game"));

        assertTrue(result.gameID() > 0);
        assertNotNull(gameDAO.getGame(result.gameID()));
        assertEquals("Big Game", gameDAO.getGame(result.gameID()).gameName());
    }

    @Test
    public void  createGameUnauthorized(){
        MemoryAuthDAO authDAO = new MemoryAuthDAO();
        MemoryGameDAO gameDAO = new MemoryGameDAO();

        CreateGameService service = new CreateGameService(authDAO, gameDAO);
        ServiceException e = assertThrows(ServiceException.class, () -> service.createGame(new CreateGameRequest("yeehaw", "Big Game")));
        assertEquals(401, e.getStatusCode());
        assertEquals("Error: unauthorized", e.getMessage());
    }
}
