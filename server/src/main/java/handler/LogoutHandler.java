package handler;

import com.google.gson.Gson;
import io.javalin.http.Context;
import requests.LogoutRequest;
import results.ErrorResponse;
import service.LogoutService;
import service.ServiceException;

public class LogoutHandler {
    private final LogoutService logoutService;
    private final Gson gson = new Gson();

    public LogoutHandler(LogoutService logoutService){
        this.logoutService = logoutService;
    }

    public void logout(Context ctx) {
        try {
            String token = ctx.header("authorization");
            LogoutRequest request = new LogoutRequest(token);
            logoutService.logout(request);

            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result("{}");
        } catch (ServiceException e){
            ctx.status(e.getStatusCode());
            ctx.contentType("application/json");
            ctx.result(gson.toJson(new ErrorResponse(e.getMessage())));
        }catch (Exception e) {
            ctx.status(500);
            ctx.contentType("application/json");
            ctx.result(gson.toJson(new ErrorResponse("Error: internal server error")));
        }
    }
}
