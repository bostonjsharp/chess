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
        if (pieceType == ChessPiece.PieceType.BISHOP || pieceType == ChessPiece.PieceType.QUEEN) {
            for (int i = 0; i < BishopSteps.length; i++) {
                for (int j = 1; j < 7; j++) {
                    ChessPosition endPosition = new ChessPosition(myPosition.getRow() + (j * BishopSteps[i][0]), myPosition.getColumn() + (j * BishopSteps[i][1]));
                    if (!this.isOnBoard(endPosition)) {
                        break;
                    }
                    if (board.getPiece(endPosition) == null) {
                        movesList.add(new ChessMove(myPosition, endPosition, null));
                        continue;
                    }
                    if (board.getPiece(endPosition) != null && board.getPiece(endPosition).getTeamColor() != this.color) {
                        movesList.add(new ChessMove(myPosition, endPosition, null));
                        break;
                    }
                    if (board.getPiece(endPosition).getTeamColor() == this.color) {
                        break;
                    }


                }
            }
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
            ChessPosition normalEndPosition = null;
            ChessPosition starterRowEndPosition = null;
            ChessPosition diag1 = null;
            ChessPosition diag2 = null;

            if(this.color == ChessGame.TeamColor.BLACK){
                normalEndPosition = new ChessPosition(myPosition.getRow() -1, myPosition.getColumn());
                starterRowEndPosition = new ChessPosition(myPosition.getRow() -2, myPosition.getColumn());
                diag1 = new ChessPosition(myPosition.getRow() -1, myPosition.getColumn() -1);
                diag2 = new ChessPosition(myPosition.getRow() -1, myPosition.getColumn() +1);

            }
            if(this.color == ChessGame.TeamColor.WHITE){
                normalEndPosition = new ChessPosition(myPosition.getRow() +1, myPosition.getColumn());
                starterRowEndPosition = new ChessPosition(myPosition.getRow() +2, myPosition.getColumn());
                diag1 = new ChessPosition(myPosition.getRow() +1, myPosition.getColumn() -1);
                diag2 = new ChessPosition(myPosition.getRow() +1, myPosition.getColumn() +1);
            }
            if(board.getPiece(normalEndPosition) == null){
                if((this.color == ChessGame.TeamColor.BLACK && myPosition.getRow() == 7 && board.getPiece(starterRowEndPosition) == null ) || (this.color == ChessGame.TeamColor.WHITE && myPosition.getRow() == 2 && board.getPiece(starterRowEndPosition) == null)){
                    movesList.add(new ChessMove(myPosition,starterRowEndPosition, null));
                }
                if((this.color == ChessGame.TeamColor.BLACK && normalEndPosition.getRow() == 1) || (this.color == ChessGame.TeamColor.WHITE && normalEndPosition.getRow() == 8)){
                    movesList.add(new ChessMove(myPosition,normalEndPosition, ChessPiece.PieceType.QUEEN));
                    movesList.add(new ChessMove(myPosition,normalEndPosition, ChessPiece.PieceType.BISHOP));
                    movesList.add(new ChessMove(myPosition,normalEndPosition, ChessPiece.PieceType.KNIGHT));
                    movesList.add(new ChessMove(myPosition,normalEndPosition, ChessPiece.PieceType.ROOK));
                }
                else {
                    movesList.add(new ChessMove(myPosition, normalEndPosition, null));
                }
            }
            if(isOnBoard(diag1) && board.getPiece(diag1) != null) {
                if (board.getPiece(diag1).getTeamColor() != this.color) {
                    if ((this.color == ChessGame.TeamColor.BLACK && diag1.getRow() == 1) || (this.color == ChessGame.TeamColor.WHITE && diag1.getRow() == 8)) {
                        movesList.add(new ChessMove(myPosition, diag1, ChessPiece.PieceType.QUEEN));
                        movesList.add(new ChessMove(myPosition, diag1, ChessPiece.PieceType.BISHOP));
                        movesList.add(new ChessMove(myPosition, diag1, ChessPiece.PieceType.KNIGHT));
                        movesList.add(new ChessMove(myPosition, diag1, ChessPiece.PieceType.ROOK));
                    }
                    else {
                        movesList.add(new ChessMove(myPosition, diag1, null));
                    }
                }
            }
            if(isOnBoard(diag2) && board.getPiece(diag2) != null) {
                if (board.getPiece(diag2).getTeamColor() != this.color) {
                    if ((this.color == ChessGame.TeamColor.BLACK && diag2.getRow() == 1) || (this.color == ChessGame.TeamColor.WHITE && diag2.getRow() == 8)) {
                        movesList.add(new ChessMove(myPosition, diag2, ChessPiece.PieceType.QUEEN));
                        movesList.add(new ChessMove(myPosition, diag2, ChessPiece.PieceType.BISHOP));
                        movesList.add(new ChessMove(myPosition, diag2, ChessPiece.PieceType.KNIGHT));
                        movesList.add(new ChessMove(myPosition, diag2, ChessPiece.PieceType.ROOK));
                    }
                    else {
                        movesList.add(new ChessMove(myPosition, diag2, null));
                    }
                }
            }
            //can only move forward unless diagonal has enemy
            //forward two spaces only on first move
            //en passant?
            //promoted to different piece if it reaches the end of the board
            //black pawns and white pawns move in different directions
            //each possible promotion counts as a possible move.

        }
        if (pieceType == ChessPiece.PieceType.ROOK ||pieceType == ChessPiece.PieceType.QUEEN) {
            //slides in cardinal directions
            //stops at edges, friendlies, captures piece
            //checks next step, checks if on board, empty, occupied, break.
            for (int i = 0; i < RookSlides.length; i++) {
                for (int j = 1; j < 7; j++) {
                    ChessPosition endPosition = new ChessPosition(myPosition.getRow() + (j * RookSlides[i][0]), myPosition.getColumn() + (j * RookSlides[i][1]));
                    if (!this.isOnBoard(endPosition)) {
                        break;
                    }
                    if (board.getPiece(endPosition) == null) {
                        movesList.add(new ChessMove(myPosition, endPosition, null));
                        continue;
                    }
                    if (board.getPiece(endPosition) != null && board.getPiece(endPosition).getTeamColor() != this.color) {
                        movesList.add(new ChessMove(myPosition, endPosition, null));
                        break;
                    }
                    if (board.getPiece(endPosition).getTeamColor() == this.color) {
                        break;
                    }


                }
            }

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

    private int[][] BishopSteps = {
            {1, 1},
            {-1,1},
            {-1,-1},
            {1,-1}
    };

    private int[][] RookSlides = {
            {0, 1},
            {0,-1},
            {1,0},
            {-1,0}
    };


}
