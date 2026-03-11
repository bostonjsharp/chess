package dataaccess;
import model.UserData;
import java.sql.SQLException;

public class MySQLUserDAO implements UserDAO {
    public UserData getUser(String username){
        String sql = "SELECT username, password, email FROM users WHERE username = ?";

        try(var conn = DatabaseManager.getConnection(); var statemnt = conn.prepareStatement(sql)){
            statemnt.setString(1, username);

            try(var resSet = statemnt.executeQuery()){
                if (resSet.next()){
                    return new UserData(
                            resSet.getString("username"),
                            resSet.getString("password"),
                            resSet.getString("email")
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
        String sql = "DELETE FROM users";
        try (var conn = DatabaseManager.getConnection(); var statemnt = conn.prepareStatement(sql)){
            statemnt.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Error clearing users", e);
        }
    }
}
