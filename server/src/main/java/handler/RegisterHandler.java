package handler;

import io.javalin.http.Context;
import requests.RegisterRequest;
import results.RegisterResult;
import results.ErrorResponse;
import service.RegisterService;
import service.ServiceException;

public class RegisterHandler {
    private final RegisterService registerService;

    public RegisterHandler(RegisterService registerService){
        this.registerService = registerService;
    }

    public void register(Context ctx) {
        try {
            RegisterRequest request = ctx.bodyAsClass(RegisterRequest.class);
            RegisterResult result = registerService.register(request);
            ctx.status(200);
            ctx.json(result);
        } catch (ServiceException e) {
            ctx.status(e.getStatusCode());
            ctx.json(new ErrorResponse(e.getMessage()));
        }
    }
}
