package service;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import dataaccess.MemoryUserDAO;
import dataaccess.MemoryAuthDAO;
import requests.RegisterRequest;
import results.RegisterResult;


public class RegisterServiceTests {

    @Test
    public void registerSuccess() throws ServiceException {
        MemoryUserDAO userDAO = new MemoryUserDAO();
        MemoryAuthDAO authDAO = new MemoryAuthDAO();
        RegisterService service = new RegisterService(userDAO, authDAO);

        RegisterRequest request = new RegisterRequest("bostonjsharp", "pass", "test@gmail.com");
        RegisterResult result = service.register(request);

        assertNotNull(result);
        assertEquals("bostonjsharp", result.username());
        assertNotNull(result.authToken());
        assertNotNull(userDAO.getUser("bostonjsharp"));
    }

    @Test
    public void duplicateRegisterFails() {
        MemoryUserDAO userDAO = new MemoryUserDAO();
        MemoryAuthDAO authDAO = new MemoryAuthDAO();
        RegisterService service = new RegisterService(userDAO, authDAO);
        RegisterRequest request = new RegisterRequest("bostonjsharp", "pass", "test@gmail.com");

        service.register(request);

        assertThrows(ServiceException.class, () -> service.register(request));
    }
}
