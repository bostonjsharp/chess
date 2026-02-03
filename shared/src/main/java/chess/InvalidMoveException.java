package chess;

/**
 * Indicates an invalid move was made in a game
 */
public class InvalidMoveException extends Exception {
    //check if move is in list of valid moves, check if it's the teams turn

    public InvalidMoveException() {}

    public InvalidMoveException(String message) {
        super(message);
    }
}
