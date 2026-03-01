package service;

import dataaccess.AuthDAO;
import requests.LogoutRequest;

public class LogoutService {
    private final AuthDAO authDAO;

    public LogoutService(AuthDAO authDAO){
        this.authDAO = authDAO;
    }

    public void logout(LogoutRequest request) throws ServiceException{
        if (request == null || request.authToken() == null){
            throw new ServiceException(401, "Error: unauthorized");
        }
        if(authDAO.getAuth(request.authToken()) == null){
            throw new ServiceException(401, "Error: unauthorized");
        }

        authDAO.deleteAuth(request.authToken());
    }
}
