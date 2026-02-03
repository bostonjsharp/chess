package chess;
import java.util.Collection;
import java.util.ArrayList;

public class Moves {
    private ChessPiece.PieceType pieceType;
    private ChessGame.TeamColor color;

    private boolean isOnBoard(ChessPosition position) {
        if(position.getRow() > 8 ||
                position.getRow() < 1 ||
                position.getColumn() > 8 ||
                position.getColumn() < 1){
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
        if (pieceType == ChessPiece.PieceType.BISHOP ||
                pieceType == ChessPiece.PieceType.QUEEN) {
            slideRules(myPosition, movesList, bishopSteps, board);
        }
        if (pieceType == ChessPiece.PieceType.ROOK ||
                pieceType == ChessPiece.PieceType.QUEEN) {
            slideRules(myPosition, movesList, rookSlides, board);

        }
        if (pieceType == ChessPiece.PieceType.KING) {
            jumpRules(myPosition, movesList, kingOffsets, board);

        }
        if (pieceType == ChessPiece.PieceType.KNIGHT) {
            jumpRules(myPosition, movesList, knightOffsets, board);

        }
        if(pieceType == ChessPiece.PieceType.PAWN){
            ChessPosition normalEndPosition = null;
            ChessPosition specialEndPosition = null;
            ChessPosition diag1 = null;
            ChessPosition diag2 = null;
            int promoRow = 1;
            int startRow = 1;

            if(color == ChessGame.TeamColor.WHITE){
                normalEndPosition = new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn());
                specialEndPosition = new ChessPosition(myPosition.getRow() + 2, myPosition.getColumn());
                diag1 = new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() +1);
                diag2 = new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() -1);
                promoRow = 8;
                startRow = 2;

            }
            if(color == ChessGame.TeamColor.BLACK){
                normalEndPosition = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn());
                specialEndPosition = new ChessPosition(myPosition.getRow() - 2, myPosition.getColumn());
                diag1 = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() +1);
                diag2 = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() -1);
                promoRow = 1;
                startRow = 7;

            }
            if(isOnBoard(normalEndPosition) && board.getPiece(normalEndPosition) == null){
                if(normalEndPosition.getRow() == promoRow){
                    movesList.add(new ChessMove(myPosition, normalEndPosition, ChessPiece.PieceType.QUEEN));
                    movesList.add(new ChessMove(myPosition, normalEndPosition, ChessPiece.PieceType.BISHOP));
                    movesList.add(new ChessMove(myPosition, normalEndPosition, ChessPiece.PieceType.ROOK));
                    movesList.add(new ChessMove(myPosition, normalEndPosition, ChessPiece.PieceType.KNIGHT));
                }
                else{
                    movesList.add(new ChessMove(myPosition, normalEndPosition, null));
                }
            }
            if(myPosition.getRow() == startRow &&
                    board.getPiece(normalEndPosition) == null &&
                    board.getPiece(specialEndPosition) == null){
                movesList.add(new ChessMove(myPosition, specialEndPosition, null));
            }
            if(isOnBoard(diag1) && board.getPiece(diag1) != null && board.getPiece(diag1).getTeamColor() != color){
                if(diag1.getRow() == promoRow){
                    movesList.add(new ChessMove(myPosition, diag1, ChessPiece.PieceType.QUEEN));
                    movesList.add(new ChessMove(myPosition, diag1, ChessPiece.PieceType.BISHOP));
                    movesList.add(new ChessMove(myPosition, diag1, ChessPiece.PieceType.ROOK));
                    movesList.add(new ChessMove(myPosition, diag1, ChessPiece.PieceType.KNIGHT));
                }
                else{
                    movesList.add(new ChessMove(myPosition, diag1, null));
                }
            }
            if(isOnBoard(diag2) && board.getPiece(diag2) != null && board.getPiece(diag2).getTeamColor() != color){
                if(diag2.getRow() == promoRow){
                    movesList.add(new ChessMove(myPosition, diag2, ChessPiece.PieceType.QUEEN));
                    movesList.add(new ChessMove(myPosition, diag2, ChessPiece.PieceType.BISHOP));
                    movesList.add(new ChessMove(myPosition, diag2, ChessPiece.PieceType.ROOK));
                    movesList.add(new ChessMove(myPosition, diag2, ChessPiece.PieceType.KNIGHT));
                }
                else{
                    movesList.add(new ChessMove(myPosition, diag2, null));
                }
            }
        }
        return movesList;
    }

    private void slideRules(ChessPosition myPosition, Collection<ChessMove> movesList, int[][] offsetSet, ChessBoard board){
        for (int i = 0; i < offsetSet.length; i++) {
            for (int j = 1; j < 7; j++) {
                ChessPosition endPosition = new ChessPosition(myPosition.getRow()+(j*offsetSet[i][0]),myPosition.getColumn()+(j*offsetSet[i][1]));
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

    private void jumpRules(ChessPosition myPosition, Collection<ChessMove> movesList, int[][] offsetSet, ChessBoard board){
        for(int i = 0; i < offsetSet.length; i++) {
            ChessPosition endPosition = new ChessPosition(myPosition.getRow()+offsetSet[i][0],myPosition.getColumn()+offsetSet[i][1]);
            if(!this.isOnBoard(endPosition)){
                continue;
            }
            if(board.getPiece(endPosition) == null || board.getPiece(endPosition).getTeamColor() != this.color) {
                movesList.add(new ChessMove(myPosition,endPosition, null));
            }


        }

    }

    private static int[][] kingOffsets = {
            {-1,-1},
            {-1,0},
            {-1,1},
            {0,-1},
            {0,1},
            {1,-1},
            {1,0},
            {1,1}
    };

    private static int[][] knightOffsets = {
            {2, 1},
            {2,-1},
            {-1,2},
            {1,2},
            {-2,1},
            {-2,-1},
            {1,-2},
            {-1,-2}
    };

    private static int[][] bishopSteps = {
            {1, 1},
            {-1,1},
            {-1,-1},
            {1,-1}
    };

    private static int[][] rookSlides = {
            {0, 1},
            {0,-1},
            {1,0},
            {-1,0}
    };


}
