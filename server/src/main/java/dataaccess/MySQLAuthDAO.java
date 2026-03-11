package dataaccess;

import model.AuthData;

public class MySQLAuthDAO implements AuthDAO{

    public AuthData getAuth(String authToken){
        String sql = "SELECT authToken, username FROM auths WHERE authToken = ?";

        try (var conn = DatabaseManager.getConnection(); var statemnt = conn.prepareStatement(sql)) {
            statemnt.setString(1,authToken);
            try(var resSet = statemnt.executeQuery()){
                if(resSet.next()){
                    return new AuthData(resSet.getString("authToken"),resSet.getString("username"));
                }
            }
            return null;
        } catch (Exception e){
            throw new RuntimeException("Error getting auth", e);
        }
    }

    public void createAuth(AuthData auth){
        String sql = "INSERT INTO auths (authToken, username) VALUES (?,?)";

        try(var conn = DatabaseManager.getConnection(); var statemnt = conn.prepareStatement(sql)){
            statemnt.setString(1, auth.authToken());
            statemnt.setString(2, auth.username());

            statemnt.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Error creating auth", e);
        }
    }

    public void deleteAuth(String authToken) {
        String sql = "DELETE FROM auths WHERE authToken = ?";

        try (var conn = DatabaseManager.getConnection(); var statemnt = conn.prepareStatement(sql)) {
            statemnt.setString(1, authToken);
            statemnt.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Error deleting auth", e);
        }
    }

    public void clear() {
        String sql = "DELETE FROM auths";

        try (var conn = DatabaseManager.getConnection(); var statemnt = conn.prepareStatement(sql)){
            statemnt.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Error clearing auths", e);
        }
    }
}
