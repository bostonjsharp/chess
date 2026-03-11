package dataaccess;

import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SqlUserDAOTest {
    private MySQLUserDAO userDAO;

    @BeforeEach
    public void setup() throws Exception {
        DatabaseManager.createDatabase();
        DatabaseManager.createTables();

        userDAO = new MySQLUserDAO();
        userDAO.clear();
    }

    @Test
    public void createUserSuccess() {
        UserData user = new UserData("bost", "pass", "test@gmail.com");

        userDAO.createUser(user);
        UserData result = userDAO.getUser("bost");
        assertNotNull(result);
        assertEquals("bost", result.username());
        assertEquals("pass", result.password());
        assertEquals("test@gmail.com", result.email());
    }

    @Test
    public void getUserFailure() {
        UserData result = userDAO.getUser("bingbong");
        assertNull(result);
    }

    @Test
    public void clearUserSuccess(){
        UserData user1 = new UserData("bost", "pass", "test@gmail.com");
        UserData user2 = new UserData("boston", "password", "testing@gmail.com");
        userDAO.createUser(user1);
        userDAO.createUser(user2);
        userDAO.clear();
        assertNull(userDAO.getUser("bost"));
        assertNull(userDAO.getUser("boston"));


    }
}
