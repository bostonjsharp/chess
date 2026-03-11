package handler;

import com.google.gson.Gson;
import io.javalin.http.Context;
import results.ErrorResponse;
import service.JoinGameService;
import service.ServiceException;
import requests.JoinGameRequest;

public class JoinGameHandler {
    private final JoinGameService joinGameService;
    private final Gson gson = new Gson();

    public JoinGameHandler(JoinGameService joinGameService){
        this.joinGameService = joinGameService;
    }
    private record Body(String playerColor, int gameID){}

    public void joinGame(Context ctx) {
        try{
            String token = ctx.header("authorization");
            Body body = gson.fromJson(ctx.body(), Body.class);
            if(body == null){
                throw new ServiceException(400, "Error: bad request");
            }
            JoinGameRequest request = new JoinGameRequest(token, body.playerColor(), body.gameID());
            joinGameService.joinGame(request);

            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result("{}");
        } catch (ServiceException e) {
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
