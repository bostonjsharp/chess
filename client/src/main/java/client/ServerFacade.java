package client;

import com.google.gson.Gson;
import model.AuthData;
import request.RegisterRequest

public class ServerFacade {
    private final String serverURL;
    private final Gson gson = new Gson();

    public ServerFacade(String serverURL){
        this.serverURL = serverURL;
    }

    public AuthData register(String username, String password, String email) throws Exception {
        RegisterRequest request =
    }
}
