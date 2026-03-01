package service;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import model.GameData;
import requests.JoinGameRequest;

public class JoinGameService {
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    public JoinGameService(AuthDAO authDAO, GameDAO gameDAO){
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    public void joinGame(JoinGameRequest request) throws ServiceException {
        if (request == null || request.authToken() == null || authDAO.getAuth(request.authToken()) == null){
            throw new ServiceException(401, "Error: unauthorized");
        }

        GameData game = gameDAO.getGame(request.gameID());
        if(game == null){
            throw new ServiceException(400, "Error: bad request");
        }
        if(request.playerColor() == null || request.playerColor().isBlank()){
            throw new ServiceException(400, "Error: bad request");
        }
        String color = request.playerColor().toUpperCase();
        String username = authDAO.getAuth(request.authToken()).username();

        if(!color.equals("WHITE") && !color.equals("BLACK")){
            throw new ServiceException(400, "Error: bad request");
        }
        if(color.equals("WHITE")){
            if(game.whiteUsername() != null){
                throw new ServiceException(403, "Error: already taken");
            }
            GameData updated = new GameData(
                    game.gameID(),
                    username,
                    game.blackUsername(),
                    game.gameName(),
                    game.game()
            );
            gameDAO.updateGame(updated);
        } else {
            if (game.blackUsername() != null){
                throw new ServiceException(403, "Error: already taken");
            }
            GameData updated = new GameData(
                    game.gameID(),
                    game.whiteUsername(),
                    username,
                    game.gameName(),
                    game.game()
            );
            gameDAO.updateGame(updated);
        }
    }
}
