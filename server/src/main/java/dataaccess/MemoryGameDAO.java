package dataaccess;

import model.GameData;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import chess.ChessGame;

public class MemoryGameDAO implements GameDAO{
    private final Map<Integer, GameData> games = new HashMap<>();
    private int nextId = 1;

    public GameData createGame(String gameName) {
        int id = nextId++;
        ChessGame chessGame = new ChessGame();
        GameData game = new GameData(id, null, null, gameName, chessGame);
        games.put(id, game);
        return game;
    }

    public Collection<GameData> listGames(){
        return games.values();
    }

    public GameData getGame(int gameID){
        return games.get(gameID);
    }

    public void updateGame(GameData game) {
        games.put(game.gameID(), game);
    }

    public void clear() {
        games.clear();
        nextId = 1;
    }
}
