package service;

import dataaccess.MemoryGameDAO;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import dataaccess.MemoryUserDAO;
import dataaccess.MemoryAuthDAO;
import model.UserData;
import model.AuthData;
import model.GameData;

public class ClearServiceTests {

    @Test
    public void clearingRemovesAllData() {
        MemoryUserDAO userDAO = new MemoryUserDAO();
        MemoryAuthDAO authDAO = new MemoryAuthDAO();
        MemoryGameDAO gameDAO = new MemoryGameDAO();
        userDAO.createUser(new UserData("bost", "pass", "test@gmail.com"));
        authDAO.createAuth(new AuthData("token1", "bost"));
        gameDAO.createGame("Turing Test");

        assertNotNull(userDAO.getUser("bost"));
        assertNotNull(authDAO.getAuth("token1"));
        assertEquals(1, gameDAO.listGames().size());

        ClearService service = new ClearService(userDAO, authDAO, gameDAO);
        service.clear();
        assertNull(userDAO.getUser("bost"));
        assertNull(authDAO.getAuth("token1"));
        assertEquals(0,gameDAO.listGames().size());
    }
}
