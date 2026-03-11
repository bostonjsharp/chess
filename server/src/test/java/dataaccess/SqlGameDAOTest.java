package dataaccess;

import chess.ChessGame;
import model.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class SqlGameDAOTest {
    private MySQLGameDAO gameDAO;

    @BeforeEach
    public void setup() throws Exception{
        DatabaseManager.createDatabase();
        DatabaseManager.createTables();
        gameDAO = new MySQLGameDAO();
        gameDAO.clear();
    }

    @Test
    public void createGameSuccess(){
        GameData game = gameDAO.createGame("cheap game");
        assertNotNull(game);
        assertTrue(game.gameID() > 0);
        assertEquals("cheap game", game.gameName());
        assertNull(game.whiteUsername());
        assertNull(game.blackUsername());
        assertNotNull(game.game());
    }

    @Test
    public void listGamesSuccess() {
        gameDAO.createGame("red game");
        gameDAO.createGame("blue game");
        Collection<GameData> games = gameDAO.listGames();
        assertNotNull(games);
        assertEquals(2,games.size());
    }

    @Test
    public void listGamesEmpty() {
        Collection<GameData> games = gameDAO.listGames();
        assertNotNull(games);
        assertEquals(0, games.size());
    }

    @Test
    public void getGameSuccess() {
        GameData gameBoy = gameDAO.createGame("game boy");
        GameData gameGot = gameDAO.getGame(gameBoy.gameID());

        assertNotNull(gameGot);
        assertEquals(gameBoy.gameID(), gameGot.gameID());
        assertEquals("game boy", gameGot.gameName());
        assertNull(gameGot.whiteUsername());
        assertNull(gameGot.blackUsername());
        assertNotNull(gameGot.game());
    }

    @Test
    public void getGameFailure(){
        GameData gameGot = gameDAO.getGame(123);
        assertNull(gameGot);
    }

    @Test
    public void updateGameSuccess(){
        GameData gameBoy = gameDAO.createGame("game boy advanced");
        ChessGame chessGame = gameBoy.game();
        GameData updated = new GameData(
                gameBoy.gameID(),
                "bost",
                "mel",
                "game boy advanced",
                chessGame
        );

        gameDAO.updateGame(updated);
        GameData gameGot = gameDAO.getGame(gameBoy.gameID());
        assertNotNull(gameGot);
        assertEquals("bost", gameGot.whiteUsername());
        assertEquals("mel", gameGot.blackUsername());
        assertEquals("game boy advanced", gameGot.gameName());
    }

    @Test
    public void updateGameFailure() {
        GameData testGame = new GameData(
                12345,
                "bing",
                "bong",
                "bingbong",
                new ChessGame()
        );
        assertDoesNotThrow(() -> gameDAO.updateGame(testGame));
        GameData gameGot = gameDAO.getGame(12345);
        assertNull(gameGot);
    }

    @Test
    public void clearSuccess() {
        gameDAO.createGame("first");
        gameDAO.createGame("second");
        gameDAO.clear();

        Collection<GameData> games = gameDAO.listGames();
        assertEquals(0, games.size());
    }
}
