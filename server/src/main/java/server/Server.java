package server;

import dataaccess.*;
import handler.*;
import io.javalin.*;
import service.*;
import com.google.gson.Gson;

public class Server {

    private final Javalin javalin;

    public Server() {
        try{
            DatabaseManager.createDatabase();
            DatabaseManager.createTables();
        } catch (DataAccessException e) {
            throw new RuntimeException("Unable to initialize database", e);
        }
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        UserDAO userDAO = new MySQLUserDAO();
        AuthDAO authDAO = new MySQLAuthDAO();
        GameDAO gameDAO = new MySQLGameDAO();

        RegisterService registerService = new RegisterService(userDAO, authDAO);
        RegisterHandler registerHandler = new RegisterHandler(registerService);

        ClearService clearService = new ClearService(userDAO, authDAO, gameDAO);
        ClearHandler clearHandler = new ClearHandler(clearService);

        LoginService loginService = new LoginService(userDAO, authDAO);
        LoginHandler loginHandler = new LoginHandler(loginService);

        LogoutService logoutService = new LogoutService(authDAO);
        LogoutHandler logoutHandler = new LogoutHandler(logoutService);

        ListGamesService listGamesService = new ListGamesService(authDAO, gameDAO);
        ListGamesHandler listGamesHandler = new ListGamesHandler(listGamesService);

        CreateGameService createGameService = new CreateGameService(authDAO, gameDAO);
        CreateGameHandler createGameHandler = new CreateGameHandler(createGameService);

        JoinGameService joinGameService = new JoinGameService(authDAO, gameDAO);
        JoinGameHandler joinGameHandler = new JoinGameHandler(joinGameService);

        javalin.post("/user", registerHandler::register);
        javalin.delete("/db", clearHandler::clear);
        javalin.post("/session", loginHandler::login);
        javalin.delete("/session", logoutHandler::logout);
        javalin.get("/game", listGamesHandler::listGames);
        javalin.post("/game", createGameHandler::createGame);
        javalin.put("/game", joinGameHandler::joinGame);
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
