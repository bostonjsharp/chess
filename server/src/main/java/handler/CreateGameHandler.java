package handler;

import com.google.gson.Gson;
import io.javalin.http.Context;
import requests.CreateGameRequest;
import results.CreateGameResult;
import results.ErrorResponse;
import service.CreateGameService;
import service.ServiceException;

public class CreateGameHandler {
    private final CreateGameService createGameService;
    private final Gson gson = new Gson();

    public CreateGameHandler(CreateGameService createGameService){
        this.createGameService = createGameService;
    }

    private record Body(String gameName) {}

    public void createGame(Context ctx) {
        try{
            String token = ctx.header("authorization");
            Body body = gson.fromJson(ctx.body(), Body.class);
            String gameName = (body == null) ? null : body.gameName();
            CreateGameResult result = createGameService.createGame(new CreateGameRequest(token, gameName));
            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(gson.toJson(result));
        } catch (ServiceException e) {
            ctx.status(e.getStatusCode());
            ctx.contentType("application/json");
            ctx.result(gson.toJson(new ErrorResponse(e.getMessage())));
        }
    }
}
