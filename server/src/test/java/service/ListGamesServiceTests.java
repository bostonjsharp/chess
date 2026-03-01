package service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import dataaccess.MemoryGameDAO;
import dataaccess.MemoryAuthDAO;
import model.AuthData;
import requests.ListGamesRequest;
import results.ListGamesResult;

public class ListGamesServiceTests {

    @Test
    public void listGamesSuccess() throws ServiceException{
        MemoryAuthDAO authDAO = new MemoryAuthDAO();
        MemoryGameDAO gameDAO = new MemoryGameDAO();

        authDAO.createAuth(new AuthData("token1", "bost"));
        gameDAO.createGame("Gameth the First");
        gameDAO.createGame("Gameth the Second");

        ListGamesService service = new ListGamesService(authDAO, gameDAO);
        ListGamesResult result = service.listGames(new ListGamesRequest("token1"));

        assertNotNull(result.games());
        assertEquals(2, result.games().size());
    }

    @Test
    public void listGamesUnauthorized(){
        MemoryAuthDAO authDAO = new MemoryAuthDAO();
        MemoryGameDAO gameDAO = new MemoryGameDAO();

        ListGamesService service = new ListGamesService(authDAO, gameDAO);

        ServiceException e = assertThrows(ServiceException.class, () -> service.listGames(new ListGamesRequest("ding")));
        assertEquals(401, e.getStatusCode());
        assertEquals("Error: unauthorized", e.getMessage());
    }
}
