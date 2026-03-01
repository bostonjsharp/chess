package service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import model.AuthData;
import model.GameData;
import requests.JoinGameRequest;
import service.JoinGameService;
import service.ServiceException;

public class JoinGameServiceTests {

    @Test
    public void joinGameSuccessWhite() throws ServiceException{
        MemoryAuthDAO authDAO = new MemoryAuthDAO();
        MemoryGameDAO gameDAO = new MemoryGameDAO();

        authDAO.createAuth(new AuthData("token1", "bost"));
        GameData game = gameDAO.createGame("Squid Game");

        JoinGameService service = new JoinGameService(authDAO, gameDAO);
        service.joinGame(new JoinGameRequest("token1", "WHITE", game.gameID()));

        GameData updated = gameDAO.getGame(game.gameID());
        assertEquals("bost", updated.whiteUsername());
        assertNull(updated.blackUsername());
    }

    @Test
    public void joinGameAlreadyTaken() {
        MemoryAuthDAO authDAO = new MemoryAuthDAO();
        MemoryGameDAO gameDAO = new MemoryGameDAO();

        authDAO.createAuth(new AuthData("token1", "bost"));
        authDAO.createAuth(new AuthData("token2", "bost2"));

        GameData game = gameDAO.createGame("The Game");
        JoinGameService service = new JoinGameService(authDAO, gameDAO);
        assertDoesNotThrow(() -> service.joinGame(new JoinGameRequest("token1", "WHITE", game.gameID())));

        ServiceException e = assertThrows(ServiceException.class, () -> service.joinGame(new JoinGameRequest("token2", "WHITE", game.gameID())));
        assertEquals(403, e.getStatusCode());
        assertEquals("Error: already taken", e.getMessage());
    }
}
