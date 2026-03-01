package service;

import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryUserDAO;
import model.UserData;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import requests.LoginRequest;
import results.LoginResult;

public class LoginServiceTests {

    @Test
    public void loginSuccess() throws ServiceException {
        MemoryUserDAO userDAO = new MemoryUserDAO();
        MemoryAuthDAO authDAO = new MemoryAuthDAO();

        userDAO.createUser(new UserData("bost", "pass", "test@gmail.com"));
        LoginService service = new LoginService(userDAO, authDAO);
        LoginResult result = service.login(new LoginRequest("bost", "pass"));

        assertEquals("bost", result.username());
        assertNotNull(result.authToken());
        assertFalse(result.authToken().isBlank());
        assertNotNull(authDAO.getAuth(result.authToken()));
        assertEquals("bost", authDAO.getAuth(result.authToken()).username());
    }

    @Test
    public void loginWrongPassword(){
        MemoryUserDAO userDAO = new MemoryUserDAO();
        MemoryAuthDAO authDAO = new MemoryAuthDAO();

        userDAO.createUser(new UserData("bost", "pass", "test@gmail.com"));
        LoginService service = new LoginService(userDAO, authDAO);

        ServiceException e = assertThrows(ServiceException.class, () -> service.login(new LoginRequest("bost", "bleh")));
        assertEquals(401, e.getStatusCode());
        assertEquals("Error: unauthorized", e.getMessage());
    }
}
