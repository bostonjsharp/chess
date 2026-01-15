package chess;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return color == that.color && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, type);
    }

    private ChessGame.TeamColor color;
    private ChessPiece.PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.color = pieceColor;
        this.type = type;

    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return color;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
       ChessPiece piece = board.getPiece(myPosition);
       if (piece.getPieceType() == PieceType.BISHOP) {
           //slides in diagonal directions
           //stops if hits edges, friendlies, or captures piece
           return List.of(new ChessMove(new ChessPosition(5,4), new ChessPosition(1,8), null));
       }
       if (piece.getPieceType() == PieceType.KING) {
           //can only go one space in any direction
           //stopped by edges, friendlies.
           //uses offsets...?

       }
       if (piece.getPieceType() == PieceType.PAWN) {
           //can only move forward unless diagonal has enemy
           //forward two spaces only on first move
           //en passant?
           //promoted to different piece if it reaches the end of the board

       }
       if (piece.getPieceType() == PieceType.ROOK) {
           //slides in cardinal directions
           //stops at edges, friendlies, caputres piece

       }
       if (piece.getPieceType() == PieceType.KNIGHT) {
           //2 spaces, then three spaces (L shape)
           //jumps over pieces
           //offsets as well...?

       }
       if (piece.getPieceType() == PieceType.QUEEN) {
           //rook and bishop moves + king moves

       }

        return List.of();
    }
}
