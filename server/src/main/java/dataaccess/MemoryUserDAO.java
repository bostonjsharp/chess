package dataaccess;

import model.UserData;

import java.util.HashMap;
import java.util.Map;

public class MemoryUserDAO implements UserDAO{
    private Map<String, UserData> users;

    public MemoryUserDAO(){
        users = new HashMap<>();
    }

    public UserData getUser(String username){
       return users.get(username);
    }

    public void createUser(UserData user){
        users.put(user.username(), user);
    }
}
