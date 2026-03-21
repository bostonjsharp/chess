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
        int endRow = (view == ChessGame.TeamColor.WHITE) ? 1 : 8;
        int rowStep = (view == ChessGame.TeamColor.WHITE) ? -1 : 1;
        int startCol = (view == ChessGame.TeamColor.WHITE) ? 1 : 8;
        int endCol = (view == ChessGame.TeamColor.WHITE) ? 8: 1;
        int colStep = (view == ChessGame.TeamColor.WHITE) ? 1: -1;

        for (int row = startRow; (rowStep > 0) ? row <= endRow : row>= endRow; row += rowStep) {
            System.out.print(SET_BG_COLOR_BLACK + SET_TEXT_COLOR_WHITE + " " + row + " ");
            for (int col = startCol;(colStep > 0) ? col <= endCol : col>= endCol; col += colStep){
                boolean lightSquare = (row + col) % 2 == 1;
                ChessPiece piece = board.getPiece(new ChessPosition(row,col));
                printSquare(lightSquare, piece);
                if (col == endCol){
                    break;
                }
            }
            System.out.print(SET_BG_COLOR_BLACK + SET_TEXT_COLOR_WHITE + " " + row + " ");
            System.out.println(RESET_BG_COLOR + RESET_TEXT_COLOR);
            if(row == endRow){
                break;
            }
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

    private void printSquare(boolean lightSquare, ChessPiece piece) {
        String bg = lightSquare ? LIGHT_SQUARE : DARK_SQUARE;
        System.out.print(bg);

        if (piece == null) {
            System.out.print(EM_SPACE + " " + EM_SPACE);
            return;
        }
        String textColor = (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? WHITE_PIECE_COLOR : BLACK_PIECE_COLOR;
        System.out.print(textColor);
        System.out.print(EM_SPACE + getPieceSymbol(piece) + EM_SPACE);
        System.out.print(RESET_TEXT_COLOR);
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
