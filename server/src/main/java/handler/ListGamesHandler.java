package handler;
import com.google.gson.Gson;
import io.javalin.http.Context;
import requests.ListGamesRequest;
import results.ErrorResponse;
import results.ListGamesResult;
import service.ListGamesService;
import service.ServiceException;

public class ListGamesHandler {
    private final ListGamesService listGamesService;
    private final Gson gson = new Gson();

    public ListGamesHandler(ListGamesService listGamesService){
        this.listGamesService = listGamesService;
    }

    public void listGames(Context ctx) {
        try{
            String token = ctx.header("bigAuth");
            ListGamesResult result = listGamesService.listGames(new ListGamesRequest(token));
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
