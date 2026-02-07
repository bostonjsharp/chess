package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private ChessBoard board;
    private TeamColor turn;


    public ChessGame() {
        this.board = new ChessBoard();
        this.board.resetBoard();
        this.turn = TeamColor.WHITE;

    }

    /**
     * @return Which team's turn it is
     */

    public TeamColor getTeamTurn() {
        return turn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        turn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }
    public void simulateMove(ChessMove move) {
        //check if move is in list of valid moves, check if it's the teams turn
        ChessPiece piece = board.getPiece(move.getStartPosition());
        if(piece == null){return;}
        //TODO: handle pawns and promo pieces
        board.addPiece(move.getEndPosition(), piece);
        board.removePiece(move.getStartPosition());


    }

    private Collection<ChessMove> simulateGame(ChessBoard board, Collection<ChessMove> rawMoves, ChessPiece piece){
        Collection<ChessMove> invalidMoves = new ArrayList();
        for (ChessMove rawMove : rawMoves) {
            ChessGame clone = new ChessGame();
            clone.setBoard(new ChessBoard(board)) ;
            clone.setTeamTurn(this.turn);
            clone.simulateMove(rawMove);
            ChessPiece stillThere = clone.getBoard().getPiece(rawMove.getStartPosition());
            //if move leads to check, add to list to be removed from validMoves
            if(clone.isInCheck(piece.getTeamColor())){
                invalidMoves.add(rawMove);
            }
        }
        return invalidMoves;
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if(piece == null){return null;}
        Collection<ChessMove> validMoves = piece.pieceMoves(board, startPosition);
        var invalidMoves = simulateGame(board, validMoves, piece);
        validMoves.removeIf(chessM -> invalidMoves.contains(chessM));
        return validMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        //check if move is in list of valid moves, check if it's the teams turn
        ChessPiece firstPiece = board.getPiece(move.getStartPosition());
        Collection<ChessMove> candidateMoves = validMoves(move.getStartPosition());
        if(candidateMoves == null ||
                !candidateMoves.contains(move) ||
                this.turn != firstPiece.getTeamColor()){
            throw new InvalidMoveException(""); //TODO: setup exception
        }
        ChessPiece piece = firstPiece;

        if(move.getPromotionPiece() != null){
            piece = new ChessPiece(firstPiece.getTeamColor(), move.getPromotionPiece());
        }
        board.addPiece(move.getEndPosition(), piece);
        board.removePiece(move.getStartPosition());
        setTeamTurn(getTeamTurn() == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE);
        //do move -> set team turn to other team
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        //if (some == thing){ return if true} else {return if false}
        //somevar = (some == thing) ? return if true : return if false
        ChessPosition myKingPosition = getKingPosition(teamColor);

        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPosition position = new ChessPosition(i,j);
                ChessPiece piece = board.getPiece(position);
                if (piece ==  null){
                    continue;
                }
                //Need to get valid moves from the Moves class or else infinite recursion
                if (piece.getTeamColor() != teamColor){
                    var oppMoves = piece.pieceMoves(board, position);
                    for (ChessMove oppMove : oppMoves) {
                        if (oppMove.getEndPosition().equals(myKingPosition)){
                            return true;
                        }

                    }
//                    if(oppMoves.stream().anyMatch((chessM -> chessM.getEndPosition() == myKingPosition))){
//                        return true;
                }
            }
        }
        return false;
    }

    //clone the board -> perform a move on cloned board -> see if it would put the king in check or get the king out of check
    //use for valid moves weeding
    //start here


    private boolean pullPieces(TeamColor teamColor){
        for (int i = 1; i <= 8; i++){
            for(int j= 1; j<= 8; j++){
                ChessPosition position = new ChessPosition(i,j);
                ChessPiece piece = board.getPiece(position);
                if (piece ==  null || piece.getTeamColor() != teamColor){
                    continue;
                }
                if(!validMoves(position).isEmpty()){
                    return false;
                }
            }
        }
        return true;
    }
    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (getTeamTurn() != teamColor) return false;
        if (!isInCheck(teamColor)) return false;
        return pullPieces(teamColor);
    }
    //your turn -> no valid moves -> king is in check

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (getTeamTurn() != teamColor) return false;
        if (isInCheck(teamColor)) return false;
        return pullPieces(teamColor);
    }
    //your turn -> no valid moves


    private ChessPosition getKingPosition(TeamColor color){
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPosition position = new ChessPosition(i,j);
                ChessPiece piece = board.getPiece(position);
                if (piece ==  null || piece.getTeamColor() != color){
                    continue;
                }
                if(piece.getPieceType() == ChessPiece.PieceType.KING){
                    return position;
                }

            }
        }
        return null;
    }
    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return Objects.equals(board, chessGame.board) && turn == chessGame.turn;
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, turn);
    }
}
