package service;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import model.GameData;
import requests.ListGamesRequest;
import results.ListGamesResult;

public class ListGamesService {
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    public ListGamesService(AuthDAO authDAO, GameDAO gameDAO){
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    public ListGamesResult listGames(ListGamesRequest request) throws ServiceException{
        if(request == null || request.authToken() == null){
            throw new ServiceException(401, "Error: unauthorized");
        }
        if(authDAO.getAuth(request.authToken()) == null){
            throw new ServiceException(401, "Error: unauthorized");
        }
        return new ListGamesResult(gameDAO.listGames());
    }
}
