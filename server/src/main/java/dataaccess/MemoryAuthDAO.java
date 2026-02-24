package dataaccess;

import model.AuthData;

import java.util.HashMap;
import java.util.Map;

public class MemoryAuthDAO implements AuthDAO{

    private Map<String, AuthData> authMap;

    public MemoryAuthDAO(){
        authMap = new HashMap<>();
    }

    public AuthData getAuth(String authToken){
        return authMap.get(authToken);
    }

    public void createAuth(AuthData auth){
        authMap.put(auth.authToken(), auth);
    }
}
