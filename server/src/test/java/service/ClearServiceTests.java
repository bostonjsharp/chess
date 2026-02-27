package service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import dataaccess.MemoryUserDAO;
import dataaccess.MemoryAuthDAO;
import model.UserData;
import model.AuthData;

public class ClearServiceTests {

    @Test
    public void clearingRemovesAllData() {
        MemoryUserDAO userDAO = new MemoryUserDAO();
        MemoryAuthDAO authDAO = new MemoryAuthDAO();
        userDAO.createUser(new UserData("bost", "pass", "test@gmail.com"));
        authDAO.createAuth(new AuthData("token1", "bost"));

        assertNotNull(userDAO.getUser("bost"));
        assertNotNull(authDAO.getAuth("token1"));

        ClearService service = new ClearService(userDAO, authDAO);
        service.clear();
        assertNull(userDAO.getUser("bost"));
        assertNull(authDAO.getAuth("token1"));
    }
}
