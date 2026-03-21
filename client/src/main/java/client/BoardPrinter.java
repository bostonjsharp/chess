package client;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import static ui.EscapeSequences.*;

public class BoardPrinter {

    public void drawBoard(ChessGame game, ChessGame.TeamColor view) {
        ChessBoard board = game.getBoard();

        printColLabels(view);

        int startRow = (view == ChessGame.TeamColor.WHITE) ? 8 : 1;
        int endRow = (view == ChessGame.TeamColor.WHITE) ? 0 : 9;
        int rowStep = (view == ChessGame.TeamColor.WHITE) ? -1 : 1;

        for (int row = startRow; row != endRow; row += rowStep) {
            System.out.print(SET_BG_COLOR_BLACK + SET_TEXT_COLOR_WHITE + " " + row + " ");
            int startCol = (view == ChessGame.TeamColor.WHITE) ? 1 : 8;
            int endCol = (view == ChessGame.TeamColor.WHITE) ? 9: 0;
            int colStep = (view == ChessGame.TeamColor.WHITE) ? 1: -1;

            for (int col = startCol; col != endCol; col += colStep){
                boolean lightSquare = (row + col) % 2 == 1;
                setSquareColor(lightSquare);
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);
                printPiece(piece);
            }
            System.out.print(SET_BG_COLOR_BLACK + SET_TEXT_COLOR_WHITE + " " + row + " ");
            System.out.println(RESET_BG_COLOR + RESET_TEXT_COLOR);
        }
        printColLabels(view);
        System.out.println(RESET_BG_COLOR + RESET_TEXT_COLOR);
    }

    private void printColLabels(ChessGame.TeamColor view){
        System.out.print(SET_BG_COLOR_BLACK + SET_TEXT_COLOR_WHITE + "   ");

        if(view == ChessGame.TeamColor.WHITE){
            for (char col ='a'; col <= 'h'; col++) {
                System.out.print(" " + col + " ");
            }
        } else {
            for (char col = 'h'; col >= 'a'; col--){
                System.out.print(" " + col + " ");
            }
        }
        System.out.println("   " + RESET_BG_COLOR + RESET_TEXT_COLOR);
    }

    private void setSquareColor(boolean lightSquare) {
        if (lightSquare) {
            System.out.print(SET_BG_COLOR_LIGHT_GREY);
        } else {
            System.out.print(SET_BG_COLOR_DARK_GREY);
        }
    }

    private void printPiece(ChessPiece piece) {
        if (piece == null) {
            System.out.print("   ");
            return;
        }
        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE){
            System.out.print(SET_TEXT_COLOR_WHITE);
        } else {
            System.out.print(SET_TEXT_COLOR_BLACK);
        }
        System.out.print(" " + getPieceSymbol(piece) + " ");
    }

    private String getPieceSymbol(ChessPiece piece) {
        return switch (piece.getPieceType()) {
            case KING -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_KING : BLACK_KING;
            case QUEEN -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_QUEEN : BLACK_QUEEN;
            case BISHOP -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_BISHOP : BLACK_BISHOP;
            case KNIGHT -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_KNIGHT : BLACK_KNIGHT;
            case ROOK -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_ROOK : BLACK_ROOK;
            case PAWN -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_PAWN : BLACK_PAWN;
        };
    }
}
