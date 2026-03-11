package handler;

import com.google.gson.Gson;
import io.javalin.http.Context;
import requests.RegisterRequest;
import results.RegisterResult;
import results.ErrorResponse;
import service.RegisterService;
import service.ServiceException;

public class RegisterHandler {
    private final RegisterService registerService;
    private final Gson gson = new Gson();

    public RegisterHandler(RegisterService registerService){
        this.registerService = registerService;
    }

    public void register(Context ctx) {
        try {
            RegisterRequest request = gson.fromJson(ctx.body(), RegisterRequest.class);
            RegisterResult result = registerService.register(request);
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
