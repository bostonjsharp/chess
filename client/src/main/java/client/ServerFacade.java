package client;

import com.google.gson.Gson;
import model.AuthData;
import requests.CreateGameRequest;
import requests.JoinGameRequest;
import requests.RegisterRequest;
import results.CreateGameResult;
import results.ListGamesResult;
import results.RegisterResult;
import requests.LoginRequest;
import results.LoginResult;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class ServerFacade {
    private final String serverURL;
    private final Gson gson = new Gson();

    public ServerFacade(String serverURL){
        this.serverURL = serverURL;
    }

    public AuthData register(String username, String password, String email) throws Exception {
        RegisterRequest request = new RegisterRequest(username, password, email);
        var result = makeRequest("POST", "/user", request, RegisterResult.class, null);
        return new AuthData(result.authToken(), result.username());
    }

    public AuthData login(String username, String password) throws Exception {
        LoginRequest request = new LoginRequest(username, password);
        var result = makeRequest("POST", "/session", request, LoginResult.class, null);
        return new AuthData(result.authToken(), result.username());
    }

    public void logout(String authToken) throws Exception {
        makeRequest("DELETE", "/session", null, null, authToken);
    }

    public ListGamesResult listGames(String authToken) throws Exception {
        return makeRequest("GET", "/game", null, ListGamesResult.class, authToken);
    }

    public int createGame(String gameName, String authToken) throws Exception {
        CreateGameRequest request = new CreateGameRequest(authToken, gameName);
        var result = makeRequest("POST", "/game", request, CreateGameResult.class, authToken);
        return result.gameID();
    }
    public void joinGame(int gameID, String playerColor, String authToken) throws Exception{
        JoinGameRequest request = new JoinGameRequest(authToken, playerColor, gameID);
        makeRequest("PUT", "/game", request, null, authToken);

    }

    public void clear() throws Exception {
        makeRequest("DELETE", "/db", null, null, null);
    }

    private <T> T makeRequest(String method, String path, Object requestBody, Class<T> responseClass, String authToken) throws Exception {
        URL url = URI.create(serverURL + path).toURL();
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        http.setRequestMethod(method);
        http.setDoOutput(true);

        if (authToken != null) {
            http.addRequestProperty("Authorization", authToken);
        }

        if (requestBody != null) {
            http.addRequestProperty("Content-Type", "application/json");
            String json = gson.toJson(requestBody);
            try (OutputStream reqBod = http.getOutputStream()) {
                reqBod.write(json.getBytes());
            }
        }
        http.connect();
        if(http.getResponseCode() / 100 == 2){
            if (responseClass == null){
                return null;
            }

            try (InputStream responseBod = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(responseBod);
                return gson.fromJson(reader, responseClass);
            }
        } else {
            try (InputStream responseErr = http.getErrorStream()){
                if (responseErr != null){
                    InputStreamReader reader = new InputStreamReader(responseErr);
                    ErrorResponse error = gson.fromJson(reader, ErrorResponse.class);
                    throw new Exception(error.message());
                } else {
                    throw new Exception("Server Error: " + http.getResponseCode());
                }
            }
        }
    }
    private record ErrorResponse(String message) {

    }
}
