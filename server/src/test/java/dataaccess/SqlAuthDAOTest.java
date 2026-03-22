package dataaccess;

import model.AuthData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SqlAuthDAOTest {
    private MySQLAuthDAO authDAO;

    @BeforeEach
    public void setup() throws Exception{
        DatabaseManager.createDatabase();
        DatabaseManager.createTables();

        authDAO = new MySQLAuthDAO();
        authDAO.clear();
    }

    @Test
    public void createAuthSuccess(){
        AuthData auth = new AuthData("token1", "bost");
        authDAO.createAuth(auth);
        AuthData result = authDAO.getAuth("token1");

        assertNotNull(result);
        assertEquals("token1", result.authToken());
        assertEquals("bost", result.username());

    }

    @Test
    public void getAuthFailure() {
        AuthData fail = authDAO.getAuth("yeehaw");
        assertNull(fail);
    }

    @Test
    public void getAuthSuccess() {
        AuthData auth = new AuthData("token2", "bost1");
        authDAO.createAuth(auth);
        AuthData result = authDAO.getAuth("token2");

        assertNotNull(result);
        assertEquals("token2", result.authToken());
        assertEquals("bost1", result.username());
    }

    @Test
    public void clearSuccess() {
        authDAO.createAuth(new AuthData("token1", "bost"));
        authDAO.createAuth(new AuthData("token2", "boston"));
        authDAO.clear();
        assertNull(authDAO.getAuth("token1"));
        assertNull(authDAO.getAuth("token2"));
    }
}
