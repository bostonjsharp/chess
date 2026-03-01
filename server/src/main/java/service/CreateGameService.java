package service;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import model.GameData;
import requests.CreateGameRequest;
import results.CreateGameResult;

public class CreateGameService {
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    public CreateGameService(AuthDAO authDAO, GameDAO gameDAO){
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    public CreateGameResult createGame(String authToken, CreateGameRequest request) throws ServiceException{
        if(authToken == null || request.authToken() == null || authDAO.getAuth(authToken) == null ){
            throw new ServiceException(401, "Error: unauthorized");
        }
        if (request.gameName() == null){
            throw new ServiceException(400, "Error: bad request");
        }
        GameData game = gameDAO.createGame(request.gameName());
        return new CreateGameResult(game.gameID());
    }
}
