package service;

import dataaccess.AuthDAO;
import dataaccess.UserDAO;
import requests.RegisterRequest;
import results.RegisterResult;

public class RegisterService {
    private UserDAO userDAO;
    private AuthDAO authDAO;

    public RegisterService(UserDAO userDAO, AuthDAO authDAO){
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public RegisterResult register(RegisterRequest request) {
        if (request == null || request.username() == null || request.password() == null || request.email() == null) {
            throw new RuntimeException("Bad Request");
        }
        if(userDAO.getUser(request.username()) != null){
            throw new RuntimeException("Already Taken");
        }

    }
}
