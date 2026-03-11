package service;

import dataaccess.AuthDAO;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;
import requests.LoginRequest;
import results.LoginResult;
import java.util.UUID;
import org.mindrot.jbcrypt.BCrypt;

public class LoginService {
    private UserDAO userDAO;
    private AuthDAO authDAO;

    public LoginService(UserDAO userDAO, AuthDAO authDAO){
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public LoginResult login(LoginRequest request) throws ServiceException{
        if (request == null ||
                request.username() == null ||
                request.password() == null) {
            throw new ServiceException(400, "Error: bad request");
        }
        UserData user = userDAO.getUser(request.username());
        if(user == null){
            throw new ServiceException(401, "Error: unauthorized");
        }
        if(!BCrypt.checkpw(request.password(), user.password())){
            throw new ServiceException(401, "Error: unauthorized");
        }
        String token = UUID.randomUUID().toString();
        AuthData auth = new AuthData(token, request.username());
        authDAO.createAuth(auth);

        return new LoginResult(request.username(), token);
    }
}
