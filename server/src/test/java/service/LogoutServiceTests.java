package service;

import dataaccess.MemoryUserDAO;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import dataaccess.MemoryAuthDAO;
import model.AuthData;
import requests.LogoutRequest;
import service.LogoutService;
import service.ServiceException;

public class LogoutServiceTests {

    @Test
    public void logoutSuccess() throws ServiceException{
        MemoryAuthDAO authDAO = new MemoryAuthDAO();
        authDAO.createAuth(new AuthData("token1", "bost"));

        assertNotNull(authDAO.getAuth("token1"));
        LogoutService service = new LogoutService(authDAO);
        service.logout(new LogoutRequest("token1"));
        assertNull(authDAO.getAuth("token1"));
    }

    @Test
    public void logoutInvalid(){
        MemoryAuthDAO authDAO = new MemoryAuthDAO();
        LogoutService service = new LogoutService(authDAO);

        ServiceException e = assertThrows(ServiceException.class, () -> service.logout(new LogoutRequest("yippee")));
        assertEquals(401, e.getStatusCode());
        assertEquals("Error: unauthorized", e.getMessage());
    }
}
