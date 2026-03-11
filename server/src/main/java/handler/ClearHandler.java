package handler;
import com.google.gson.Gson;
import io.javalin.http.Context;
import results.ErrorResponse;
import service.ClearService;
import service.ServiceException;

public class ClearHandler {
    private final ClearService clearService;
    private final Gson gson = new Gson();

    public ClearHandler(ClearService clearService) {
        this.clearService = clearService;
    }

    public void clear(Context ctx) {
        try{clearService.clear();
            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result("{}");
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
