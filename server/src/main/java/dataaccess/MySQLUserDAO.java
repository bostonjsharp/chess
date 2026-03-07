package dataaccess;
import model.UserData;
import java.sql.SQLException;

public class MySQLUserDAO implements UserDAO {
    public UserData getUser(String username){
        String sql = "SELECT username, password, email FROM users WHERE username = ?";

        try(var conn = DatabaseManager.getConnection(); var statemnt = conn.prepareStatement(sql)){
            statemnt.setString(1, username);

            try(var rs = statemnt.executeQuery()){
                if (rs.next()){
                    return new UserData(
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("email")
                    );
                }
            }


            return null;
        } catch (Exception e){
            throw new RuntimeException("Error getting user", e);
        }
    }

    public void createUser(UserData user){
        String sql = "INSERT INTO users (username, password, email) VALUES (?,?,?)";

        try (var conn = DatabaseManager.getConnection(); var statemnt = conn.prepareStatement(sql)){
            statemnt.setString(1, user.username());
            statemnt.setString(2,user.password());
            statemnt.setString(3, user.email());
            statemnt.executeUpdate();
        } catch (Exception e){
            throw new RuntimeException("Error creating user", e);
        }

    }

    public void clear() {

    }
}
