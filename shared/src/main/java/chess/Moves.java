package chess;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

public class Moves {
//    var moves = {
//            moves:
//
//    {
//        getPossible()
//    }
//}
    private ChessPiece.PieceType pieceType;
    private ChessGame.TeamColor color;

    private boolean isOnBoard(ChessPosition position) {
        if(position.getRow() > 8 || position.getRow() < 1 || position.getColumn() > 8 || position.getColumn() < 1){
            return false;
        }
        return true;
    }

    public Moves(ChessPiece.PieceType pieceType, ChessGame.TeamColor color){
        this.pieceType = pieceType;
        this.color = color;


    }

    public Collection<ChessMove> getValid(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> movesList = new ArrayList();
        if (pieceType == ChessPiece.PieceType.BISHOP) {

            //slides in diagonal directions
            //stops if hits edges, friendlies, or captures piece
//           return List.of(new ChessMove(new ChessPosition(5,4), new ChessPosition(1,8), null));
        }
        if (pieceType == ChessPiece.PieceType.KING) {
            for(int i = 0; i < KingOffsets.length; i++) {
                ChessPosition endPosition = new ChessPosition(myPosition.getRow() + KingOffsets[i][0], myPosition.getColumn() + KingOffsets[i][1]);
                if(!this.isOnBoard(endPosition)){
                    continue;
                }
                if(board.getPiece(endPosition) == null || board.getPiece(endPosition).getTeamColor() != this.color) {
                    movesList.add(new ChessMove(myPosition,endPosition, null));
                }


            }
            //can only go one space in any direction
            //stopped by edges, friendlies.
            //uses offsets...? -> try the square to see if it's valid

        }
        if (pieceType == ChessPiece.PieceType.PAWN) {
            //can only move forward unless diagonal has enemy
            //forward two spaces only on first move
            //en passant?
            //promoted to different piece if it reaches the end of the board
            //black pawns and white pawns move in different directions
            //each possible promotion counts as a possible move.

        }
        if (pieceType == ChessPiece.PieceType.ROOK) {
            //slides in cardinal directions
            //stops at edges, friendlies, captures piece
            //checks next step, checks if on board, empty, occupied, break.

        }
        if (pieceType == ChessPiece.PieceType.KNIGHT) {
            for(int i = 0; i < KnightOffsets.length; i++) {
                ChessPosition endPosition = new ChessPosition(myPosition.getRow() + KnightOffsets[i][0], myPosition.getColumn() + KnightOffsets[i][1]);
                if(!this.isOnBoard(endPosition)){
                    continue;
                }
                if(board.getPiece(endPosition) == null || board.getPiece(endPosition).getTeamColor() != this.color) {
                    movesList.add(new ChessMove(myPosition,endPosition, null));
                }


            }

        }
        if (pieceType == ChessPiece.PieceType.QUEEN) {
            //rook and bishop moves + king moves


        }

        return movesList;
    }

    private int[][] KingOffsets = {
            {-1,-1},
            {-1,0},
            {-1,1},
            {0,-1},
            {0,1},
            {1,-1},
            {1,0},
            {1,1}
    };

    private int[][] KnightOffsets = {
            {2, 1},
            {2,-1},
            {-1,2},
            {1,2},
            {-2,1},
            {-2,-1},
            {1,-2},
            {-1,-2}
    };

}
