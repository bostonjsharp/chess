package service;

import dataaccess.AuthDAO;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;
import requests.RegisterRequest;
import results.RegisterResult;
import java.util.UUID;
import org.mindrot.jbcrypt.BCrypt;

public class RegisterService {
    private UserDAO userDAO;
    private AuthDAO authDAO;

    public RegisterService(UserDAO userDAO, AuthDAO authDAO){
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public RegisterResult register(RegisterRequest request) throws ServiceException {
        if (request == null ||
                request.username() == null ||
                request.password() == null ||
                request.email() == null) {
            throw new ServiceException(400, "Error: bad request");
        }
        if(userDAO.getUser(request.username()) != null){
            throw new ServiceException(403, "Error: already taken");
        }
        String hashPass = BCrypt.hashpw(request.password(), BCrypt.gensalt());
        UserData user = new UserData(request.username(), hashPass, request.email());
        userDAO.createUser(user);
        String token = UUID.randomUUID().toString();
        AuthData auth = new AuthData(token, request.username());
        authDAO.createAuth(auth);

        return new RegisterResult(request.username(), token);

    }
}
