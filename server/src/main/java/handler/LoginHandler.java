package handler;

import com.google.gson.Gson;
import io.javalin.http.Context;
import requests.LoginRequest;
import results.ErrorResponse;
import results.LoginResult;
import service.LoginService;
import service.ServiceException;

public class LoginHandler {
    private final LoginService loginService;
    private final Gson gson = new Gson();

    public LoginHandler(LoginService loginService){
        this.loginService = loginService;
    }

    public void login(Context ctx){
        try {
            LoginRequest request = gson.fromJson(ctx.body(), LoginRequest.class);
            LoginResult result = loginService.login(request);
            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(gson.toJson(result));
        } catch (ServiceException e) {
            ctx.status(e.getStatusCode());
            ctx.contentType("application/json");
            ctx.result(gson.toJson(new ErrorResponse(e.getMessage())));
        } catch (Exception e) {
            ctx.status(500);
            ctx.contentType("application/json");
            ctx.result(gson.toJson(new ErrorResponse("Error: internal server error")));
        }
    }

}
