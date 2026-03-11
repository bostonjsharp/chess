package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MySQLGameDAO implements GameDAO {

    private final Gson gson = new Gson();

    public GameData createGame(String gameName) {
        String sql = """
                INSERT INTO games (whiteUsername, blackUsername, gameName, gameJson) VALUES (?, ?, ?, ?)
                """;

        ChessGame chessGame = new ChessGame();
        String gameJson = gson.toJson(chessGame);
        try(var conn = DatabaseManager.getConnection(); var statemnt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
            statemnt.setString(1, null);
            statemnt.setString(2, null);
            statemnt.setString(3, gameName);
            statemnt.setString(4, gameJson);

            statemnt.executeUpdate();

            try(var resSet = statemnt.getGeneratedKeys()){
                resSet.next();
                int id = resSet.getInt(1);
                return new GameData(id, null, null, gameName, chessGame);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error creating game", e);
        }
    }

    public Collection<GameData> listGames(){
        String sql = "SELECT * FROM games";
        List<GameData> games = new ArrayList<>();

        try (var conn = DatabaseManager.getConnection();
             var statemnt = conn.prepareStatement(sql);
             var resSet = statemnt.executeQuery()) {
            while (resSet.next()){
                ChessGame chessGame = gson.fromJson(resSet.getString("gameJson"), ChessGame.class);

                GameData game = new GameData(
                        resSet.getInt("gameID"),
                        resSet.getString("whiteUsername"),
                        resSet.getString("blackUsername"),
                        resSet.getString("gameName"),
                        chessGame
                );
                games.add(game);
            }
            return games;
        } catch (Exception e) {
            throw new RuntimeException("Error listing games", e);
        }
    }

    public GameData getGame(int gameID) {
        String sql = "SELECT * FROM games WHERE gameID = ?";

        try(var conn = DatabaseManager.getConnection(); var statemnt = conn.prepareStatement(sql)) {
            statemnt.setInt(1, gameID);

            try(var resSet = statemnt.executeQuery()) {
                if (resSet.next()){
                    ChessGame chessGame = gson.fromJson(resSet.getString("gameJson"), ChessGame.class);

                    return new GameData(
                            resSet.getInt("gameID"),
                            resSet.getString("whiteUsername"),
                            resSet.getString("blackUsername"),
                            resSet.getString("gameName"),
                            chessGame
                    );
                }
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Error getting game", e);
        }
    }

    public void updateGame(GameData game){
        String sql = """
                UPDATE games SET whiteUsername=?, blackUsername=?, gameName=?, gameJson=?
                WHERE gameID=?
                """;
        String gameJson = gson.toJson(game.game());

        try(var conn = DatabaseManager.getConnection(); var statemnt = conn.prepareStatement(sql)){
            statemnt.setString(1, game.whiteUsername());
            statemnt.setString(2, game.blackUsername());
            statemnt.setString(3, game.gameName());
            statemnt.setString(4, gameJson);
            statemnt.setInt(5, game.gameID());
            statemnt.executeUpdate();
        } catch (Exception e){
            throw new RuntimeException("Error upadating game", e);
        }
    }

    public void clear() {
        String sql = "DELETE FROM games";

        try(var conn = DatabaseManager.getConnection(); var statemnt = conn.prepareStatement(sql)){
            statemnt.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Error clearing games", e);
        }
    }

}
